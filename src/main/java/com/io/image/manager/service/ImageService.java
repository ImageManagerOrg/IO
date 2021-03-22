package com.io.image.manager.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface ImageService {
    Optional<BufferedImage> fetchRemoteImage(String filename);
    Optional<BufferedImage> fetchLocalImage(String filename, List<ImageOperation> operations);
    void storeImage(BufferedImage image, String filename, List<ImageOperation> operations) throws IOException;
    BufferedImage applyOperations(BufferedImage image, List<ImageOperation> operations) throws ImageOperation.ImageOperationException;
}
