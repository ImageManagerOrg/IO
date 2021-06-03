package com.io.image.manager.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(
        name="cache_record",
        uniqueConstraints=
        @UniqueConstraint(columnNames={"origin", "nameHash"})
)
public class CacheRecord {
    protected CacheRecord() {
    }

    public CacheRecord(String origin, String imageFilename, String nameHash, Long sizeInBytes, String etag, Long ttl) {
        var imageId = Integer.parseInt(imageFilename.substring(0, imageFilename.lastIndexOf(".")));
        this.origin = origin;
        this.imageId = imageId;
        this.nameHash = nameHash;
        this.sizeInBytes = sizeInBytes;
        this.etag = etag;
        this.ttl = ttl;
        this.hits = 1;
        this.remoteFetch = LocalDateTime.now();
    }

    public CacheRecord cloneWithNewHash(String nameHash, Long sizeInBytes) {
        var newRecord = new CacheRecord();
        newRecord.origin = this.origin;
        newRecord.imageId = this.imageId;
        newRecord.nameHash = nameHash;
        newRecord.sizeInBytes = sizeInBytes;
        newRecord.etag = this.etag;
        newRecord.ttl = this.ttl;
        newRecord.hits = 1;
        newRecord.remoteFetch = this.remoteFetch;
        return newRecord;
    }

    public boolean isTTLValid() {
        return (Duration.between(remoteFetch, LocalDateTime.now()).getSeconds() < ttl);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private Long sizeInBytes;

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
