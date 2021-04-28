package com.io.image.manager.service;

import com.io.image.manager.cache.CacheResult;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;

import java.io.IOException;
import java.util.List;


public interface ImageService {
    CacheResult fetchAndCacheImage(OriginServer server, String filename, List<ImageOperation> operations, ConversionInfo info)
            throws IOException, ImageOperationException, ImageNotFoundException;
}
