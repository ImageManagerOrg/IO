package com.io.image.manager.service;

import com.io.image.manager.cache.CacheResult;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.models.CacheRecord;
import com.io.image.manager.models.CacheRecordRepository;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import com.io.image.manager.service.operations.ImageOperationParser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.Remote;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ImageServiceImpl implements ImageService {
    private final AppConfigurationProperties props;
    private final CacheRecordRepository repository;
    private final ImageCache cache;
    private final Counter missCounter;
    private final DistributionSummary originTrafficSummary;
    private final Pattern maxAgePattern = Pattern.compile("max-age=([0-9]+)");

    public ImageServiceImpl(AppConfigurationProperties props, CacheRecordRepository repository, ImageCache cache, PrometheusMeterRegistry mr) {
        this.props = props;
        this.repository = repository;
        this.cache = cache;
        missCounter = Counter.builder("cache.miss.count").register(mr);
        originTrafficSummary = DistributionSummary
                .builder("origin.traffic.size")
                .baseUnit("bytes") // optional
                .register(mr);
    }

    @Override
    public CacheResult fetchAndCacheImage(
            OriginServer origin,
            String filename,
            List<ImageOperation> operations,
            ConversionInfo info
    ) throws IOException, ImageOperationException, ImageNotFoundException, ConversionException {
        // check if image from given origin and with particular operations is already in cache
        var cacheResult = cache.checkInCache(origin, filename, operations, info);
        if (cacheResult.isPresent()) {
            try {
                repository.incrementImageHit(origin.getHost(), cacheResult.get().resultHash());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return cacheResult.get();
        }

        cacheResult = fetchLocalProcessAndCache(origin, filename, operations, info);

        if (cacheResult.isPresent()) {
            return cacheResult.get();
        }

        cacheResult = fetchRemoteProcessAndCache(origin, filename, operations, info);
        if (cacheResult.isPresent()) {
            return cacheResult.get();
        }

        throw new ImageNotFoundException("Image not found at origin: " + origin.getUrl());
    }

    @Data
    private static class RemoteFetchResult {
        private final Optional<BufferedImage> image;
        private final Optional<String> cacheControl;
        private final Optional<String> etag;

        public final static RemoteFetchResult EMPTY_FETCH_RESULT = new RemoteFetchResult(Optional.empty(), Optional.empty(), Optional.empty());
    }

    private RemoteFetchResult fetchRemoteImage(OriginServer origin, String filename) {
        try {
            URL url = new URL(origin.getUrl() + filename);
            URLConnection conn = url.openConnection();

            var headers = conn.getHeaderFields();

            Optional<String> cacheControl = Optional.empty();
            if(headers.containsKey("Cache-Control")) {
                cacheControl = headers.get("Cache-Control").stream().findFirst();
            }

            Optional<String> etag = Optional.empty();
            if(headers.containsKey("ETag")) {
                 etag = headers.get("ETag").stream().findFirst();
            }

            byte[] imgBytes = conn.getInputStream().readAllBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
            if (image == null) {
                return RemoteFetchResult.EMPTY_FETCH_RESULT;
            }

            originTrafficSummary.record(imgBytes.length);

            return new RemoteFetchResult(Optional.of(image), cacheControl, etag);
        } catch (Exception e) {
            return RemoteFetchResult.EMPTY_FETCH_RESULT;
        }
    }

    public BufferedImage compressImage(BufferedImage image, ConversionInfo conversionInfo) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        var format = conversionInfo.getFormat();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        ImageWriter writer = (ImageWriter) writers.next();
        ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);


        ImageOutputStream ios = ImageIO.createImageOutputStream(bao);
        writer.setOutput(ios);

        if (format.equals("png")) {
            imageWriteParam.setCompressionQuality(conversionInfo.getPngRate());
        } else {
            imageWriteParam.setCompressionQuality(conversionInfo.getJpgRate());
        }
        writer.write(null, new IIOImage(image, null, null), imageWriteParam);

        var is = new ByteArrayInputStream(bao.toByteArray());
        return ImageIO.read(is);
    }

    private Optional<CacheResult> fetchRemoteProcessAndCache(OriginServer origin, String filename, List<ImageOperation> operations, ConversionInfo info) throws ConversionException, IOException, ImageOperationException {
        var remoteFetchResult = fetchRemoteImage(origin, filename);

        if (remoteFetchResult.image.isPresent()) {
            missCounter.increment();

            var maxAge = maxAgePattern.matcher(remoteFetchResult.cacheControl.orElse("max-age=0"));
            long ttl = 0L;
            if (maxAge.find()) {
                ttl = Long.parseLong(maxAge.group(1));
            }

            // cache image without operations and conversion for an optimization
            var originalCacheResult = cache.storeImage(origin, remoteFetchResult.image.get(), filename, Collections.emptyList(), ImageOperationParser.getDefaultConversionInfo(info.getFormat()));
            repository.save(new CacheRecord(origin.getHost(), filename, originalCacheResult.resultHash(), originalCacheResult.totalResourceSizeInBytes(),  remoteFetchResult.etag.orElse(""), ttl));

            // process image
            var image = processImage(remoteFetchResult.image.get(), operations, info);

            // store image
            var storeResult = cache.storeImage(origin, image, filename, operations, info);

            if (!storeResult.resultHash().equals(originalCacheResult.resultHash())) {
                repository.save(new CacheRecord(origin.getHost(), filename, storeResult.resultHash(), storeResult.totalResourceSizeInBytes(),  remoteFetchResult.etag.orElse(""), ttl));
            }

            return Optional.of(storeResult);
        }
        return Optional.empty();
    }

    private Optional<CacheResult> fetchLocalProcessAndCache(OriginServer origin, String filename, List<ImageOperation> operations, ConversionInfo info)  throws ConversionException, IOException, ImageOperationException {
        var foundImage = cache.loadImage(origin, filename, Collections.emptyList(), ImageOperationParser.getDefaultConversionInfo(info.getFormat()));

        if (foundImage.isEmpty()) {
            return Optional.empty();
        }

        var image = foundImage.get();
        image = processImage(image, operations, info);
        var result = cache.storeImage(origin, image, filename, operations, info);

        var originalHash = cache.cacheHash(
                origin,
                filename,
                Collections.emptyList(),
                ImageOperationParser.getDefaultConversionInfo(info.getFormat()));

        var originalRecord = repository .findByOriginAndNameHash( origin.getHost(), originalHash);

        if (originalRecord.isPresent()) {
            repository.save(originalRecord.get().cloneWithNewHash(result.resultHash(), result.totalResourceSizeInBytes()));
        } else {
            // this should not happen but just in case save it in database
            repository.save(new CacheRecord(origin.getHost(), filename, result.resultHash(), result.totalResourceSizeInBytes(), "", 0L));
        }

        return Optional.of(result);
    }

    private BufferedImage processImage(BufferedImage image, List<ImageOperation> operations, ConversionInfo info) throws ImageOperationException, IOException {
        for (var op : operations) {
            image = op.run(image);
        }
        return compressImage(image, info);
    }
}
