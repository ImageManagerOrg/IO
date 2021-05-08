package com.io.image.manager.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CacheRecord {
    protected CacheRecord() {}

    public CacheRecord(String origin, String imageFilename, String nameHash, String etag, Long ttl) {
        var imageId = Integer.parseInt(imageFilename.substring(0, imageFilename.lastIndexOf(".")));
        this.origin = origin;
        this.imageId = imageId;
        this.nameHash = nameHash;
        this.etag = etag;
        this.ttl = ttl;
        this.hits = 1;
        this.remoteFetch = LocalDateTime.now();
    }

    public CacheRecord cloneWithNewHash(String nameHash) {
        var newRecord = new CacheRecord();
        newRecord.origin = this.origin;
        newRecord.imageId = this.imageId;
        newRecord.nameHash = nameHash;
        newRecord.etag = this.etag;
        newRecord.ttl = this.ttl;
        newRecord.hits = 1;
        newRecord.remoteFetch = this.remoteFetch;
        return newRecord;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String origin;

    @Getter
    @Setter
    private Integer imageId;

    @Getter
    @Setter
    private String nameHash;

    @Getter
    @Setter
    private String etag;

    @Getter
    @Setter
    private Long ttl;

    @Getter
    @Setter
    private int hits;

    @Getter
    @Setter
    private LocalDateTime remoteFetch;
}
