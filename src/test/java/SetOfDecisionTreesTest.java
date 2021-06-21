import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SetOfDecisionTreesTest {

    @Test
    public void testSet_BiggerTrees() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                1,
                new PredicateNode(
                        2,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        2,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        PredicateNode tree2 = new PredicateNode(
                2,
                new PredicateNode(
                        1,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        1,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        PredicateNode tree3 = new PredicateNode(
                3,
                new PredicateNode(
                        2,
                        new PredicateNode(1, classifyZero, classifyOne),
                        new PredicateNode(1, classifyOne, classifyZero)
                ),
                classifyOne
        );

        PredicateNode tree4 = new PredicateNode(
                1,
                new PredicateNode(
                        3,
                        new PredicateNode(2, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        2,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );


        assertTrue(DecisionTree.isEqual(tree1, tree2));
        assertTrue(DecisionTree.isEqual(tree1, tree3));
        assertTrue(DecisionTree.isEqual(tree2, tree3));
        assertTrue(DecisionTree.isEqual(tree2, tree4));

        SetOfDecisionTrees set = new SetOfDecisionTrees();

        set.add(tree1);
        set.add(tree2);
        set.add(tree3);
        set.add(tree4);

        assertEquals(1, set.size());

        assertTrue(set.contains(tree1));
        assertTrue(set.contains(tree2));
        assertTrue(set.contains(tree3));
        assertTrue(set.contains(tree4));
    }

    @Test
    public void testSet_BiggerTrees_oneUnequal() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                1,
                new PredicateNode(
                        2,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        2,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        PredicateNode tree2 = new PredicateNode(
                2,
                new PredicateNode(
                        1,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        1,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        PredicateNode tree3 = new PredicateNode(
                3,
                new PredicateNode(
                        2,
                        new PredicateNode(1, classifyZero, classifyOne),
                        new PredicateNode(1, classifyOne, classifyZero)
                ),
                classifyOne
        );

        PredicateNode tree4 = new PredicateNode(
                1,
                new PredicateNode(
                        3,
                        new PredicateNode(2, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        5,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );


        assertTrue(DecisionTree.isEqual(tree1, tree2));
        assertTrue(DecisionTree.isEqual(tree1, tree3));
        assertTrue(DecisionTree.isEqual(tree2, tree3));

        SetOfDecisionTrees set = new SetOfDecisionTrees();

        set.add(tree1);
        set.add(tree2);
        set.add(tree3);
        set.add(tree4);

        assertEquals(2, set.size());

        assertTrue(set.contains(tree1));
        assertTrue(set.contains(tree2));
        assertTrue(set.contains(tree3));
        assertTrue(set.contains(tree4));
    }

    @Test
    public void testSet_ManySmallMethods() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                1,
                new PredicateNode(
                        2,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        2,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        SetOfDecisionTrees set = new SetOfDecisionTrees();

        set.add(tree1);

        assertEquals(set.iterator().next(), tree1);
        assertEquals(set.getItems().get(0), tree1);
        assertEquals(set.toArray()[0], tree1);
        assertFalse(set.isEmpty());

        // the first alrady is in the set, the second is not
        set.addAll(Arrays.asList(
                new PredicateNode(
                        3,
                        new PredicateNode(
                                2,
                                new PredicateNode(1, classifyZero, classifyOne),
                                new PredicateNode(1, classifyOne, classifyZero)
                        ),
                        classifyOne
                ),
                new PredicateNode(
                1,
                new PredicateNode(
                        3,
                        new PredicateNode(2, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        5,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
                )
        ));

        assertEquals(2, set.size());

        set.removeAll(Arrays.asList(
                new PredicateNode(
                        3,
                        new PredicateNode(
                                2,
                                new PredicateNode(1, classifyZero, classifyOne),
                                new PredicateNode(1, classifyOne, classifyZero)
                        ),
                        classifyOne
                ),
                new PredicateNode(
                        1,
                        new PredicateNode(
                                3,
                                new PredicateNode(2, classifyZero, classifyOne),
                                classifyOne
                        ),
                        new PredicateNode(
                                5,
                                classifyOne,
                                new PredicateNode(3, classifyZero, classifyOne)
                        )
                )
        ));

        assertEquals(0, set.size());

    }

    @Test
    public void testSet_ManySmallMethods2() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                1,
                new PredicateNode(
                        2,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        2,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        SetOfDecisionTrees set = new SetOfDecisionTrees();

        set.add(tree1);

        assertFalse(set.containsAll(Arrays.asList(tree1, classifyOne)));

        PredicateNode tree2 = new PredicateNode(
                2,
                new PredicateNode(
                        1,
                        new PredicateNode(3, classifyZero, classifyOne),
                        classifyOne
                ),
                new PredicateNode(
                        1,
                        classifyOne,
                        new PredicateNode(3, classifyZero, classifyOne)
                )
        );

        assertTrue(set.containsAll(Arrays.asList(tree1, tree2)));

        set.clear();

        assertTrue(set.isEmpty());

        assertFalse(set.removeAll(Arrays.asList(tree1, tree2)));
    }
}
