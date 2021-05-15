package com.io.loadbalancer.balancer;

import lombok.Data;

import java.util.List;

@Data
public class ImageManagerProperties {

    private List<ImageManagerDestination> imageManagers;

    @Data
    public static class ImageManagerDestination {
        private String url;
    }
}

