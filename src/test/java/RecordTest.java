import static org.junit.Assert.*;

import Data.Record;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class RecordTest {
    private final Boolean[] bools = {true,false,true};
    private final ArrayList<Boolean> boolsList = new ArrayList<>(Arrays.asList(bools));

    private final Boolean[] bools2 = {true,false,false};
    private final ArrayList<Boolean> boolsList2 = new ArrayList<>(Arrays.asList(bools2));

    @Test
    public void testConstructors() {
        Record r1 = new Record(boolsList);
        Record r2 = new Record(boolsList, 0);

        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals(3, r1.getNumberOfFeatures());
        assertEquals(3, r2.getNumberOfFeatures());
    }

    @Test
    public void testGetActualClass() {
        Record r1 = new Record(boolsList);
        Record r2 = new Record(boolsList, 0);

        assertEquals(-1, r1.getActualClass());
        assertEquals(0, r2.getActualClass());
    }

    @Test
    public void testPredicates() {
        Record r1 = new Record(boolsList);

        assertEquals(true, r1.checkPredicate(0));
        assertEquals(false, r1.checkPredicate(1));
        assertEquals(true, r1.checkPredicate(2));
    }

    @Test
    public void testToString() {
        Record r1 = new Record(boolsList);

        assertEquals("Data.Record{features=[true, false, true], actualClass=-1}", r1.toString());
    }

    @Test
    public void testEquals() {
        Record r1 = new Record(boolsList);
        Record r2 = new Record(boolsList, 0);

        assertEquals(r1, r1);
        assertNotEquals(r1, (Object)"hey");
        assertNotEquals(r1, r2);
    }

    @Test
    public void testEquals2() {
        Record r1 = new Record(boolsList);
        Record r2 = new Record(boolsList, 0);
        Record r3 = new Record(boolsList2);

        assertEquals(r1, r1);
        assertNotEquals(r1, (Object)"hey");
        assertNotEquals(r1, r2);
        assertNotEquals(r1, r3);
    }

    @Test
    public void testDefaultWeightIsOne() {
        Record r1 = new Record(boolsList);
        Record r2 = new Record(boolsList, 0);

        assertEquals(1.0, r1.getWeight(), 0.0001);
        assertEquals(1.0, r2.getWeight(), 0.0001);
    }

    @Test
    public void testGetSetWeight() {
        Record r1 = new Record(boolsList);
        Record r2 = new Record(boolsList, 0);

        r1.setWeight(100.0);
        r2.setWeight(0.0);

        assertEquals(100.0, r1.getWeight(), 0.00001);
        assertEquals(0.0, r2.getWeight(), 0.00001);
    }

    @Test
    public void testMapToSubspace() {
        Record r1 = new Record(boolsList, 1);

        r1.setWeight(100.0);

        Record r1New = r1.mapToSubSpace(new ArrayList<>(Arrays.asList(1,2)));

        assertFalse(r1New.checkPredicate(0));
        assertTrue(r1New.checkPredicate(1));

        assertEquals(100.0, r1New.getWeight(), 0.00001);
        assertEquals(1, r1New.getActualClass());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMapToSubspaceOutOfBounds() {
        Record r1 = new Record(boolsList, 1);

        r1.setWeight(100.0);

        Record r1New = r1.mapToSubSpace(new ArrayList<>(Arrays.asList(1,2)));

        r1New.checkPredicate(2);
    }
}
