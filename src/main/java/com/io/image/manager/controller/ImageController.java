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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ImageController {
    private final ImageService imageService;
    private final DistributionSummary outboundTrafficSummary;
    private final AppConfigurationProperties props;
    private final String logPath = "/log/IM_log.txt";
    private BufferedWriter writer = null;

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

        // TODO: change origin to a one given in request headers, take it from config for now
        var origin = new OriginServer(props.getOriginServer());

        List<ImageOperation> operations = ImageOperationParser.parse(request.getQueryString());

        Optional<BufferedImage> image;
        image = imageService.fetchAndCacheImage(origin, filename, operations);

        if (image.isPresent()) {
            logRequest(filename, props.getOriginServer(), true, operations);
            byte[] imageArray = dumpImage(image.get());
            outboundTrafficSummary.record(imageArray.length);
            return ResponseEntity.ok(imageArray);
        }
        logRequest(filename, props.getOriginServer(), false, operations);

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private byte[] dumpImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bao);
        return bao.toByteArray();
    }

    private void logRequest(String filename, String origin, boolean isPresent, List<ImageOperation> operations){
        try {
            if (writer == null){
                writer = new BufferedWriter(new FileWriter(logPath, true));
            }
            String opNames = operations.stream().map(ImageOperation::getName).collect(Collectors.joining("|"));
            String opArgs = operations.stream().map(ImageOperation -> ImageOperation
                    .getArguments().toString())
                    .collect(Collectors.joining("|"));
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            String[] values = {timestamp, origin, filename, String.valueOf(isPresent), opNames};

            for (String val: values) {
                writer.append(val);
                writer.append("`");
            }
            writer.append(opArgs);
            writer.append('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            writer = null;
        }

    }
}