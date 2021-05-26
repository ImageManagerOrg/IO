package com.io.loadbalancer.config;

import com.io.loadbalancer.balancer.Balancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import io.micrometer.core.annotation.Timed;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Slf4j
@Configuration
public class RouterConfiguration {

    Balancer balancer;

    public RouterConfiguration(Balancer balancer) {
        this.balancer = balancer;
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(GET("/{image}"),
                        serverRequest ->
                                processRequest(serverRequest)
                );
    }

    @Timed
    private Mono<ServerResponse> processRequest(org.springframework.web.reactive.function.server.ServerRequest serverRequest) {
        return Mono.zip(
                Mono.justOrEmpty(serverRequest.pathVariable("image")),
                Mono.justOrEmpty(serverRequest)
        )
                .map(params -> balancer.requestImage(params.getT1(), params.getT2()))
                .flatMap(p -> p);
    }
}

