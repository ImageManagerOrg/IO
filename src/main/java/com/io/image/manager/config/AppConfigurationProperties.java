package com.io.image.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image-manager")
public class AppConfigurationProperties {
    private String originServer;
    private String diskCacheMountPoint;

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

}
