package com.io.image.manager.service;

import java.awt.image.BufferedImage;
import java.util.Map;

public abstract class ImageOperation {
    private final String name;
    private final Map<String, String> arguments;

    static class ImageOperationException extends Exception {}

    public ImageOperation(String name, Map<String, String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public abstract BufferedImage run(BufferedImage image) throws ImageOperationException;
}
