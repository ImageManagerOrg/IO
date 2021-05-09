package com.io.image.manager.service;

import com.io.image.manager.config.AppConfigurationProperties;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService{
    private CloseableHttpClient client;
    private HttpClientConnectionManager connectionManager;

    public ConnectionServiceImpl(AppConfigurationProperties props){
        var poolManager = new PoolingHttpClientConnectionManager();
        poolManager.setMaxTotal(20);
        poolManager.setDefaultMaxPerRoute(5);
        List<String> limits = Arrays.asList(props.getConnectionLimits().split(","));
        List<String> routes = Arrays.asList(props.getRoutesToLimit().split(","));
        for (int i = 0; i < routes.size(); i++) {
            poolManager.setMaxPerRoute(new HttpRoute(HttpHost.create(routes.get(i))), Integer.parseInt(limits.get(i)));
        }
        poolManager.setMaxPerRoute(new HttpRoute(HttpHost.create(props.getRoutesToLimit())), 6);
        this.connectionManager = poolManager;
        this.client = HttpClients.custom().setConnectionManager(this.connectionManager).build();

    }
    public CloseableHttpClient getHttpClient(){
        return client;
    }
}
