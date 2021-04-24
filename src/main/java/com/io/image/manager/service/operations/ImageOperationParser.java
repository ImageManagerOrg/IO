package com.io.image.manager.service.operations;

import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;

import java.util.*;

public class ImageOperationParser {

    public static ConversionInfo parseConversion(String query)throws ConversionException {
        //TODO: get conversion paramters from query

        return new ConversionInfo("png",0);
    }

    public static List<ImageOperation> parse(String query) {

        if (query == null) {
            return Collections.emptyList();
        }

        List<ImageOperation> imageOperations = new ArrayList<>();

        String[] params = query.split("&");

        ImageOperation imageOperation = null;

        for (String param : params) {
            String[] args = param.split("=");
            String key = args[0];
            String value = args[1];

            if ("op".equals(key)) {
                if (imageOperation != null) {
                    imageOperations.add(imageOperation);
                }

                imageOperation = createImageOperation(value);
                continue;
            }

            Objects.requireNonNull(imageOperation).addArgument(key, value);
        }
        imageOperations.add(imageOperation);

        return imageOperations;
    }

    public static ImageOperation createImageOperation(String operationName) {

        return switch (operationName) {
            case "crop" -> new CropOperation(operationName, new HashMap<>());
            case "scale" -> new ScaleOperation(operationName, new HashMap<>());
            default -> throw new IllegalArgumentException();
        };
    }
}
