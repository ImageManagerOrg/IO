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
        assertEquals(expectedWidth, logoAghImageProcessed.getWidth());
        assertEquals(expectedHeight, logoAghImageProcessed.getHeight());
    }
}
