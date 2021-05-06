package com.io.image.manager.controller;

import com.io.image.manager.cache.CacheResult;
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
import org.springframework.core.io.AbstractResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ImageController {
    private final ImageService imageService;
    private final DistributionSummary outboundTrafficSummary;
    private final AppConfigurationProperties props;
    private final boolean logRequests;
    private BufferedWriter writer;

    private final Logger logger = LoggerFactory.getLogger(ImageController.class);

    public ImageController(ImageService imageService, PrometheusMeterRegistry mr, AppConfigurationProperties props) throws IOException {
        this.imageService = imageService;
        outboundTrafficSummary = DistributionSummary
                .builder("outbound.traffic.size")
                .baseUnit("bytes") // optional
                .register(mr);
        this.props = props;
        this.logRequests = props.getLogRequests();

        if (this.logRequests) {
            String logDir = props.getDiskLogMountPoint();
            Path logPath = Paths.get(logDir);
            if (!Files.exists(logPath)) {
                Files.createDirectory(logPath);
            }
            writer = new BufferedWriter(new FileWriter(logDir + "/IM_log.txt", true));
        }
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
    public ResponseEntity<AbstractResource> getImage(
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
//        var origin = originFromHost(host);
        var origin = new OriginServer(props.getOriginServer());

        List<ImageOperation> operations = ImageOperationParser.parseAndGetOperationList(request.getQueryString());
        ConversionInfo conversionInfo = ImageOperationParser.parseConversion(filename, request.getQueryString());

        // ==============
        if (logRequests) logRequest(filename, props.getOriginServer(), "null", operations);
        // ==============

        String normalizedFilename = filename.substring(0, filename.indexOf(".")) + ".jpg";

        CacheResult cacheResult;
        try {
            cacheResult = imageService.fetchAndCacheImage(origin, normalizedFilename, operations, conversionInfo);
        } catch (ImageNotFoundException e) {
            // ==============
            if(logRequests) logRequest(filename, props.getOriginServer(), "false", operations);
            // ==============
            throw e;
        }

        // ==============
        if(logRequests) logRequest(filename, props.getOriginServer(), "true", operations);
        // ==============

        outboundTrafficSummary.record(cacheResult.totalResourceSizeInBytes());

        return ResponseEntity.ok(cacheResult.getCacheResource());
    }

    private OriginServer originFromHost(String host) {
        return new OriginServer(String.format("https://%s/", host));
    }

    private synchronized void logRequest(String filename, String origin, String isPresent, List<ImageOperation> operations){
        try {
            String opNames = operations.stream().map(ImageOperation::getName).collect(Collectors.joining("|"));
            String opArgs = operations.stream().map(ImageOperation -> ImageOperation
                    .getArguments().toString())
                    .collect(Collectors.joining("|"));
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            String[] values = {timestamp, origin, filename, isPresent, opNames};

            for (String val: values) {
                writer.append(val);
                writer.append("`");
            }
            writer.append(opArgs);
            writer.append('\n');
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}