package com.io.image.manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {
    @Value("${image-manager.origin-server}")
    private String originServer;

    @Override
    public Optional<BufferedImage> fetchRemoteImage(String filename) {
        try {
            // FIXME: this does not look pretty
            URL url = new URL(originServer + filename);
            BufferedImage image = ImageIO.read(url.openStream());
            if (image == null) {
                return Optional.empty();
            } else {
                return Optional.of(image);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<BufferedImage> fetchLocalImage(String filename, List<ImageOperation> operations) {
        return Optional.empty();
    }

    @Override
    public BufferedImage applyOperations(BufferedImage image, List<ImageOperation> operations) throws ImageOperation.ImageOperationException {
        for (var operation: operations) {
            image = operation.run(image);
        }
        return image;
    }
}
