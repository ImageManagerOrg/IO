package com.io.image.manager.service;

import com.io.image.manager.origin.OriginServer;

public interface ConnectivityService {
    public boolean isConnectivityEstablished();
    public void registerRequest(OriginServer host);
    public void registerFail(OriginServer host);
}
