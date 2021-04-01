package com.io.image.manager.service.operations;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class ScaleOperation extends ImageOperation {

    public ScaleOperation(String name, Map<String, String> arguments) {
        super(name, arguments);
    }

    @Override
    public BufferedImage run(BufferedImage image) throws ImageOperationException {

        prepareNewDimensionImage(image.getWidth(), image.getHeight());

        Image resultingImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    public void prepareNewDimensionImage(int imageWidth, int imageHeight) {
        String widthValue = getArguments().get("w");
        String heightValue = getArguments().get("h");

        if (widthValue == null) {
            newHeight = Integer.parseInt(heightValue);
            newWidth = imageWidth * newHeight / imageHeight;
        }

        else if (heightValue == null) {
            newWidth = Integer.parseInt(widthValue);
            newHeight = imageHeight * newWidth / imageWidth;
        }
        else {
            newWidth = Integer.parseInt(widthValue);
            newHeight = Integer.parseInt(heightValue);
        }
    }
}
