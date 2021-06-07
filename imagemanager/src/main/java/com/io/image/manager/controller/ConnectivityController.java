package com.io.image.manager.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.io.image.manager.service.ConnectivityService;

@RestController
public class ConnectivityController {
    private ConnectivityService connectivityService;

    public ConnectivityController(ConnectivityService connectivityService){
        this.connectivityService = connectivityService;
    }

    @RequestMapping(
            value = "/connectivityCheck",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ConnectivityCheckResponse connectivityCheck(){
        return new ConnectivityCheckResponse(connectivityService.isConnectivityEstablished());
    }

    public static class ConnectivityCheckResponse {
        public boolean status;

        public ConnectivityCheckResponse(boolean status) {
            this.status = status;
        }
    }
}
