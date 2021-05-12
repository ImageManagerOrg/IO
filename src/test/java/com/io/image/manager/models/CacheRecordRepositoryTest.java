package com.io.image.manager.models;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        // given
        var result = repository.save(defaultCacheRecord());

        // when

        // then
        assertNotNull(repository.findById(result.getId()));
    }

    @Test
    public void incrementsCacheRecordHit() {
        // given
        var result = repository.save(defaultCacheRecord());

        // when
        assertEquals(result.getHits(), 1);
        repository.incrementImageHit(result.getOrigin(), result.getNameHash());
        result = repository.findById(result.getId()).get();

        //then
        assertEquals(2, result.getHits());
    }

    @Test
    public void updatedTTL() {
        // given
        var result = repository.save(defaultCacheRecord());

        // when
        repository.updateTTL(result.getOrigin(), result.getNameHash(), 999L);
        var newResult = repository.findById(result.getId()).get();

        // then
        assertEquals(newResult.getTtl(), 999L);
        assert (newResult.getRemoteFetch().isAfter(result.getRemoteFetch()));
    }

}