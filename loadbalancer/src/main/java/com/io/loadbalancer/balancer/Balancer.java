package com.io.loadbalancer.balancer;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

public class Balancer {
    List<ImageManager> imageManagers;
    WebClient client;

    public Balancer(List<ImageManager> imageManagers, WebClient client) {
        this.imageManagers = imageManagers;
        this.client = client;
    }

    public Mono<ServerResponse> requestImage(String image, ServerRequest request) {
        URI uri = null;

        try {
            Random r = new Random();
            var manager = imageManagers.get(r.nextInt(imageManagers.size()));
            var managerUri = new URI(manager.getUrl());

            uri = UriComponentsBuilder
                    .fromUri(request.uri())
                    .host(managerUri.getHost())
                    .port(managerUri.getPort())
                    .build()
                    .toUri();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return client
                .get()
                .uri(uri)
                .headers(headers -> headers.addAll(request.headers().asHttpHeaders()))
                .accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
                .exchangeToMono(response ->
                        response
                                .bodyToMono(DataBuffer.class)
                                .flatMap(body ->
                                        ServerResponse
                                                .status(response.statusCode())
                                                .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
                                                .bodyValue(body)
                                ));
    }
}

