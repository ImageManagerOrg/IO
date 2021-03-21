package com.io.image.manager.service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;


public interface ImageService {
    Optional<BufferedImage> fetchRemoteImage(String filename);
    Optional<BufferedImage> fetchLocalImage(String filename, List<ImageOperation> operations);
    BufferedImage applyOperations(BufferedImage image, List<ImageOperation> operations) throws ImageOperation.ImageOperationException;
}
