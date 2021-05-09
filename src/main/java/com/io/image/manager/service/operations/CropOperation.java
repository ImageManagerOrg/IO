package com.io.image.manager.service.operations;

import com.io.image.manager.exceptions.ImageOperationException;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Map;

@Slf4j
public class CropOperation extends ImageOperation {

    public CropOperation(String name, Map<String, String> arguments) {
        super(name, arguments);
    }

    @Override
    public BufferedImage run(BufferedImage image) throws ImageOperationException {

        int startXPoint = Integer.parseInt(getArguments().getOrDefault("x", Integer.toString(0)));
        int startYPoint = Integer.parseInt(getArguments().getOrDefault("y", Integer.toString(0)));

        prepareNewDimensionImage(image.getWidth(), image.getHeight(), startXPoint, startYPoint);
        checkValidationOfArguments(startXPoint, startYPoint, image.getWidth(), image.getHeight());

        return image.getSubimage(startXPoint, startYPoint, newWidth, newHeight);
    }

    public void prepareNewDimensionImage(int imageWidth, int imageHeight, int startXPoint, int startYPoint) {
        newWidth = Integer.parseInt(getArguments().getOrDefault("w", Integer.toString(imageWidth - startXPoint)));
        newHeight = Integer.parseInt(getArguments().getOrDefault("h", Integer.toString(imageHeight - startYPoint)));
    }

    public void checkValidationOfArguments(int startXPoint, int startYPoint, int imageWidth, int imageHeight)
            throws ImageOperationException {

        if (startXPoint < 0 || startYPoint < 0 || startXPoint > imageWidth || startYPoint > imageHeight) {
            throw new ImageOperationException("Origin point is outside of the original image bounds");
        }

        if (startXPoint + newWidth > imageWidth || startYPoint + newHeight > imageHeight) {
            throw new ImageOperationException("Origin point moved by a vector ('w', 'h') is outside of the original image bounds");
        }

        if (newWidth < 0 || newHeight < 0) {
            throw new ImageOperationException("Target width or height is negative");
        }

        if (newWidth > Math.pow(2, 16) || newHeight > Math.pow(2, 16)) {
            throw new ImageOperationException("Target width or height is larger than 2^16");
        }
    }
}
