package com.io.image.manager.service.operations;

import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;

import java.util.*;

public class ImageOperationParser {

    public static ConversionInfo parseConversion(String filename, String query)throws ConversionException {

        String fileExtension = filename.split("\\.")[1];

        if (query == null) {
            return new ConversionInfo(fileExtension,1);
        }
        else if (query.charAt(0) == 'c' || query.charAt(0) == 'q') {
            int rate = Integer.parseInt(query.split("&")[0].split("=")[1]);
            return new ConversionInfo(fileExtension, rate);
        }
        return new ConversionInfo(fileExtension,1);
    }

    public static List<ImageOperation> parseAndGetOperationList(String query) {

        if (query == null) {
            return Collections.emptyList();
        }

        List<String> params = new ArrayList<>(Arrays.asList(query.split("&")));

        boolean isWatermark = removeWatermarkParamIfExist(params);

        String[] args = params.get(0).split("=");
        if ("p".equals(args[0]) || "q".equals(args[0])) {
            params.remove(0);
        }

        return parse(params, isWatermark);
    }

    private static List<ImageOperation> parse(List<String> params, boolean isWatermark) {

        List<ImageOperation> imageOperations = new ArrayList<>();
        ImageOperation imageOperation = null;

        if (params.size() > 0) {
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
        }
        // This operation must be last because other operations can remove it
        if (isWatermark) {
            imageOperation = createImageOperation("watermark");
            imageOperations.add(imageOperation);
        }
        return imageOperations;
    }

    private static boolean removeWatermarkParamIfExist(List<String> params) {
        boolean isWatermark = false;
        if (params.size() > 0) {
            if (params.get(0).matches("w=true")) {
                isWatermark = true;
                params.remove(0);
            }
            else if (params.get(0).matches("w=false")) {
                params.remove(0);
            }
        }
        return isWatermark;
    }

    public static ImageOperation createImageOperation(String operationName) {

        return switch (operationName) {
            case "crop" -> new CropOperation(operationName, new HashMap<>());
            case "scale" -> new ScaleOperation(operationName, new HashMap<>());
            case "watermark" -> new WatermarkOperation(operationName, new HashMap<>());
            default -> throw new IllegalArgumentException();
        };
    }
}
