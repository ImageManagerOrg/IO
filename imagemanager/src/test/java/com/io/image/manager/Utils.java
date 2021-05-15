package com.io.image.manager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class Utils {

    private static final String IMAGE_NAME = "logo_AGH.jpg";
    public static final String CROP_OPERATION_NAME = "crop";
    public static final String SCALE_OPERATION_NAME = "scale";
    public static final String WATERMARK_OPERATION_NAME = "watermark";

    public static BufferedImage loadTestImage() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(IMAGE_NAME);
        return ImageIO.read(Objects.requireNonNull(url));
    }
}
