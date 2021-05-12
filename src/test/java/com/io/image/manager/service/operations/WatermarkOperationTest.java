package com.io.image.manager.service.operations;

import com.io.image.manager.Utils;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WatermarkOperationTest {
    boolean areImagesEqualSizesButDifferentPixelvise(BufferedImage buf1, BufferedImage buf2) {
        int width = buf1.getWidth();
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
    };

    @Test
    void watermarkExecuteOperationTest() throws IOException, ImageOperationException {
        // given
        BufferedImage logoAghImagePrimary = Utils.loadTestImage();
        BufferedImage logoAghImageControl = Utils.loadTestImage();
        WatermarkOperation watermarkOperation =
                new WatermarkOperation(Utils.WATERMARK_OPERATION_NAME, Map.of());

        // when
        var logoAghImageProcessed = watermarkOperation.run(logoAghImagePrimary);

        // then
        assertTrue(areImagesEqualSizesButDifferentPixelvise(logoAghImageProcessed, logoAghImageControl));
    }
}
