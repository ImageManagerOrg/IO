package com.io.image.manager.tasks;

import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.models.CacheRecordRepository;
import com.io.image.manager.origin.OriginServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class PeriodicDiskCacheCleanup {
    AppConfigurationProperties props;
    CacheRecordRepository repository;
    ImageCache cache;

    public PeriodicDiskCacheCleanup(AppConfigurationProperties props, CacheRecordRepository repository, ImageCache cache) {
        this.props = props;
        this.repository = repository;
        this.cache = cache;
    }

    // trigger disk checking task every 3 hours to check for reaching declared disk capacity
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    public void diskCapacityAlertCleanup() {
        var currentUsage = FileUtils.sizeOfDirectory(new File(props.getDiskCacheMountPoint()));

        var currentPercentageUsage = (100.0 * currentUsage) / props.getCacheStorageLimit();

        log.info("Current cache usage level: {}%", currentPercentageUsage);

        if (currentPercentageUsage > props.getCacheStorageLimitAlert()) {
            log.info("Reached disk storage alert level: >{}%", props.getCacheStorageLimitAlert());

            // FIXME: I don't want to spend any more time on thinking how to clear cache so just dump it for now
            var origins = repository.listOrigins();
            origins.forEach(origin -> {
                try {
                    log.info("Deleting all images for origin: {}", origin);
                    cache.purgeOrigin(new OriginServer(String.format("http://%s", origin)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // garbage collect files every 6 hours
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    public void garbageCollection() throws IOException {
        log.info("Starting garbage collection of untracked images...");

        Set<String> cachedPaths = new HashSet<>();
        repository.findAll().forEach(cacheRecord -> {
            cachedPaths.add(String.format("%s/%s/%s", props.getDiskCacheMountPoint(), cacheRecord.getOrigin(), cacheRecord.getImageId().toString()));
        });

        Path cachePath = Paths.get(props.getDiskCacheMountPoint());

        Files
                .walk(cachePath)
                .filter(Files::isDirectory)
                .filter(this::isImageDirectory)
                .forEach(dir -> {
                    if (!cachedPaths.contains(dir.toString())) {
                        try {
                            log.warn("Deleting " + dir.toString() + " as it is no longer tracked");
                            FileUtils.forceDelete(new File(dir.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        log.info("Finished garbage collection of untracked images");
    }

    boolean isImageDirectory(Path dir) {
        // mount point + origin folder
        return dir.getNameCount() > 2;
    }
}
