package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class ImageManagerMonitor {

    private final MonitorClient monitorClient;

    private final List<ImageManager> imageManagers;
    private int[] IMRunningIndexes;

    public ImageManagerMonitor(List<ImageManager> imageManagers, MonitorClient monitorClient) {
        this.imageManagers = imageManagers;
        this.monitorClient = monitorClient;
        IMRunningIndexes = IntStream.range(0, imageManagers.size()).toArray();
    }

    // trigger check connection to all IM instances every 5 seconds
    @Scheduled(fixedDelay = 1000 * 5)
    public void checkIMRunningIndexed() {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i < imageManagers.size(); i++) {
            boolean imageManagerStatus = monitorClient.getStatus(imageManagers.get(i).getUrl());
            if (imageManagerStatus) {
                indexes.add(i);
            }
        }
        this.IMRunningIndexes = indexes.stream().mapToInt(i->i).toArray();
    }

    // This function should return sorted array of indices of the IM instances that are running correctly
    public int[] getValidInstancesIndexes(){
        return this.IMRunningIndexes;
    }
}
