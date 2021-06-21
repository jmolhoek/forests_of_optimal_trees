import Plot.Coordinate;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ExperimentTest {
    @Test
    public void testHorizontalLineAtHeight() {
        List<Coordinate> l = Experiment.horizontalLineAtHeight(0.02);

        assertEquals(2, l.size());
        assertEquals(0.02, l.get(0).y, 0.0001);
        assertEquals(0.02, l.get(1).y, 0.0001);
    }

    @Test
    public void testRound_0() {
        double d = 1.234567;

        double res = Experiment.round(d, 0);

        assertEquals(1.0, res, 0.0001);
    }

    @Test
    public void testRound_1() {
        double d = 1.234567;

        double res = Experiment.round(d, 1);

        assertEquals(1.2, res, 0.0001);
    }

    @Test
    public void testRound_2() {
        double d = 1.234567;

        double res = Experiment.round(d, 2);

        assertEquals(1.23, res, 0.0001);
    }

    @Test
    public void testRound_3() {
        double d = 1.234567;

        double res = Experiment.round(d, 3);

        assertEquals(1.235, res, 0.0001);
    }
}
