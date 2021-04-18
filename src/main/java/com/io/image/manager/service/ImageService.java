package com.io.image.manager.service;

import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface ImageService {
    Optional<BufferedImage> fetchAndCacheImage(OriginServer server, String filename, List<ImageOperation> operations)
            throws IOException, ImageOperationException;
}
