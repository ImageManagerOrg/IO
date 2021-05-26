package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import com.io.loadbalancer.balancer.ImageManagerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

@Configuration
public class HashingConfiguration {

    ImageManagerProperties properties;

    public HashingConfiguration(ImageManagerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Hashing hashing(){
        int IMMax = properties.getImageManagers().size();
        return new Hashing(
                properties
                        .getImageManagers()
                        .stream()
                        .map(manager -> new ImageManager(manager.getUrl()))
                        .collect(Collectors.toList()),
                new PopularityMonitor(), // Not sure if it should be initialized here
                new ImageManagerMonitor(IMMax)); // Not sure if it should be initialized here
    }
}
