package com.io.loadbalancer.consistent_hashing;

import com.io.loadbalancer.exceptions.NoIMInstanceAvailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HashingTest {

    @Mock
    private ImageManagerMonitor imageManagerMonitor;

    @Mock
    private PopularityMonitor popularityMonitor;

    @Test
    public void testPopularityHandling() throws NoIMInstanceAvailableException {
        int IMMax = 5;
        Hashing hashing = new Hashing(IMMax, popularityMonitor, imageManagerMonitor);
        String fileName = "1234.jpg";

        Mockito.when(popularityMonitor.getContentPopularity(fileName))
                .thenReturn(0.51);
        Mockito.when(imageManagerMonitor.getValidInstances())
                .thenReturn(IntStream.range(0, IMMax).toArray());

        ArrayList<Integer> counter = new ArrayList<> ();
        for (int i = 0; i < IMMax; i++) counter.add(0);

        for(int i = 0; i < 1000; i++){
            int hash = hashing.getIMMapping(fileName);
            counter.set(hash, counter.get(hash) + 1);
        }

        assertEquals(0, counter.get(2));
        assertEquals(0, counter.get(3));
        assertTrue(counter.get(0) > 290);
        assertTrue(counter.get(1) > 290);
        assertTrue(counter.get(4) > 290);
    }

    @Test
    public void testMissingIMHandling() throws NoIMInstanceAvailableException{
        int IMMax = 5;
        Hashing hashing = new Hashing(IMMax, popularityMonitor, imageManagerMonitor);
        String fileName = "1234.jpg";

        Mockito.when(popularityMonitor.getContentPopularity(fileName))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstances())
                .thenReturn(new int[]{0, 1, 2, 3});
        // Hash of this file given above popularity is 4.
        // Since there is no 4 in the array hash should be 0.
        int hash = hashing.getIMMapping(fileName);
        assertEquals(0, hash);

        fileName = "123456.jpg";

        Mockito.when(popularityMonitor.getContentPopularity(fileName))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstances())
                .thenReturn(new int[]{0, 3, 4});
        // Hash of this file given above popularity is 2.
        // Since there is no 2 in the array hash should be 3.
        hash = hashing.getIMMapping(fileName);
        assertEquals(3, hash);
    }

    @Test
    public void testGeneralHashing() throws NoIMInstanceAvailableException{
        int IMMax = 2;
        Hashing hashing = new Hashing(IMMax, popularityMonitor, imageManagerMonitor);
        String fileName = "1234.jpg";

        Mockito.when(popularityMonitor.getContentPopularity(fileName))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstances())
                .thenReturn(IntStream.range(0, IMMax).toArray());

        int hash = hashing.getIMMapping(fileName);
        assertEquals(1, hash);
    }

    @Test
    public void testNoIMInstances(){
        int IMMax = 2;
        Hashing hashing = new Hashing(IMMax, popularityMonitor, imageManagerMonitor);
        String fileName = "1234.jpg";

        Mockito.when(popularityMonitor.getContentPopularity(fileName))
                .thenReturn(0.1);
        Mockito.when(imageManagerMonitor.getValidInstances())
                .thenReturn(new int[] {});

        assertThrows(NoIMInstanceAvailableException.class, () -> {
            hashing.getIMMapping(fileName);});
    }
}
