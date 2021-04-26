package com.io.image.manager.service;

import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
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
    public Optional<BufferedImage> fetchAndCacheImage(
            OriginServer origin,
            String filename,
            List<ImageOperation> operations
    ) throws IOException, ImageOperationException, ImageNotFoundException {
        Optional<BufferedImage> image = fetchLocalImage(origin, filename, operations);
        if (image.isPresent()) {
            return image;
        }

        // try to fetch local image but without any operations
        if (operations.size() > 0) {
            image = fetchLocalImage(origin, filename, Collections.emptyList());
        }

        // if no local image has been found then ask the origin server
        if (image.isEmpty()) {
            image = fetchRemoteImage(origin, filename);

            // found remote image, increment miss counter
            if (image.isPresent()) {
                missCounter.increment();

                // cache image without operations for an optimization
                cache.storeImage(origin, image.get(), filename, Collections.emptyList());
            }
        }

        if (image.isPresent()) {
            var img = image.get();
            for (var op : operations) {
                img = op.run(img);
            }
            // cache image after performing operations
            cache.storeImage(origin, img, filename, operations);

            return Optional.of(img);
        }
        throw new ImageNotFoundException("Image not found at origin: " + origin.getUrl());
    }

    private Optional<BufferedImage> fetchRemoteImage(OriginServer origin, String filename) {
        try {
            // FIXME: this does not look pretty
            URL url = new URL(origin.getUrl() + filename);
            URLConnection conn = url.openConnection();
            Map<String,List<String>> headers = conn.getHeaderFields();
            List<String> cacheHead = headers.get("Cache-Control");
            List<String> eTag = headers.get("ETag");
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

    private Optional<BufferedImage> fetchLocalImage(
            OriginServer origin,
            String filename,
            List<ImageOperation> operations
    ) throws IOException {
        return cache.loadImage(origin, filename, operations);
    }
}
