package com.io.image.manager.cache;

import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ImageCache {
    Optional<BufferedImage> loadImage(OriginServer origin, String filename, List<ImageOperation> operations) throws IOException;
    void storeImage(OriginServer origin, BufferedImage image, String filename, List<ImageOperation> operations) throws IOException;

    void purgeOrigin(OriginServer origin) throws IOException;
    void purgeImage(OriginServer origin, String filename) throws IOException;
}
