package com.io.loadbalancer.balancer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ConstructorBinding
@ConfigurationProperties(prefix = "balancer")
public class ImageManagerProperties {
    Logger logger = LoggerFactory.getLogger(ImageManagerProperties.class);

    @Getter
    private final List<ImageManagerDestination> imageManagers;

    public ImageManagerProperties(String imageManagerUrls) {
        this.imageManagers = Arrays.stream(imageManagerUrls.split(",")).map(ImageManagerDestination::new).collect(Collectors.toList());

        logger.info("Parsed {} image managers from properties: {}", this.imageManagers.size(), imageManagerUrls);
    }

    @Data
    @AllArgsConstructor
    public static class ImageManagerDestination {
        private String url;
    }
}

