package com.io.image.manager.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import com.io.image.manager.cache.CacheResult;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import com.io.image.manager.service.operations.ImageOperationParser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.ResponseEntity;
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
            ConversionInfo info,
            CloseableHttpClient client
    ) throws IOException, ImageOperationException, ImageNotFoundException, ConversionException {
        // check if image from given origin and with particular operations is already in cache
        var cacheResult = cache.checkInCache(origin, filename, operations, info);
        if (cacheResult.isPresent()) {
            return cacheResult.get();
        }

        // TODO: apply correct default conversion info
        // image with given operations and conversion info has not been found in cache, try to find original image with default params
        var defaultConversion = ImageOperationParser.getDefaultConversionInfo("jpg");
        var image = cache.loadImage(origin, filename, Collections.emptyList(), defaultConversion);

        // if no local image has been found then ask the origin server
        if (image.isEmpty()) {
            image = fetchRemoteImage(origin, filename, client);

            // found remote image, increment miss counter
            if (image.isPresent()) {
                missCounter.increment();

                // cache image without operations and conversion for an optimization
                cache.storeImage(origin, image.get(), filename, Collections.emptyList(), defaultConversion);
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

    private Optional<BufferedImage> fetchRemoteImage(OriginServer origin, String filename, CloseableHttpClient client) {
            // FIXME: this does not look pretty
            try {
                    HttpGet get = new HttpGet(origin.getUrl() + filename);
                    CloseableHttpResponse response = client.execute(get);
                    try{
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();

                    Optional<String> cacheHead = Optional.empty();
                    if (response.getHeaders("Cache-Control") != null) {
                        cacheHead = Optional.of(response.getHeaders("Cache-Control").toString());
                    }
                    Optional<String> eTag = Optional.empty();
                    if (response.getHeaders("ETag") != null) {
                        eTag = Optional.of(response.getHeaders("ETag").toString());
                    }
                    //byte[] imgBytes = conn.getInputStream().readAllBytes();
                    byte[] imgBytes = is.readAllBytes();
                    is.close();
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgBytes));
                    if (image == null) {
                        return Optional.empty();
                    } else {
                        originTrafficSummary.record(imgBytes.length);
                        return Optional.of(image);
                    }
            } catch (Exception e) {
                return Optional.empty();
            } finally {
                response.close();
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
