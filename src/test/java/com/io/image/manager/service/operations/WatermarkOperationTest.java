package com.io.image.manager.service.operations;

import com.io.image.manager.Utils;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WatermarkOperationTest {
    @Autowired
    ImageService imageService;

    boolean areImagesEqualSizesButDifferentPixelvise(BufferedImage buf1,BufferedImage buf2){
        int width  = buf1.getWidth();
        int height = buf1.getHeight();

        if (buf1.getWidth() != buf2.getWidth() || buf1.getHeight() != buf2.getHeight()) {
            return false;
        }
        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (buf1.getRGB(x, y) != buf2.getRGB(x, y)) {
                    return true;
                }
            }
        }

        return false;
    }
    };

    @Test
    void watermarkExecuteOperationTest() throws IOException, ImageOperationException {
        // given
        BufferedImage logoAghImagePrimary = Utils.loadTestImage();
        WatermarkOperation watermarkOperation =
                new WatermarkOperation(Utils.WATERMARK_OPERATION_NAME, Map.of());

        // when
        int expectedHeight = logoAghImagePrimary.getHeight();
        int expectedWidth = logoAghImagePrimary.getWidth();
        var logoAghImageProcessed = watermarkOperation.run(logoAghImagePrimary);

        // then
        assertTrue(areImagesEqualSizesButDifferentPixelvise(logoAghImageProcessed,logoAghImagePrimary));
    }
}
