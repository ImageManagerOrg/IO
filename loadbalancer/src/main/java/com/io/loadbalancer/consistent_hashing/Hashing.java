package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import com.io.loadbalancer.exceptions.NoIMInstanceAvailableException;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;

public class Hashing {

    List<ImageManager> imageManagers;
    int IMMax;
    PopularityMonitor popularityMonitor;
    ImageManagerMonitor imageManagerMonitor;
    Random random = new Random();
    double epsilon = 0.000001;

    public Hashing(List<ImageManager> imageManagers,
                   PopularityMonitor popularityMonitor,
                   ImageManagerMonitor imageManagerMonitor) {
        this.imageManagers = imageManagers;
        this.IMMax = imageManagers.size();
        this.popularityMonitor = popularityMonitor;
        this.imageManagerMonitor = imageManagerMonitor;
    }

    private int getCongruence(int number, int modulus) {
        return (number % modulus + modulus) % modulus;
    }

    // This function takes only filename as argument - no operations
    public int getIMMappingIndex(String fileName) throws NoIMInstanceAvailableException {

        int fileNameHashMod = getCongruence(fileName.hashCode(), IMMax);

        double popularity = popularityMonitor.getContentPopularity(fileName);
        int rangeIMInstances = min((int) Math.ceil((popularity + epsilon) * IMMax), IMMax);
        int hashGivenPopularity = (fileNameHashMod + random.nextInt(rangeIMInstances)) % IMMax;

        int validIMInstances [] = imageManagerMonitor.getValidInstancesIndexes();
        if (validIMInstances.length == 0){
            throw new NoIMInstanceAvailableException();
        }

        int position = Arrays.binarySearch(validIMInstances, hashGivenPopularity);
        position = position >= 0 ? position : -1 * (position + 1);

        return validIMInstances[position % validIMInstances.length];

    }

    public ImageManager getIMMapping(String fileName) throws NoIMInstanceAvailableException {
        return imageManagers.get(getIMMappingIndex(fileName));
    }

}
