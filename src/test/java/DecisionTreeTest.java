import Data.Record;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DecisionTreeTest {

    @Test
    public void testClassificationNode() {
        DecisionTree node = new ClassificationNode(0);
        assertEquals(0, ((ClassificationNode) node).getAssignClass());
    }

    @Test
    public void testGetSetAssignClass() {
        ClassificationNode node = new ClassificationNode(0);
        assertEquals(0, node.getAssignClass());

        node.setAssignClass(10);
        assertEquals(10, node.getAssignClass());
    }

    @Test
    public void testPredicateNode() {
        PredicateNode tree = new PredicateNode(1, new ClassificationNode(0), new ClassificationNode(1));

        assertEquals(tree.getPredicate(), 1);
        assertEquals(((ClassificationNode) tree.getLeftTree()).getAssignClass(), 0);
        assertEquals(((ClassificationNode) tree.getRightTree()).getAssignClass(), 1);
    }

    @Test
    public void testGetSetLeftRightTree() {
        PredicateNode tree = new PredicateNode(1, new ClassificationNode(0), new ClassificationNode(1));

        assertEquals(tree.getPredicate(), 1);
        assertEquals(((ClassificationNode) tree.getLeftTree()).getAssignClass(), 0);
        assertEquals(((ClassificationNode) tree.getRightTree()).getAssignClass(), 1);

        tree.setPredicate(10);
        tree.setLeftTree(new ClassificationNode(11));
        tree.setRightTree(new ClassificationNode(12));

        assertEquals(tree.getPredicate(), 10);
        assertEquals(((ClassificationNode) tree.getLeftTree()).getAssignClass(), 11);
        assertEquals(((ClassificationNode) tree.getRightTree()).getAssignClass(), 12);
    }

    @Test
    public void testClassify_standard() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, predicateZero, predicateOne);

        Boolean[] instance1 = {true,false,true};
        Record record1 = new Record(new ArrayList<Boolean>(Arrays.asList(instance1)));
        int result1 = tree.classify(record1);

        Boolean[] instance2 = {true,true,true};
        Record record2 = new Record(new ArrayList<Boolean>(Arrays.asList(instance2)));
        int result2 = tree.classify(record2);

        Boolean[] instance3 = {true,true,false};
        Record record3 = new Record(new ArrayList<Boolean>(Arrays.asList(instance3)));
        int result3 = tree.classify(record3);

        Boolean[] instance4 = {false,true,false};
        Record record4 = new Record(new ArrayList<Boolean>(Arrays.asList(instance4)));
        int result4 = tree.classify(record4);

        assertEquals(0, result1);
        assertEquals(1, result2);

        assertEquals(1, result3);
        assertEquals(0, result4);
    }

    @Test
    public void testClassify_dynamic_tree_update() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, predicateZero, predicateOne);

        Boolean[] instance1 = {true,false,true};
        Record record1 = new Record(new ArrayList<Boolean>(Arrays.asList(instance1)));
        int result1 = tree.classify(record1);

        Boolean[] instance2 = {false,false,true};
        Record record2 = new Record(new ArrayList<Boolean>(Arrays.asList(instance2)));
        int result2 = tree.classify(record2);

        assertEquals(0, result1);
        assertEquals(0, result2);

        predicateOne.setRightTree(predicateZero);
        predicateOne.setLeftTree(predicateZero);

        int result3 = tree.classify(record1);
        int result4 = tree.classify(record2);

        assertEquals(1, result3);
        assertEquals(0, result4);
    }

    @Test
    public void testDeptAndNodes() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, predicateZero, predicateOne);

        assertEquals(2, tree.depth());
        assertEquals(3, tree.numberOfPredicateNodes());
        assertEquals(7, tree.numberOfNodes());
    }

    @Test
    public void testDeptAndNodes_imbalanced() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, classifyZero, predicateOne);

        assertEquals(2, tree.depth());
        assertEquals(2, tree.numberOfPredicateNodes());
        assertEquals(5, tree.numberOfNodes());
    }

    @Test
    public void testToString() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, predicateZero, predicateOne);

        assertEquals("[If not feature 2 then [If not feature 0 then [Classify to 0] else [Classify to 1]] else [If not feature 1 then [Classify to 0] else [Classify to 1]]]"
                , tree.toString());
    }

    @Test
    public void testPrettyPrint_simple() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, predicateZero, predicateOne);

        assertEquals("[Consider: isTall]" +
                        "\n--------if false: [Consider: hasMoney]" +
                        "\n----------------if false: [Classify to NOT isAttractive]" +
                        "\n----------------if true: [Classify to isAttractive]" +
                        "\n--------if true: [Consider: isHandsome]" +
                        "\n----------------if false: [Classify to NOT isAttractive]" +
                        "\n----------------if true: [Classify to isAttractive]"
                , tree.prettyPrint(new ArrayList<>(Arrays.asList("hasMoney", "isHandsome" ,"isTall")), "isAttractive"));
    }

    @Test
    public void testPrettyPrint_extraIndent() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(2, predicateZero, predicateOne);

        assertEquals("[Consider: isTall]" +
                        "\n------------------------if false: [Consider: hasMoney]" +
                        "\n--------------------------------if false: [Classify to NOT isAttractive]" +
                        "\n--------------------------------if true: [Classify to isAttractive]" +
                        "\n------------------------if true: [Consider: isHandsome]" +
                        "\n--------------------------------if false: [Classify to NOT isAttractive]" +
                        "\n--------------------------------if true: [Classify to isAttractive]"
                , tree.prettyPrint(new ArrayList<>(Arrays.asList("hasMoney", "isHandsome" ,"isTall")), "isAttractive", 2));
    }

    @Test
    public void testPrettyPrint_classificationNode() {
        ClassificationNode classifyOne = new ClassificationNode(1);

        assertEquals("[Classify to isAttractive]"
                , classifyOne.prettyPrint(new ArrayList<>(Arrays.asList("hasMoney", "isHandsome" ,"isTall")), "isAttractive"));
        assertEquals("[Classify to isAttractive]"
                , classifyOne.prettyPrint(new ArrayList<>(Arrays.asList("hasMoney", "isHandsome" ,"isTall")), "isAttractive", 10));
    }

    @Test
    public void testGetConsideredFeatures_leaf() {
        ClassificationNode classifyOne = new ClassificationNode(1);

        assertEquals(0, classifyOne.getConsideredFeatures().size());
    }

    @Test
    public void testGetConsideredFeatures_tree() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);
        PredicateNode predicateZero = new PredicateNode(0, classifyZero, classifyOne);
        PredicateNode predicateOne = new PredicateNode(1, classifyZero, classifyOne);
        DecisionTree tree = new PredicateNode(6, predicateZero, predicateOne);

        assertEquals(3, tree.getConsideredFeatures().size());
        assertTrue(tree.getConsideredFeatures().contains(0));
        assertTrue(tree.getConsideredFeatures().contains(1));
        assertTrue(tree.getConsideredFeatures().contains(6));
    }

    @Test
    public void testAnd_Equal() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                10,
                new PredicateNode(11, classifyOne, classifyZero),
                classifyZero
        );

        PredicateNode tree2 = new PredicateNode(
                11,
                new PredicateNode(10, classifyOne, classifyZero),
                classifyZero
        );

        assertTrue(DecisionTree.isEqual(tree1, tree2));
    }

    @Test
    public void testAnd_NotEqual() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                10,
                new PredicateNode(11, classifyOne, classifyZero),
                classifyZero
        );

        PredicateNode tree2 = new PredicateNode(
                11,
                new PredicateNode(10, classifyZero, classifyOne),
                classifyZero
        );

        assertFalse(DecisionTree.isEqual(tree1, tree2));
    }

    @Test
    public void testBAndNotA_Equal() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                10,
                new PredicateNode(11, classifyZero, classifyOne),
                classifyZero
        );

        PredicateNode tree2 = new PredicateNode(
                11,
                classifyZero,
                new PredicateNode(10, classifyOne, classifyZero)
        );

        assertTrue(DecisionTree.isEqual(tree1, tree2));
    }

    @Test
    public void testBAndNotA_NotEqual() {
        ClassificationNode classifyOne = new ClassificationNode(1);
        ClassificationNode classifyZero = new ClassificationNode(0);

        PredicateNode tree1 = new PredicateNode(
                10,
                new PredicateNode(11, classifyZero, classifyOne),
                classifyZero
        );

        PredicateNode tree2 = new PredicateNode(
                11,
                classifyOne,
                new PredicateNode(10, classifyOne, classifyZero)
        );

        assertFalse(DecisionTree.isEqual(tree1, tree2));
    }

    @Test
    public void testEqual_BiggerTrees() {
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
    }
}
