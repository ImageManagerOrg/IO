package com.io.image.manager.config;

import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConfigurationProperties(prefix = "image-manager")
@EnableScheduling
public class AppConfigurationProperties {
    private String originServer;
    private String diskCacheMountPoint;
    private String diskLogMountPoint;
    private boolean urlShowMode;
    private boolean logRequests;
    private String routesToLimit;
    private String connectionLimits;
    private long cacheStorageLimit;
    private int cacheStorageLimitAlert;

    public long getCacheStorageLimit() {
        return cacheStorageLimit;
    }

    public int getCacheStorageLimitAlert() {
        return cacheStorageLimitAlert;
    }

    public void setCacheStorageLimit(long cacheStorageLimit) {
        this.cacheStorageLimit = cacheStorageLimit;
    }

    public void setCacheStorageLimitAlert(int cacheStorageLimitAlert) {
        this.cacheStorageLimitAlert = cacheStorageLimitAlert;
    }

    public String getOriginServer() {
        return originServer;
    }

    public void setOriginServer(String originServer) {
        this.originServer = originServer;
    }

    public String getDiskCacheMountPoint() {
        return diskCacheMountPoint;
    }

    public void setDiskCacheMountPoint(String diskCacheMountPoint) {
        this.diskCacheMountPoint = diskCacheMountPoint;
    }

    public String getDiskLogMountPoint() {
        return diskLogMountPoint;
    }

    public void setDiskLogMountPoint(String diskLogMountPoint) {
        this.diskLogMountPoint = diskLogMountPoint;
    }

    public boolean isUrlShowMode() {
        return urlShowMode;
    }

    public void setUrlShowMode(boolean urlShowMode) {
        this.urlShowMode = urlShowMode;
    }

    public String getRoutesToLimit() {
        return routesToLimit;
    }

    public void setRoutesToLimit(String routeToLimit) {
        this.routesToLimit = routeToLimit;
    }

    public String getConnectionLimits() {
        return connectionLimits;
    }

    public void setConnectionLimits(String connectionLimit) {
        this.connectionLimits = connectionLimit;
    }

    public boolean getLogRequests() {
        return logRequests;
    }

    public void setLogRequests(String logRequests) {
        this.logRequests = logRequests.equals("true");
    }

    @Bean
    HttpTraceRepository getHttpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }
}
