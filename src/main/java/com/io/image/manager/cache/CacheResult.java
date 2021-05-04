package com.io.image.manager.cache;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;

public interface CacheResult {
     AbstractResource getCacheResource();
     long totalResourceSizeInBytes() throws IOException;
     String resultHash();
}
