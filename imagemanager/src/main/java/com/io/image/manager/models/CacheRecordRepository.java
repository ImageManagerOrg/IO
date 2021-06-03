package com.io.image.manager.models;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CacheRecordRepository extends CrudRepository<CacheRecord, Long> {
    Optional<CacheRecord> findByOriginAndNameHash(String origin, String nameHash);


    @Query("select distinct origin from CacheRecord")
    List<String> listOrigins();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CacheRecord cr set cr.hits = cr.hits + 1 where cr.origin = :origin and cr.nameHash = :nameHash")
    void incrementImageHit(@Param("origin") String origin, @Param("nameHash") String nameHash);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CacheRecord cr set cr.ttl = :ttl, cr.remoteFetch = (strftime('%s', current_timestamp) || substr(strftime('%f', current_timestamp), 4)) where cr.origin = :origin and cr.nameHash = :nameHash")
    void updateTTL(@Param("origin") String origin, @Param("nameHash") String nameHash, @Param("ttl") Long ttl);

    @Transactional
    @Query("select cr.ttl from CacheRecord cr where cr.origin = :origin and cr.nameHash = :nameHash")
    int getTTL(@Param("origin") String origin, @Param("nameHash") String nameHash);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from CacheRecord cr where cr.origin = :origin and cr.imageId = :imageId")
    void deleteImagesForOriginAndId(@Param("origin") String origin, @Param("imageId") Integer imageId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CacheRecord cr set cr.ttl = :ttl, cr.remoteFetch = strftime('%s', current_timestamp) || substr(strftime('%f', current_timestamp),4) where cr.origin = :origin and cr.imageId = :imageId")
    void updateTTLForAllImages(@Param("origin") String origin, @Param("imageId") Integer imageId, @Param("ttl") Long ttl);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from CacheRecord cr where cr.origin = :origin")
    void deleteImagesForOrigin(@Param("origin") String origin);
}
