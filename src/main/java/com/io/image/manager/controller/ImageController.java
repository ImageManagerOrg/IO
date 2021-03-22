package com.io.image.manager.controller;

import com.io.image.manager.service.ImageOperation;
import com.io.image.manager.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // IMPORTANT: make sure that errors are handled properly
    @ResponseBody
    @RequestMapping(value = "/{filename}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String filename) throws IOException, ImageOperation.ImageOperationException {
        List<ImageOperation> operations =  Collections.emptyList();

        var image = imageService.fetchAndCacheImage(filename, operations);
        if (image.isPresent()) {
            return dumpImage(image.get());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private byte[] dumpImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bao);
        return bao.toByteArray();
    }
}
