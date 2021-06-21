import Plot.Coordinate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoordinateTest {

    @Test
    public void testConstructor() {
        Coordinate c = new Coordinate(10.0, 110.6);

        assertEquals(10.0, c.x, 0.0001);
        assertEquals(110.6, c.y, 0.0001);
    }

    @Test
    public void testConstructor2() {
        Coordinate c = new Coordinate(10.0, 110.6, 0.12);

        assertEquals(10.0, c.x, 0.0001);
        assertEquals(110.6, c.y, 0.0001);
        assertEquals(0.12, c.stdev, 0.0001);
    }

    @Test
    public void testCompareTo() {
        Coordinate c1 = new Coordinate(10.0, 110.6);
        Coordinate c2 = new Coordinate(10.1, 110.6);

        assertEquals(-1, c1.compareTo(c2));
    }
}
