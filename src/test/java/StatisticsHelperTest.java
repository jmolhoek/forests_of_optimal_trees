import org.javatuples.Pair;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class StatisticsHelperTest {

    @Test
    public void testMeanAndStd() {
        Pair<Double, Double> meanAndStdev = StatisticsHelper.meanAndStdOf(new LinkedList<>(Arrays.asList(
                10.0,
                12.0,
                23.0,
                23.0,
                16.0,
                23.0,
                21.0,
                16.0
        )));

        assertEquals(18.0, meanAndStdev.getValue0(), 0.0001);
        assertEquals(5.237229, meanAndStdev.getValue1(), 0.0001);
    }


    @Test
    public void testRemoveOutliers_odd() {
        ArrayList<Double> data = new ArrayList<>(Arrays.asList(
                100.0,
                101.0,
                102.0,
                99.0,
                500.0,
                501.0,
                102.0,
                97.0,
                95.0,
                105.0,
                10.0
        ));

        StatisticsHelper.removeOutliersSmart(data);

        assertEquals(11 - 3, data.size());
        assertFalse(data.contains(500.0));
        assertFalse(data.contains(501.0));
        assertFalse(data.contains(10.0));
        assertTrue(data.contains(102.0));
    }

    @Test
    public void testRemoveOutliers_even() {
        ArrayList<Double> data = new ArrayList<>(Arrays.asList(
                100.0,
                101.0,
                99.0,
                500.0,
                501.0,
                102.0,
                97.0,
                95.0,
                105.0,
                10.0
        ));

        StatisticsHelper.removeOutliersSmart(data);

        assertEquals(10 - 3, data.size());
        assertFalse(data.contains(500.0));
        assertFalse(data.contains(501.0));
        assertFalse(data.contains(10.0));
        assertTrue(data.contains(102.0));
    }

    @Test
    public void testMedian_oneElement() {
        ArrayList<Double> data = new ArrayList<>(Collections.singletonList(
                100.0
        ));

        double res = StatisticsHelper.getMedianOfSortedList(data);

        assertEquals(100.0, res, 0.00001);
    }

    @Test
    public void testMedian_threeElements() {
        ArrayList<Double> data = new ArrayList<>(Arrays.asList(
                1.0,
                2.0,
                3.0
        ));

        double res = StatisticsHelper.getMedianOfSortedList(data);

        assertEquals(2.0, res, 0.00001);
    }

    @Test
    public void testMedian_fourElements() {
        ArrayList<Double> data = new ArrayList<>(Arrays.asList(
                1.0,
                2.0,
                3.0,
                4.0
        ));

        double res = StatisticsHelper.getMedianOfSortedList(data);

        assertEquals(2.5, res, 0.00001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMedian_Exception1() {
        ArrayList<Double> data = new ArrayList<>();

        StatisticsHelper.getMedianOfSortedList(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMedian_Exception2() {
        StatisticsHelper.getMedianOfSortedList(null);
    }


}
