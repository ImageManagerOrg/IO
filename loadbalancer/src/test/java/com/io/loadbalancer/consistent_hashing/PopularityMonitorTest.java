package com.io.loadbalancer.consistent_hashing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PopularityMonitorTest {
    private static final int REQUESTSPERIMINSTANCE = 10;
    private static final int WINDOWSIZE = 5; // number of ranges in the window
    private static final int TIMERANGESIZE = 1000; // length of time range in milliseconds
    private static final int NUMIMINSTANCES = 2;
    private static final String FILENAME = "filename.jpg";

    @Test
    public void testLowPopularity(){
        // given
        PopularityMonitor popularityMonitor = new PopularityMonitor(
                REQUESTSPERIMINSTANCE,
                WINDOWSIZE,
                TIMERANGESIZE,
                NUMIMINSTANCES
        );

        //when
        for(int i = 0; i < 10; i++){
            popularityMonitor.getContentPopularity(FILENAME);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        double popularity = popularityMonitor.getContentPopularity(FILENAME);

        // then
        assertTrue(popularity < 0.5);
    }

    @Test
    public void testHighPopularity() {
        // given
        PopularityMonitor popularityMonitor = new PopularityMonitor(
                REQUESTSPERIMINSTANCE,
                WINDOWSIZE,
                TIMERANGESIZE,
                NUMIMINSTANCES
        );

        //when
        for(int i = 0; i < 1000; i++){
            popularityMonitor.getContentPopularity(FILENAME);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        double popularity = popularityMonitor.getContentPopularity(FILENAME);

        // then
        assertTrue(popularity > 0.5);
    }

    @Test
    public void testWindowCleaningOnTrafficBreak(){
        // given
        PopularityMonitor popularityMonitor = new PopularityMonitor(
                REQUESTSPERIMINSTANCE,
                WINDOWSIZE,
                TIMERANGESIZE,
                NUMIMINSTANCES
        );

        //when
        for(int i = 0; i < 1000; i++){
            popularityMonitor.getContentPopularity(FILENAME);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(WINDOWSIZE * TIMERANGESIZE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double popularity = popularityMonitor.getContentPopularity(FILENAME);

        // then
        assertTrue(popularity < 0.5);
    }
}
