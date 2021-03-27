package com.io.image.manager.controller;

import com.io.image.manager.service.operations.ImageOperation;
import com.io.image.manager.service.operations.ImageOperationParser;
import com.io.image.manager.service.ImageService;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
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

    public ImageController(ImageService imageService, PrometheusMeterRegistry mr) {
        this.imageService = imageService;
        outboundTrafficSummary = DistributionSummary
                .builder("response.size")
                .baseUnit("bytes") // optional
                .register(mr);
    }


    @ResponseBody
    @RequestMapping(value = "/{filename}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Object> getImage(@PathVariable String filename, HttpServletRequest request) throws IOException {

//        outboundTrafficSummary.record(request.getContentLength()); # todo: correctly record size of the request
        outboundTrafficSummary.record(100); //dummy value

        List<ImageOperation> operations = ImageOperationParser.parse(request.getQueryString());

        Optional<BufferedImage> image;
        try {
            image = imageService.fetchAndCacheImage(filename, operations);
        } catch (ImageOperation.ImageOperationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (image.isPresent()) {
            return ResponseEntity.ok(dumpImage(image.get()));
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private byte[] dumpImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bao);
        return bao.toByteArray();
    }
}
