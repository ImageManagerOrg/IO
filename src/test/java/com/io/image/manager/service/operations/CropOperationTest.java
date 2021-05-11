package com.io.image.manager.service.operations;

import com.io.image.manager.Utils;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CropOperationTest {

    @Autowired
    ImageService imageService;

    @Test
    void cropExecuteOperationTest() throws IOException, ImageOperationException {
        // given
        BufferedImage logoAghImagePrimary = Utils.loadTestImage();
        CropOperation cropOperation =
                new CropOperation(Utils.CROP_OPERATION_NAME, Map.of("w", "1", "h", "1"));

        // when
        var logoAghImageProcessed = cropOperation.run(logoAghImagePrimary);
        int expectedValue = 1;

        // then
        assertEquals(expectedValue, logoAghImageProcessed.getWidth());
        assertEquals(expectedValue, logoAghImageProcessed.getHeight());
    }

    @Test
    void throwImageOperationExceptionWhenWrongOperationParams() throws IOException {
        // given
        BufferedImage logoAghImagePrimary = Utils.loadTestImage();
        CropOperation cropOperation =
                new CropOperation(Utils.CROP_OPERATION_NAME, Map.of("w", "-100"));

        // when then
        assertThrows(ImageOperationException.class, () -> cropOperation.run(logoAghImagePrimary));
    }

}