package com.io.loadbalancer.consistent_hashing;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class MonitorClient {

    public boolean hasActiveOrigins(String host) {
        try {
            RestTemplate restTemplate = getTemplate();
            String url = host + "/connectivityCheck";
            ConnectivityCheckResponse response =
                    restTemplate.getForEntity(url, ConnectivityCheckResponse.class).getBody();
            return Objects.requireNonNull(response).hasActiveOrigins;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private RestTemplate getTemplate() {
        return new RestTemplate();
    }

    @NoArgsConstructor
    private static class ConnectivityCheckResponse {
        public boolean hasActiveOrigins;
    }
}
