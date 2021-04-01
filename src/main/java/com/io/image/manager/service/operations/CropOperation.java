package com.io.image.manager.service.operations;

import java.awt.image.BufferedImage;
import java.util.Map;

public class CropOperation extends ImageOperation {

    public CropOperation(String name, Map<String, String> arguments) {
        super(name, arguments);
    }

    @Override
    public BufferedImage run(BufferedImage image) throws ImageOperationException {

        int startXPoint = Integer.parseInt(getArguments().getOrDefault("x", Integer.toString(0)));
        int startYPoint = Integer.parseInt(getArguments().getOrDefault("y", Integer.toString(0)));

        prepareNewDimensionImage(image.getWidth(), image.getHeight(), startXPoint, startYPoint);

        return image.getSubimage(startXPoint, startYPoint, newWidth, newHeight);
    }

    public void prepareNewDimensionImage(int imageWidth, int imageHeight, int startXPoint, int startYPoint) {
        newWidth = Integer.parseInt(getArguments().getOrDefault("w", Integer.toString(imageWidth - startXPoint)));
        newHeight = Integer.parseInt(getArguments().getOrDefault("h", Integer.toString(imageHeight - startYPoint)));
    }
}
