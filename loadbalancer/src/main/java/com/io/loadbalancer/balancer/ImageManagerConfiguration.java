package com.io.loadbalancer.balancer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.stream.Collectors;

@Configuration
public class ImageManagerConfiguration {

    @Bean
    public Balancer balancer(ImageManagerProperties properties) {
        return new Balancer(
                properties
                        .getImageManagers()
                        .stream()
                        .map(manager -> new ImageManager(manager.getUrl()))
                        .collect(Collectors.toList()),
                initWebClient()
                );
    }

    public WebClient initWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider
                .builder("connectionProvider")
                .maxConnections(50)
                .pendingAcquireMaxCount(500)
                .pendingAcquireTimeout(Duration.ofSeconds(60)).build();
        HttpClient httpClient = HttpClient.create(connectionProvider);
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build())
                .clientConnector(connector)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "balancer")
    public ImageManagerProperties imageManagerProperties() {
        return new ImageManagerProperties();
    }
}



