package com.io.image.manager.service.operations;

import java.util.*;

public class ImageOperationParser {

    public static List<ImageOperation> parse(String query) {
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
            case "crop" -> new CropOperation("crop", new HashMap<>());
            case "scale" -> new ScaleOperation("scale", new HashMap<>());
            default -> null;
        };
    }
}
