package com.io.image.manager.service.operations;

import com.io.image.manager.exceptions.ImageOperationException;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

@Slf4j
public class ScaleOperation extends ImageOperation {

    public ScaleOperation(String name, Map<String, String> arguments) {
        super(name, arguments);
    }

    @Override
    public BufferedImage run(BufferedImage image) throws ImageOperationException {

        prepareNewDimensionImage(image.getWidth(), image.getHeight());
        checkValidationOfArguments();

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
        } else if (heightValue == null) {
            newWidth = Integer.parseInt(widthValue);
            newHeight = imageHeight * newWidth / imageWidth;
        } else {
            newWidth = Integer.parseInt(widthValue);
            newHeight = Integer.parseInt(heightValue);
        }
    }

    public void checkValidationOfArguments() throws ImageOperationException {

        if (newWidth < 0 || newHeight < 0) {
            throw new ImageOperationException("Target width or height is negative");
        }

        if (newWidth > Math.pow(2, 16) || newHeight > Math.pow(2, 16)) {
            throw new ImageOperationException("Target width or height is larger than 2^16");
        }
    }
}
