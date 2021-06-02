package com.io.image.manager.controller;

import com.io.image.manager.cache.ImageCache;
import com.io.image.manager.data.DeleteCacheRequest;
import com.io.image.manager.models.CacheRecordRepository;
import com.io.image.manager.origin.OriginServer;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/*
    NOTE: This controller will probably do nothing for now as images in ORIGIN are not yet labeled with integers as their IDs
 */
@RestController
public class CacheController {
    ImageCache cache;
    CacheRecordRepository repository;

    public CacheController(ImageCache cache, CacheRecordRepository repository) {
        this.cache = cache;
        this.repository = repository;
    }

    @DeleteMapping(value = "/api/cache")
    public void purgeCache(@RequestHeader("Host") String host, @RequestBody DeleteCacheRequest request) throws IOException {
        var origin = originFromHost(host);
        if (request.getKeys() != null) {
            request.getKeys().forEach((key) -> {
                purgeImageByKey(origin, key);
            });
        }
        if (request.getKeyRanges() != null) {
            request.getKeyRanges().forEach((range) -> {
                for (int i = range.getFrom(); i <= range.getTo(); i++) {
                    purgeImageByKey(origin, i);
                }
            });
        }
    }

    @DeleteMapping(value = "/api/cache/all")
    public void purgeCache(@RequestHeader("Host") String host) throws IOException {
        this.repository.deleteImagesForOrigin(host);
        this.cache.purgeOrigin(originFromHost(host));
    }

    private OriginServer originFromHost(String host) {
        return new OriginServer(String.format("https://%s/", host));
    }

    private void purgeImageByKey(OriginServer origin, int id) {
        try {
            this.repository.deleteImagesForOriginAndId(origin.getHost(), id);
            this.cache.purgeImage(origin, String.valueOf(id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
