package com.io.image.manager.service;

import org.apache.http.impl.client.CloseableHttpClient;

public interface ConnectionService {
   public CloseableHttpClient getHttpClient();
}
