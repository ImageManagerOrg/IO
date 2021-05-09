package com.io.image.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image-manager")
public class AppConfigurationProperties {
    private String originServer;
    private String diskCacheMountPoint;
    private String diskLogMountPoint;
    private boolean urlShowMode;
    private String routesToLimit;
    private String connectionLimits;
    private boolean logRequests;

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

    public String getDiskLogMountPoint() { return diskLogMountPoint; }

    public void setDiskLogMountPoint(String diskLogMountPoint) { this.diskLogMountPoint = diskLogMountPoint; }

    public boolean isUrlShowMode() {
        return urlShowMode;
    }

    public void setUrlShowMode(boolean urlShowMode) {
        this.urlShowMode = urlShowMode;
    }

    public String getRoutesToLimit() { return routesToLimit; }

    public void setRoutesToLimit(String routeToLimit) { this.routesToLimit = routeToLimit; }

    public String getConnectionLimits() { return connectionLimits; }

    public void setConnectionLimits(String connectionLimit) { this.connectionLimits = connectionLimit; }

    public boolean getLogRequests() {return logRequests; }

    public void setLogRequests(String logRequests) {
        this.logRequests = logRequests.equals("true");
    }
}
