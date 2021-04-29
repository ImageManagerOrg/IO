package com.io.image.manager.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class CacheRecord {
    protected CacheRecord() {}

    public CacheRecord(String origin, int imageId, String nameHash, String etag, Long ttl) {
        this.origin = origin;
        this.imageId = imageId;
        this.nameHash = nameHash;
        this.etag = etag;
        this.ttl = ttl;
        this.hits = 1;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String origin;
    private int imageId;
    private String nameHash;
    private String etag;
    private Long ttl;
    private int hits;
}
