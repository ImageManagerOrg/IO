package com.io.loadbalancer.consistent_hashing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopularityMonitor {

    private List<Map<String, Integer>> trackingWindow;
    private int requestsPerWindowTimeRangePerIMInstance; // data from Grafana
    private int windowSize; // number of ranges in the window
    private int timeRangeSize; // length of time range in milliseconds
    private int numIMInstances;
    private long lastWindowCleanUp;

    PopularityMonitor(int requestsPerWindowTimeRangePerIMInstance, int windSize, int timeRangeSize, int numIMInstances) {
        this.requestsPerWindowTimeRangePerIMInstance = requestsPerWindowTimeRangePerIMInstance;
        this.windowSize = windSize;
        this.timeRangeSize = timeRangeSize;
        this.numIMInstances = numIMInstances;
        this.lastWindowCleanUp = System.currentTimeMillis();

        this.trackingWindow = new ArrayList<Map<String, Integer>>();
        for (int i = 0; i < windowSize; i++) {
            trackingWindow.add(new HashMap<String, Integer>());
        }
    }

    public synchronized double getContentPopularity(String contentName) {
        long currentTime = System.currentTimeMillis();
        int currentTimeRange = (int) ((currentTime % (long) (windowSize * timeRangeSize)) / timeRangeSize);

        // Remove obsolete data
        if (currentTime - lastWindowCleanUp >= (long) windowSize * timeRangeSize) {
            for (int i = 0; i < windowSize; i++) {
                trackingWindow.get(i).clear();
            }
            lastWindowCleanUp = currentTime;
        } else if (currentTime - lastWindowCleanUp >= (long) timeRangeSize) {
            int toCleanUp = (int) ((currentTime - lastWindowCleanUp) / timeRangeSize);
            for (int i = 0; i < toCleanUp; i++) {
                int id = (windowSize + currentTimeRange - i) % windowSize;
                trackingWindow.get(id).clear();
            }
            lastWindowCleanUp = currentTime;
        }

        // Get popularity
        int occurrences = 0;
        int requestTypesSum = 0;
        for (int i = 0; i < windowSize; i++) {
            requestTypesSum += trackingWindow.get(i).size();
            occurrences += trackingWindow.get(i).getOrDefault(contentName, 0);
        }

        double requestTypeMean = (double) requestTypesSum / (double) windowSize;
        // Estimate standard traffic per content type
        double maxRequestsPerType;
        if(requestTypeMean < 1){
            maxRequestsPerType = requestsPerWindowTimeRangePerIMInstance;
        } else {
            maxRequestsPerType =requestsPerWindowTimeRangePerIMInstance / requestTypeMean;
        }
        double popularity = occurrences / (numIMInstances * maxRequestsPerType);

        // Put current request in Window
        int count = trackingWindow.get(currentTimeRange).getOrDefault(contentName, 0);
        trackingWindow.get(currentTimeRange).put(contentName, count + 1);

        return popularity;
    }
}