package com.io.image.manager.origin;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.net.URI;

@Value
@AllArgsConstructor
public class OriginServer {
     String url;

     public String getHost() {
          try {
               return new URI(url).getHost();
          } catch (Exception e) {
               return "";
          }
     }
}
