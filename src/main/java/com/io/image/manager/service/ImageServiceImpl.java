package com.io.image.manager.service;

import com.io.image.manager.cache.CacheResult;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.core.io.AbstractResource;
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
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {
    private final AppConfigurationProperties props;
    private final ImageCache cache;
    private final Counter missCounter;
    private final DistributionSummary originTrafficSummary;

    public ImageServiceImpl(AppConfigurationProperties props, ImageCache cache, PrometheusMeterRegistry mr) {
        this.props = props;
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
    ) throws IOException, ImageOperationException, ImageNotFoundException {
        // check if image from given origin and with particular operations is already in cache
        var cacheResult = cache.checkInCache(origin, filename, operations, info);
        if (cacheResult.isPresent()) {
            return cacheResult.get();
        }

        // image has not been found in cache therefore try to load original image from cache, without applied operations
        var image = cache.loadImage(origin, filename, Collections.emptyList(), info);

        // if no local image has been found then ask the origin server
        if (image.isEmpty()) {
            image = fetchRemoteImage(origin, filename);

            // found remote image, increment miss counter
            if (image.isPresent()) {
                missCounter.increment();

                // cache image without operations for an optimization
                cache.storeImage(origin, image.get(), filename, Collections.emptyList(), info);
            }
        }

        if (image.isPresent()) {
            var img = image.get();
            for (var op : operations) {
                img = op.run(img);
            }
            img = compressImage(img, info);
            // cache image after performing operations
            return cache.storeImage(origin, img, filename, operations, info);
        }
        throw new ImageNotFoundException("Image not found at origin: " + origin.getUrl());
    }

    private Optional<BufferedImage> fetchRemoteImage(OriginServer origin, String filename) {
        try {
            // FIXME: this does not look pretty
            URL url = new URL(origin.getUrl() + filename);
            URLConnection conn = url.openConnection();
            Map<String,List<String>> headers = conn.getHeaderFields();
            Optional<List<String>> cacheHead = Optional.empty();
            if(headers.containsKey("Cache-Control")) {
                cacheHead = Optional.of(headers.get("Cache-Control"));
            }
            Optional<List<String>> eTag = Optional.empty();
            if(headers.containsKey("ETag")) {
                 eTag = Optional.of(headers.get("ETag"));
            }
            byte[] imgBytes = conn.getInputStream().readAllBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
            if (image == null) {
                return Optional.empty();
            } else {
                originTrafficSummary.record(imgBytes.length);
                return Optional.of(image);
            }
        } catch (Exception e) {
            return Optional.empty();
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
}
