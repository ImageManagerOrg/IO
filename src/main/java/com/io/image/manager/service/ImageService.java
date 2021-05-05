package com.io.image.manager.service;

import  org.apache.http.conn.HttpClientConnectionManager;
import com.io.image.manager.cache.CacheResult;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.exceptions.ConversionException;
import com.io.image.manager.exceptions.ImageNotFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.List;


public interface ImageService {
    CacheResult fetchAndCacheImage(OriginServer server, String filename, List<ImageOperation> operations, ConversionInfo info, CloseableHttpClient client)
            throws IOException, ImageOperationException, ImageNotFoundException, ConversionException;
}
