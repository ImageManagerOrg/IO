package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import com.io.loadbalancer.balancer.ImageManagerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class HashingConfiguration {

    ImageManagerProperties properties;
    MonitorClient monitorClient;

    public HashingConfiguration(ImageManagerProperties properties, MonitorClient monitorClient) {
        this.properties = properties;
        this.monitorClient = monitorClient;
    }

    public List<ImageManager> getImageManagerList() {
        return properties
                .getImageManagers()
                .stream()
                .map(manager -> new ImageManager(manager.getUrl()))
                .collect(Collectors.toList());
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
    public ImageManagerMonitor imageManagerMonitor() {
        return new ImageManagerMonitor(getImageManagerList(), monitorClient);
    }

    @Bean
    public Hashing hashing(){
        return new Hashing(
                getImageManagerList(),
                popularityMonitor(),
                imageManagerMonitor());
    }
}
