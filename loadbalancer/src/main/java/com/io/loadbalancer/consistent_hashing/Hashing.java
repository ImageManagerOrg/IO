package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.exceptions.NoIMInstanceAvailableException;

import java.util.Arrays;
import java.util.Random;

public class Hashing {

    int IMMax;
    PopularityMonitor popularityMonitor;
    ImageManagerMonitor imageManagerMonitor;
    Random random = new Random();

    public Hashing(int IMMax, PopularityMonitor popularityMonitor, ImageManagerMonitor imageManagerMonitor) {
        this.IMMax = IMMax;
        this.popularityMonitor = popularityMonitor;
        this.imageManagerMonitor = imageManagerMonitor;
    }

    private int getCongruence(int number, int modulus) {
        return (number % modulus + modulus) % modulus;
    }

    public int getIMMapping(String fileName) throws NoIMInstanceAvailableException {
        // This function takes only filename as argument - no operations

        int fileNameHashMod = getCongruence(fileName.hashCode(), IMMax);

        double popularity = popularityMonitor.getContentPopularity(fileName);
        int rangeIMInstances = (int) Math.ceil(popularity * IMMax);
        int hashGivenPopularity = (fileNameHashMod + random.nextInt(rangeIMInstances)) % IMMax;

        int validIMInstances [] = imageManagerMonitor.getValidInstances();
        if (validIMInstances.length == 0){
            throw new NoIMInstanceAvailableException();
        }

        int position = Arrays.binarySearch(validIMInstances, hashGivenPopularity);
        position = position >=0 ? position : -1 * (position + 1);

        return validIMInstances[position % validIMInstances.length];

    }

}
