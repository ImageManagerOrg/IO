package com.io.image.manager.service.operations;

import com.io.image.manager.exceptions.ImageOperationException;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

@Slf4j
public class WatermarkOperation extends ImageOperation {


    public WatermarkOperation(String name, Map<String, String> arguments) {
        super(name, arguments);
    }

    public BufferedImage run(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        graphics.setFont(new Font("Arial", Font.BOLD, 30));
        String watermark = "AGH Copyright @2021";
        graphics.drawString(watermark, 0, 100);
        graphics.dispose();
        return image;
    }
}
