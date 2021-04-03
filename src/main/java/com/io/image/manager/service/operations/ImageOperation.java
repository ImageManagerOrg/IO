package com.io.image.manager.service.operations;

import com.io.image.manager.exceptions.ImageOperationException;

import java.awt.image.BufferedImage;
import java.util.Map;

public abstract class ImageOperation {
    private final String name;
    private final Map<String, String> arguments;
    protected int newWidth;
    protected int newHeight;

    public String getName() {
        return name;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public ImageOperation(String name, Map<String, String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public void addArgument(String key, String value) {
        arguments.put(key, value);
    }

    public abstract BufferedImage run(BufferedImage image) throws ImageOperationException;
}
