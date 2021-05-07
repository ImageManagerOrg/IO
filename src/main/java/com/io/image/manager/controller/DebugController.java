package com.io.image.manager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @GetMapping("/testHeaderDebug")
    public String testHeaderDebug(
            @RequestHeader("Host") String host,
            @RequestHeader("ROUTE_RULE") String routeRule,
            @RequestHeader("X-Forwarded-For") String xForwardedFor
    ) {

        logger.info(host + " " + routeRule + " " + xForwardedFor);

        return "TEST";
    }
}
