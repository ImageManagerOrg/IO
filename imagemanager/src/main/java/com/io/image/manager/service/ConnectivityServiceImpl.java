package com.io.image.manager.service;

import java.util.HashMap;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URL;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Service;
import com.io.image.manager.origin.OriginServer;


@Service
public class ConnectivityServiceImpl implements ConnectivityService {

    private final ConnectionService connectionService;
    private HashMap<OriginServer, Integer> allRequests = new HashMap<>();
    private HashMap<OriginServer, Integer> failRequests = new HashMap<>();
    private HashMap<OriginServer, Integer> allRequestsOld = new HashMap<>();
    private HashMap<OriginServer, Integer> failRequestsOld = new HashMap<>();
    private long lastTimestamp = System.currentTimeMillis();
    private boolean connectivityEstablished = true;

    public ConnectivityServiceImpl(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public synchronized void registerRequest(OriginServer host) {
        long now = System.currentTimeMillis();
        if (now - lastTimestamp > 60000) {
            allRequestsOld = allRequests;
            failRequestsOld = failRequests;
            allRequests = new HashMap<>();
            failRequests = new HashMap<>();
            lastTimestamp = now;
        }
        if (!allRequests.containsKey(host))
            allRequests.put(host, 0);
        allRequests.put(host, allRequests.get(host) + 1);
    }

    ;

    public synchronized void registerFail(OriginServer host) {
        if (!failRequests.containsKey(host))
            failRequests.put(host, 0);
        failRequests.put(host, failRequests.get(host) + 1);
    }

    ;

    public boolean isConnectivityEstablished() {
        if (!connectivityEstablished) {
            return false;
        }
        boolean connectivity = true;
        OriginServer failedHost = null;
        for (OriginServer host : allRequestsOld.keySet()) {
            if (failRequestsOld.containsKey(host) && failRequests.get(host) == allRequests.get(host)) {
                connectivity = false;
                failedHost = host;
            }
        }
        final OriginServer toReconnect = failedHost;
        Runnable r2 = () -> {
            boolean flag = true;
            while (flag) {
                try {
                    Thread.sleep(5000);
                    URLConnection connection = new URL(toReconnect.getUrl()).openConnection();
                    connection.connect();
                    flag = false;
                    connectivityEstablished = true;
                } catch (Exception e) {
                    continue;
                }
            }

        };
        if (!connectivity) {

            connectivityEstablished = false;
            Thread worker = new Thread(r2);
            worker.start();

        }

        return connectivity;
    }

    ;


}
