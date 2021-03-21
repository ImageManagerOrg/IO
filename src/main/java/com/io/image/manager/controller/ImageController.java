package com.io.image.manager.controller;

import com.io.image.manager.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // TODO: parse query params to image operations and apply them to requested image
    // IMPORTANT: make sure that errors are handled properly
    @ResponseBody
    @RequestMapping(value = "/{filename}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String filename) throws IOException {

        // TODO: do the whole image processing
        // 1. Try to fetch image from cache (stored locally) that has parsed operations already applied
        // 2. If not found then fetch remote image
        // 3. If image has been found apply the processing operations
        // 4. If everything went ok save image to cache
        // 5. Serve processed image as a response
        BufferedImage image = imageService.fetchRemoteImage(filename).orElseThrow();

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bao);
        return bao.toByteArray();
    }
}
