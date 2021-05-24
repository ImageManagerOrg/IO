package com.io.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan({"com.io.loadbalancer"})
public class LoadbalancerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadbalancerApplication.class, args);
    }

}
