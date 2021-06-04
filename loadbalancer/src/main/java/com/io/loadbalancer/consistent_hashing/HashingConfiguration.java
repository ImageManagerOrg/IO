package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import com.io.loadbalancer.balancer.ImageManagerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.stream.Collectors;

@Configuration
public class HashingConfiguration {

    ImageManagerProperties properties;

    public HashingConfiguration(ImageManagerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Scope("singleton")
    public PopularityMonitor popularityMonitor(){
        int IMMax = properties.getImageManagers().size();
        return new PopularityMonitor(
                70000,
                60,
                60000,
                IMMax);
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
                popularityMonitor(),
                new ImageManagerMonitor(IMMax)); // Not sure if it should be initialized here
    }
}
