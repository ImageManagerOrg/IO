package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.balancer.ImageManager;
import com.io.loadbalancer.exceptions.NoIMInstanceAvailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HashingTest {

    private static final String FILENAME1 = "1234.jpg";
    private static final String FILENAME2 = "123456.jpg";

    @Mock
    private ImageManagerMonitor imageManagerMonitor;

    @Mock
    private PopularityMonitor popularityMonitor;

    @Test
    public void testPopularityHandling() throws NoIMInstanceAvailableException {
        // given
        int IMMax = 5;
        Hashing hashing = new Hashing(
                IntStream
                        .range(0, IMMax)
                        .mapToObj(managerIndex -> new ImageManager("IMURL" + String.valueOf(managerIndex)))
                        .collect(Collectors.toList()),
                popularityMonitor,
                imageManagerMonitor);

        Mockito.when(popularityMonitor.getContentPopularity(FILENAME1))
                .thenReturn(0.51);
        Mockito.when(imageManagerMonitor.getValidInstancesIndexes())
                .thenReturn(IntStream.range(0, IMMax).toArray());

        ArrayList<Integer> counter = new ArrayList<> ();
        for (int i = 0; i < IMMax; i++) counter.add(0);

        // when
        for(int i = 0; i < 1000; i++){
            int hash = hashing.getIMMappingIndex(FILENAME1);
            counter.set(hash, counter.get(hash) + 1);
        }

        // then
        assertEquals(0, counter.get(2));
        assertEquals(0, counter.get(3));
        assertTrue(counter.get(0) > 290);
        assertTrue(counter.get(1) > 290);
        assertTrue(counter.get(4) > 290);
    }

    @Test
    public void testMissingIMHandling() throws NoIMInstanceAvailableException{
        //given
        int IMMax = 5;
        Hashing hashing = new Hashing(
                IntStream
                        .range(0, IMMax)
                        .mapToObj(managerIndex -> new ImageManager("IMURL" + String.valueOf(managerIndex)))
                        .collect(Collectors.toList()),
                popularityMonitor,
                imageManagerMonitor);

        Mockito.when(popularityMonitor.getContentPopularity(FILENAME1))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstancesIndexes())
                .thenReturn(new int[]{0, 1, 2, 3});

        // when
        // Hash of this file given above popularity is 4.
        // Since there is no 4 in the array hash should be 0.
        int hash = hashing.getIMMappingIndex(FILENAME1);

        // then
        assertEquals(0, hash);

        // given
        Mockito.when(popularityMonitor.getContentPopularity(FILENAME2))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstancesIndexes())
                .thenReturn(new int[]{0, 3, 4});
        // when
        // Hash of this file given above popularity is 2.
        // Since there is no 2 in the array hash should be 3.
        hash = hashing.getIMMappingIndex(FILENAME2);

        // then
        assertEquals(3, hash);
    }

    @Test
    public void testGeneralHashing() throws NoIMInstanceAvailableException{
        // given
        int IMMax = 2;
        Hashing hashing = new Hashing(
                IntStream
                        .range(0, IMMax)
                        .mapToObj(managerIndex -> new ImageManager("IMURL" + String.valueOf(managerIndex)))
                        .collect(Collectors.toList()),
                popularityMonitor,
                imageManagerMonitor);

        Mockito.when(popularityMonitor.getContentPopularity(FILENAME1))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstancesIndexes())
                .thenReturn(IntStream.range(0, IMMax).toArray());

        // when
        int hash = hashing.getIMMappingIndex(FILENAME1);

        // then
        assertEquals(1, hash);
    }

    @Test
    public void testNoIMInstances(){
        // given
        int IMMax = 2;
        Hashing hashing = new Hashing(
                IntStream
                        .range(0, IMMax)
                        .mapToObj(managerIndex -> new ImageManager("IMURL" + String.valueOf(managerIndex)))
                        .collect(Collectors.toList()),
                popularityMonitor,
                imageManagerMonitor);


        Mockito.when(popularityMonitor.getContentPopularity(FILENAME1))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstancesIndexes())
                .thenReturn(new int[] {});
        // when

        // then
        assertThrows(NoIMInstanceAvailableException.class, () -> {
            hashing.getIMMappingIndex(FILENAME1);});
    }
}
