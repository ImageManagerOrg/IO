package com.io.image.manager.cache;

import org.springframework.core.io.AbstractResource;

import java.io.IOException;

public interface CacheResult {
    AbstractResource getCacheResource();

    long totalResourceSizeInBytes() throws IOException;

    String resultHash();

    void setTTL(long ttl);

    long getTTL();
}
