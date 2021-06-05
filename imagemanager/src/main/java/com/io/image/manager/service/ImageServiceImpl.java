package com.io.image.manager.service;

import com.io.image.manager.cache.CacheResult;
import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.models.CacheRecord;
import com.io.image.manager.models.CacheRecordRepository;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import com.io.image.manager.service.operations.ImageOperationParser;
import com.io.image.manager.service.ConnectivityService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.Data;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.HttpStatus;
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
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ImageServiceImpl implements ImageService {
    private final CacheRecordRepository repository;
    private final ImageCache cache;
    private final Counter missCounter;
    private final DistributionSummary originTrafficSummary;
    private final ConnectionService connectionService;
    private final Pattern maxAgePattern = Pattern.compile("max-age=([0-9]+)");
    private final ConnectivityService connectivityService;

    public ImageServiceImpl(CacheRecordRepository repository, ImageCache cache, PrometheusMeterRegistry mr, ConnectionService connectionService, ConnectivityService connectivityServic) {
        this.repository = repository;
        this.cache = cache;
        missCounter = Counter.builder("cache.miss.count").register(mr);
        originTrafficSummary = DistributionSummary
                .builder("origin.traffic.size")
                .baseUnit("bytes") // optional
                .register(mr);
        this.connectionService = connectionService;
        this.connectivityService = connectivityServic;
    }

    @Override
    public CacheResult fetchAndCacheImage(
            OriginServer origin,
            String filename,
            List<ImageOperation> operations,
            ConversionInfo info
    ) throws IOException, ImageOperationException, ImageNotFoundException, ConversionException {
        var imageHash = cache.cacheHash(origin, filename, operations, info);

        var cacheEntry = repository.findByOriginAndNameHash(origin.getHost(), imageHash);
        if (cacheEntry.isPresent() && !cacheEntry.get().isTTLValid()) {
            revalidateImage(origin, filename, info);
        }

        var cacheResult = cache.checkInCache(origin, filename, operations, info);
        if (cacheResult.isPresent()) {
            try {
                repository.incrementImageHit(origin.getHost(), cacheResult.get().resultHash());
                int ttl = repository.getTTL(origin.getHost(), cacheResult.get().resultHash());
                cacheResult.get().setTTL(ttl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return cacheResult.get();
        }

        cacheResult = fetchLocalProcessAndCache(origin, filename, operations, info);

        if (cacheResult.isPresent()) {
            return cacheResult.get();
        }

        connectivityService.registerRequest(origin);
        cacheResult = fetchRemoteProcessAndCache(origin, filename, operations, info);
        if (cacheResult.isPresent()) {
            return cacheResult.get();
        }
        connectivityService.registerFail(origin);
        throw new ImageNotFoundException("Image not found at origin: " + origin.getUrl());
    }


    @Data
    private static class RemoteFetchResult {
        private final Optional<BufferedImage> image;
        private final int bytes;
        private final Optional<String> cacheControl;
        private final Optional<String> etag;
        public final static RemoteFetchResult EMPTY_FETCH_RESULT = new RemoteFetchResult(Optional.empty(), 0, Optional.empty(), Optional.empty());
    }

    private RemoteFetchResult fetchRemoteImage(OriginServer origin, String filename) {
        try {
            CloseableHttpClient client = connectionService.getHttpClient();
            HttpGet get = new HttpGet(origin.getUrl() + filename);
            CloseableHttpResponse response = client.execute(get);

            var result = fetchImageFromHttpResponse(response);
            response.close();

            originTrafficSummary.record(result.getBytes());

            return result;
        } catch (Exception e) {
            return RemoteFetchResult.EMPTY_FETCH_RESULT;
        }
    }

    private RemoteFetchResult fetchImageFromHttpResponse(CloseableHttpResponse response) {
        try {
            var entity = response.getEntity();
            InputStream is = entity.getContent();
            Optional<String> cacheControl = Optional.empty();
            if (response.getHeaders("Cache-Control") != null) {
                cacheControl = Optional.of(response.getHeaders("Cache-Control").toString());
            }
            Optional<String> etag = Optional.empty();
            if (response.getHeaders("ETag") != null) {
                etag = Optional.of(response.getFirstHeader("ETag").getValue());
            }
            byte[] imgBytes = is.readAllBytes();
            is.close();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
            if (image == null) {
                return RemoteFetchResult.EMPTY_FETCH_RESULT;
            }
            return new RemoteFetchResult(Optional.of(image), imgBytes.length, cacheControl, etag);
        } catch (Exception e) {
            e.printStackTrace();
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

            long ttl = parseTtl(remoteFetchResult.cacheControl.orElse("max-age=0"));

            // cache image without operations and conversion for an optimization
            var originalCacheResult = cache.storeImage(origin, remoteFetchResult.image.get(), filename, Collections.emptyList(), ImageOperationParser.getDefaultConversionInfo(info.getFormat()));
            repository.save(new CacheRecord(origin.getHost(), filename, originalCacheResult.resultHash(), originalCacheResult.totalResourceSizeInBytes(), remoteFetchResult.etag.orElse(""), ttl));

            // process image
            var image = processImage(remoteFetchResult.image.get(), operations, info);

            // store image
            var storeResult = cache.storeImage(origin, image, filename, operations, info);

            if (!storeResult.resultHash().equals(originalCacheResult.resultHash())) {
                repository.save(new CacheRecord(origin.getHost(), filename, storeResult.resultHash(), storeResult.totalResourceSizeInBytes(), remoteFetchResult.etag.orElse(""), ttl));
            }
            storeResult.setTTL(ttl);

            return Optional.of(storeResult);
        }
        return Optional.empty();
    }

    private Optional<CacheResult> fetchLocalProcessAndCache(OriginServer origin, String filename, List<ImageOperation> operations, ConversionInfo info) throws ConversionException, IOException, ImageOperationException {
        var record = repository.findByOriginAndNameHash(origin.getHost(), cache.cacheHash(origin, filename, Collections.emptyList(), ImageOperationParser.getDefaultConversionInfo(info.getFormat())));

        // I'm lazy and don't check for ttl in the origin, whatever, fetch it one more time
        if (record.isPresent() && !record.get().isTTLValid()) {
            if (!revalidateImage(origin, filename, info)) {
                return Optional.empty();
            }
        }

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

        var originalRecord = repository.findByOriginAndNameHash(origin.getHost(), originalHash);

        if (originalRecord.isPresent()) {
            repository.save(originalRecord.get().cloneWithNewHash(result.resultHash(), result.totalResourceSizeInBytes()));
            result.setTTL(originalRecord.get().getTtl());
        } else {
            // this should not happen but just in case save it in database
            repository.save(new CacheRecord(origin.getHost(), filename, result.resultHash(), result.totalResourceSizeInBytes(), "", 0L));
            result.setTTL(0L);
        }

        return Optional.of(result);
    }

    private BufferedImage processImage(BufferedImage image, List<ImageOperation> operations, ConversionInfo info) throws ImageOperationException, IOException {
        for (var op : operations) {
            image = op.run(image);
        }
        return compressImage(image, info);
    }

    private Long parseTtl(String maxAgeString) {
        var maxAge = maxAgePattern.matcher(maxAgeString);
        long ttl = 0L;
        if (maxAge.find()) {
            ttl = Long.parseLong(maxAge.group(1));
        }
        return ttl;
    }

    // returns if image is still valid
    boolean revalidateImage(OriginServer origin, String filename, ConversionInfo info) {
        try {
            // check for unprocessed image hash
            var hash = cache.cacheHash(origin, filename, Collections.emptyList(), ImageOperationParser.getDefaultConversionInfo(info.getFormat()));
            var record = repository.findByOriginAndNameHash(origin.getHost(), hash);

            // we don't have an original image, delete every instance of the image and go on
            if (record.isEmpty()) {
                cache.purgeImage(origin, filename);

                // and this annoying parsing...
                if (filename.contains(".")) {
                    filename = filename.substring(0, filename.indexOf("."));
                }

                var imageId = Integer.parseInt(filename);
                repository.deleteImagesForOriginAndId(origin.getHost(), imageId);
                return false;
            }


            // original image has been found, continue to checking its etag validity
            CloseableHttpClient client = connectionService.getHttpClient();
            HttpGet request = new HttpGet(origin.getUrl() + filename);
            request.setHeader("If-None-Match", record.get().getEtag());
            CloseableHttpResponse response = client.execute(request);
            try {
                var statusCode = response.getStatusLine().getStatusCode();
                var maxAge = response.getFirstHeader("Cache-Control").getValue();

                if (statusCode == HttpStatus.NOT_MODIFIED.value()) {
                    // image has not been modified, update the ttl for all instances
                    var ttl = parseTtl(maxAge);
                    repository.updateTTLForAllImages(origin.getHost(), record.get().getImageId(), ttl);
                    return true;
                } else {
                    // delete all image instances from cache and continue on fetching it
                    cache.purgeImage(origin, filename);
                    repository.deleteImagesForOriginAndId(record.get().getOrigin(), record.get().getImageId());

                    // forget about the response having the image, too much hustle for this project, just call the http request again when needed...
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
