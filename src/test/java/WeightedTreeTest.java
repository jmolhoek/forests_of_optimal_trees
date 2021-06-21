import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class WeightedTreeTest {

    @Test
    public void testGettersAndSetters() {
        DecisionTree node = new ClassificationNode(1);
        WeightedTree t = new WeightedTree(node, 9.0);

        assertEquals(node, t.getTree());
        assertEquals(9.0, t.getWeight(), 0.00001);
        assertNull(t.getSubspace());

        t.normalizeBy(9.0);

        assertEquals(1.0, t.getWeight(), 0.00001);
        assertEquals(0, t.depth());
        assertEquals(1, t.numberOfNodes());
        assertEquals(0, t.numberOfPredicateNodes());
        assertTrue(t.getConsideredFeatures().isEmpty());
        assertEquals("[Classify to 1]", t.toString());
        assertEquals("[Classify to target]", t.prettyPrint(new ArrayList<>(), "target"));
        assertEquals("[Classify to target]", t.prettyPrint(new ArrayList<>(), "target", 1));
        assertTrue(t.getClassificationNodes().contains(new ClassificationNode(1)));
    }

    @Test
    public void testEquals() {
        DecisionTree node = new ClassificationNode(1);
        WeightedTree t = new WeightedTree(node, 9.0);

        assertEquals(t, t);

        DecisionTree node2 = new ClassificationNode(1);
        WeightedTree t2 = new WeightedTree(node2, 9.0);

        assertEquals(t, t2);
        assertNotEquals(t, node2);

        DecisionTree node3 = new ClassificationNode(1);
        WeightedTree t3 = new WeightedTree(node3, 9.1);

        assertNotEquals(t, t3);

        DecisionTree node4 = new ClassificationNode(0);
        WeightedTree t4 = new WeightedTree(node4, 9.0);

        assertNotEquals(t, t4);

        DecisionTree node5 = new ClassificationNode(1);
        WeightedTree t5 = new WeightedTree(node5, 9.0, new ArrayList<>(Arrays.asList(1,2,3)));

        DecisionTree node6 = new ClassificationNode(1);
        WeightedTree t6 = new WeightedTree(node6, 9.0, new ArrayList<>(Arrays.asList(1,2,3)));

        assertEquals(t5, t6);

        assertNotEquals(t, t5);
        assertNotEquals(t5, t);

    }
}
