package com.io.loadbalancer.consistent_hashing;

import java.util.stream.IntStream;

public class ImageManagerMonitor {

    int IMMax;

    public ImageManagerMonitor(int IMMax) {
        this.IMMax = IMMax;
    }

    // This function should return sorted array of indices of the IM instances that are running correctly
    public int[] getValidInstancesIndexes(){
        return IntStream.range(0, IMMax).toArray();
    }
}
