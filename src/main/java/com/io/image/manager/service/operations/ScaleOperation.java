package com.io.image.manager.service.operations;

import java.awt.image.BufferedImage;
import java.util.Map;

public class ScaleOperation extends ImageOperation {

    public ScaleOperation(String name, Map<String, String> arguments) {
        super(name, arguments);
    }

    @Override
    public BufferedImage run(BufferedImage image) throws ImageOperationException {
        //TODO remember to handle errors properly (throw ImageOperationException when arg is wrong)
        return image;
    }
}
