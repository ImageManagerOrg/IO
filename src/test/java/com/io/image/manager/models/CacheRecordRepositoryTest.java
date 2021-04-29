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
        return new CacheRecord("origin", 1, "hash", "etag", 1000L);
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

        System.out.println(repository.findByOriginAndNameHash(result.getOrigin(), result.getNameHash()));
        result = repository.findById(result.getId()).get();
        System.out.println(result);

        assertEquals(2, result.getHits());
    }

}