package com.io.image.manager.models;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CacheRecordRepositoryTest {
    @Autowired
    private CacheRecordRepository repository;

    private CacheRecord defaultCacheRecord() {
        return new CacheRecord("origin", "1.jpg", "hash", 1000000L, "etag", 1000L);
    }

    @Test
    public void insertsCacheRecord() {
        var result = repository.save(defaultCacheRecord());
        assertNotNull(repository.findById(result.getId()));
    }

    @Test
    public void incrementsCacheRecordHit() {
        var result = repository.save(defaultCacheRecord());
        assertEquals(result.getHits(), 1);

        repository.incrementImageHit(result.getOrigin(), result.getNameHash());

        result = repository.findById(result.getId()).get();

        assertEquals(2, result.getHits());
    }

    @Test
    public void updatedTTL() {
        var result = repository.save(defaultCacheRecord());
        assertEquals(result.getTtl(), 1000L);

        repository.updateTTL(result.getOrigin(), result.getNameHash(), 999L);
        var newResult = repository.findById(result.getId()).get();

        assertEquals(newResult.getTtl(), 999L);
        assert(newResult.getRemoteFetch().isAfter(result.getRemoteFetch()));
    }

}