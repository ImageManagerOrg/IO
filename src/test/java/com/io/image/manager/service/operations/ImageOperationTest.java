package com.io.image.manager.service.operations;

import com.io.image.manager.Utils;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ImageOperationTest {

    @Autowired
    ImageService imageService;

    @Test
    void multiExecuteOperationTest() throws IOException, ImageOperationException {
        // given
        BufferedImage logoAghImage = Utils.loadTestImage();

        List<ImageOperation> imageOperations = List.of(
                new CropOperation(Utils.CROP_OPERATION_NAME, Map.of("w", "1", "h", "1")),
                new ScaleOperation(Utils.SCALE_OPERATION_NAME, Map.of("w", "100", "h", "700")),
                new ScaleOperation(Utils.SCALE_OPERATION_NAME, Map.of("h", "250")),
                new CropOperation(Utils.CROP_OPERATION_NAME, Map.of("x", "1"))
        );

        // when
        for (ImageOperation imageOperation : imageOperations) {
            logoAghImage = imageOperation.run(logoAghImage);
        }
        int expectedHeight = 250;
        int expectedWidth = 34;

        // then
        assertEquals(expectedHeight, logoAghImage.getHeight());
        assertEquals(expectedWidth, logoAghImage.getWidth());
    }

}