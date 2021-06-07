package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class ImageManagerMonitor {

    private final MonitorClient monitorClient;

    private final List<ImageManager> imageManagers;
    private int[] onlineManagersIndexes;

    public ImageManagerMonitor(List<ImageManager> imageManagers, MonitorClient monitorClient) {
        this.imageManagers = imageManagers;
        this.monitorClient = monitorClient;
        onlineManagersIndexes = IntStream.range(0, imageManagers.size()).toArray();
    }

    // trigger check connection to all IM instances every 5 seconds
    @Scheduled(fixedDelay = 1000 * 5)
    public void checkIMRunningIndexed() {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i < imageManagers.size(); i++) {
            boolean hasActiveOrigins = monitorClient.hasActiveOrigins(imageManagers.get(i).getUrl());
            if (hasActiveOrigins) {
                indexes.add(i);
            }
        }
        this.onlineManagersIndexes = indexes.stream().mapToInt(i->i).toArray();
    }

    // This function should return sorted array of indices of the IM instances that are running correctly
    public int[] getValidInstancesIndexes(){
        return Arrays.copyOf(this.onlineManagersIndexes, this.onlineManagersIndexes.length);
    }
}
