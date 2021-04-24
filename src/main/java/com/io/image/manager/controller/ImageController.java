package com.io.image.manager.controller;

import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;
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

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
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

    /**
     * The following URL format applies:
     * /[OBJECT_ID].[FORMAT]?[FORMAT_OPTIONS]&[WATERMARK_OPTION]&[OP_LIST]
     * <p>
     * Required: [OBJECT_ID], [FORMAT]
     * Not required: [FORMAT_OPTIONS], [WATERMARK_OPTION], [OP_LIST]
     * <p>
     * In required header named "Host" is ip for origin
     */

    @Timed
    @ResponseBody
    @RequestMapping(
            value = "/{filename}",
            method = RequestMethod.GET,
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE}
    )
    public ResponseEntity<Object> getImage(
            @RequestHeader("Host") String host,
            @PathVariable String filename,
            HttpServletRequest request
    ) throws IOException, ImageOperationException, ImageNotFoundException, ConversionException {

        if (props.isUrlShowMode()) {
            String url = String.format("https://%s/%s", host, filename);
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }
            logger.info(url);
        }
        var origin = originFromHost(host);

        List<ImageOperation> operations = ImageOperationParser.parseAndGetOperationList(request.getQueryString());
        ConversionInfo conversionInfo = ImageOperationParser.parseConversion(filename, request.getQueryString());

        String normalized_filename = filename.substring(0, filename.indexOf(".")) + ".jpg";
        Optional<BufferedImage> image = imageService.fetchAndCacheImage(origin, normalized_filename, operations);

        if (image.isPresent()) {
            byte[] imageArray = imageService.dumpImage(image.get(), conversionInfo);
            outboundTrafficSummary.record(imageArray.length);
            return ResponseEntity.ok(imageArray);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private OriginServer originFromHost(String host) {
        return new OriginServer(String.format("https://%s/", host));
    }
}