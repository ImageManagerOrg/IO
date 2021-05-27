package com.io.loadbalancer.config;

import com.io.loadbalancer.balancer.Balancer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Slf4j
@Configuration
public class RouterConfiguration {

    Balancer balancer;
    MeterRegistry registry;
    Timer timer;

    public RouterConfiguration(Balancer balancer, MeterRegistry registry) {
        this.balancer = balancer;
        this.registry = registry;
        System.out.println(registry);

        timer = Timer.builder("http_server_requests")
                .publishPercentiles(0.5, 0.90, 0.99)
                .register(registry);
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(GET("/{image}"),
                        serverRequest ->
                                processRequest(serverRequest)
                );
    }


    private Mono<ServerResponse> processRequest(org.springframework.web.reactive.function.server.ServerRequest serverRequest) {
        Timer.Sample sample = Timer.start();

        return Mono.zip(
                Mono.justOrEmpty(serverRequest.pathVariable("image")),
                Mono.justOrEmpty(serverRequest)
        )
                .map(params -> balancer.requestImage(params.getT1(), params.getT2()))
                .flatMap(p -> p)
                .doFinally(signalType -> sample.stop(timer));
    }
}

