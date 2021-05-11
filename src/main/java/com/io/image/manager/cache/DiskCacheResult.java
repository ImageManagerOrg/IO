package com.io.image.manager.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@AllArgsConstructor
public class DiskCacheResult implements CacheResult {
    private Path imageDestination;
    private String resultHash;
    private long ttl;

    @Override
    public AbstractResource getCacheResource() {
        return new FileSystemResource(this.imageDestination);
    }

    @Override
    public long totalResourceSizeInBytes() throws IOException {
        return Files.size(imageDestination);
    }

    @Override
    public String resultHash() {
        return this.resultHash;
    }

    @Override
    public void setTTL(long ttl) {
        this.ttl = ttl;
    }

    @Override
    public long getTTL() {
        return ttl;
    }
}
