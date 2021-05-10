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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScaleOperationTest {
    @Test
    void scaleExecuteOperationTest() throws IOException, ImageOperationException {
        // given
        BufferedImage logoAghImagePrimary = Utils.loadTestImage();

        ScaleOperation scaleOperation =
                new ScaleOperation("scale", Map.of("w", "750", "h", "100"));

        // when
        var logoAghImageProcessed = scaleOperation.run(logoAghImagePrimary);
        int expectedWidth = 750;
        int expectedHeight = 100;

        // then
        assertEquals(expectedWidth, logoAghImageProcessed.getWidth());
        assertEquals(expectedHeight, logoAghImageProcessed.getHeight());
    }

    @Test
    void throwImageOperationExceptionWhenWrongOperationParams() throws IOException {
        // given
        BufferedImage logoAghImagePrimary = Utils.loadTestImage();
        ScaleOperation scaleOperation = new ScaleOperation("scale", Map.of("w", "200000"));

        // when then
        assertThrows(ImageOperationException.class, () -> scaleOperation.run(logoAghImagePrimary));
    }

}