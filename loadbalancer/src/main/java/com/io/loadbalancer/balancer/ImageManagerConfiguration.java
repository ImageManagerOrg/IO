package com.io.loadbalancer.balancer;

import com.io.loadbalancer.consistent_hashing.Hashing;
import com.io.loadbalancer.consistent_hashing.HashingConfiguration;
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

    ImageManagerProperties properties;
    Hashing hashing;

    public ImageManagerConfiguration(ImageManagerProperties properties, Hashing hashing) {
        this.properties = properties;
        this.hashing = hashing;
    }

    @Bean
    public Balancer balancer() {
        return new Balancer(
                properties
                        .getImageManagers()
                        .stream()
                        .map(manager -> new ImageManager(manager.getUrl()))
                        .collect(Collectors.toList()),
                initWebClient(),
                hashing
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
}



