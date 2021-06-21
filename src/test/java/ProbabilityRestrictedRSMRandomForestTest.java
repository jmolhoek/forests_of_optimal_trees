import org.javatuples.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ProbabilityRestrictedRSMRandomForestTest {

    @Test
    public void testSampleOne() {
        ArrayList<Pair<Integer, Double>> li = new ArrayList<>(Arrays.asList(
                new Pair<>(1, 1.0),
                new Pair<>(2, 0.0),
                new Pair<>(3, 0.0)
        ));

        int result = SoftRestrictedRSMRandomForest.sampleOne(li);

        assertEquals(1, result);
        assertEquals(2, li.size());

        assertFalse(li.contains(new Pair<>(1, 1.0)));
        assertTrue(li.contains(new Pair<>(2, 0.0)));
        assertTrue(li.contains(new Pair<>(3, 0.0)));
    }

    @Test
    public void testSampleOne2() {
        ArrayList<Pair<Integer, Double>> li = new ArrayList<>(Arrays.asList(
                new Pair<>(1, 0.0),
                new Pair<>(2, 6.0),
                new Pair<>(3, 0.0)
        ));

        int result = SoftRestrictedRSMRandomForest.sampleOne(li);

        assertEquals(2, result);
        assertEquals(2, li.size());

        assertFalse(li.contains(new Pair<>(2, 6.0)));
        assertTrue(li.contains(new Pair<>(1, 0.0)));
        assertTrue(li.contains(new Pair<>(3, 0.0)));
    }
}
