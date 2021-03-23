package com.io.image.manager.service.operations;

import java.util.*;

public class ImageOperationParser {

    public static List<ImageOperation> parse(String query) {
        List<ImageOperation> imageOperations = new ArrayList<>();

        String[] params = query.split("&");

        ImageOperation imageOperation = null;

        for (String param : params) {
            String[] args = param.split("=");

            if ("op".equals(args[0])) {
                if (imageOperation != null) {
                    imageOperations.add(imageOperation);
                }

                if ("crop".equals(args[1])) {
                    imageOperation = new CropOperation("crop", new HashMap<>());
                }
                else if ("scale".equals(args[1])) {
                    imageOperation = new ScaleOperation("scale", new HashMap<>());
                }
                continue;
            }

            Objects.requireNonNull(imageOperation).addArgument(args[0], args[1]);
        }
        imageOperations.add(imageOperation);

        return imageOperations;
    }
}
