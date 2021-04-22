package com.io.image.manager.controller;

import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import com.io.image.manager.service.operations.ImageOperationParser;
import com.io.image.manager.service.ImageService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
public class ImageController {
    private final ImageService imageService;
    private final DistributionSummary outboundTrafficSummary;
    private final AppConfigurationProperties props;

    private final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(ImageService imageService, PrometheusMeterRegistry mr, AppConfigurationProperties props) {
        this.imageService = imageService;
        outboundTrafficSummary = DistributionSummary
                .builder("outbound.traffic.size")
                .baseUnit("bytes") // optional
                .register(mr);
        this.props = props;
    }

    @Timed
    @ResponseBody
    @RequestMapping(value = "/{filename}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Object> getImage(@PathVariable String filename, HttpServletRequest request)
            throws IOException, ImageOperationException, ImageNotFoundException {

        if (props.isUrlShowMode()) {
            String url = filename;
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }
            logger.info(url);
        }

        // TODO: change origin to a one given in request headers, take it from config for now
        var origin = new OriginServer(props.getOriginServer());

        List<ImageOperation> operations = ImageOperationParser.parse(request.getQueryString());

        Optional<BufferedImage> image;
        image = imageService.fetchAndCacheImage(origin, filename, operations);

        if (image.isPresent()) {
            byte[] imageArray = dumpImage(image.get());
            outboundTrafficSummary.record(imageArray.length);
            return ResponseEntity.ok(imageArray);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private byte[] dumpImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bao);
        return bao.toByteArray();
    }
}