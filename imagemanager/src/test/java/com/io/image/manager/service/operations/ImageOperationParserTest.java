package com.io.image.manager.service.operations;

import com.io.image.manager.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ImageOperationParserTest {

    private static final String QUERY_EXAMPLE = "op=crop&w=1&h=1&op=scale&w=500&h=500";
    private static final String WRONG_QUERY_EXAMPLE = "op=delete&w=1&h=1";

    @Test
    void parseQueryParamsNull() {
        // given
        String query = null;

        // when
        var actualOperations = ImageOperationParser.parseAndGetOperationList(query);

        // then
        assertEquals(Collections.emptyList(), actualOperations);
    }

    @Test
    void parseQueryParams() {
        // given
        List<ImageOperation> imageOperations = List.of(
                new CropOperation(Utils.CROP_OPERATION_NAME, Map.of("w", "1", "h", "1")),
                new ScaleOperation(Utils.SCALE_OPERATION_NAME, Map.of("w", "500", "h", "500"))
        );

        // when
        var actualOperations = ImageOperationParser.parseAndGetOperationList(QUERY_EXAMPLE);

        // then
        assertEquals(imageOperations.size(), actualOperations.size());
        assertThat(imageOperations).isEqualTo(actualOperations);
    }

    @Test
    void throwExceptionWhenWrongOperationName() {
        // given

        // when then
        assertThrows(IllegalArgumentException.class, () ->
                ImageOperationParser.parseAndGetOperationList(WRONG_QUERY_EXAMPLE));
    }

}
