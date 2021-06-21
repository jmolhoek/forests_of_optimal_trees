import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import Data.Record;
import org.javatuples.Pair;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class AllMurTreeBuilderTest {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

    @Test
    public void testTicTacToeMurTree_DepthOne() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new AllMurTreeBuilder().withDepth(1).build(data);

        assertEquals(0.30062630480167013, c.error(data), 0.00000001);
    }

    @Test
    public void testMurTreeBuilder() {
        AllMurTreeBuilder b = new AllMurTreeBuilder().withDepth(1);

        assertEquals(1, b.getDepth());
    }

    @Test
    public void testMurTreeBuilderInfeasible() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        Classifier c = new AllMurTreeBuilder().withDepth(-1).build(data);

        assertNull(c);
    }

    @Test
    public void testTicTacToeMurTree_DepthTwo() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new AllMurTreeBuilder().withDepth(2).build(data);

        assertEquals(0.29436325678496866, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthTwo_AllTheSameScore() throws FileNotFoundException, NotBinarisedException, Infeasible, InterruptedException {
        SetUp();
        Pair<ArrayList<DecisionTree>, Double> t = AllMurTreeBuilder.optimalTrees(data, 2);

        for (DecisionTree tree : t.getValue0()) {
            assertEquals(t.getValue1() / data.getSize(), tree.error(data), 0.00001);
            assertTrue(tree.depth() <= 2);
        }
    }

    @Test
    public void testTicTacToeMurTree_DepthTwoSet_AllTheSameScore() throws FileNotFoundException, NotBinarisedException, Infeasible, InterruptedException {
        SetUp();
        Pair<SetOfDecisionTrees, Double> t = AllMurTreeBuilder.optimalTreesSet(data, 2);

        for (DecisionTree tree : t.getValue0()) {
            assertEquals(t.getValue1() / data.getSize(), tree.error(data), 0.00001);
            assertTrue(tree.depth() <= 2);
        }
    }

    @Test
    public void testTicTacToeMurTree_DepthThree_AllTheSameScore() throws FileNotFoundException, NotBinarisedException, Infeasible, InterruptedException {
        SetUp();
        Pair<ArrayList<DecisionTree>, Double> t = AllMurTreeBuilder.optimalTrees(data, 3);

        for (DecisionTree tree : t.getValue0()) {
            assertEquals(t.getValue1() / data.getSize(), tree.error(data), 0.00001);
            assertTrue(tree.depth() <= 3);
        }
    }

    @Test
    public void testTicTacToeMurTree_DepthThreeSet_AllTheSameScore() throws FileNotFoundException, NotBinarisedException, Infeasible, InterruptedException {
        SetUp();
        Pair<SetOfDecisionTrees, Double> t = AllMurTreeBuilder.optimalTreesSet(data, 3);

        for (DecisionTree tree : t.getValue0()) {
            assertEquals(t.getValue1() / data.getSize(), tree.error(data), 0.00001);
            assertTrue(tree.depth() <= 3);
        }
    }


    @Test
    public void testTicTacToeMurTree_DepthFour_AllTheSameScore() throws FileNotFoundException, NotBinarisedException, Infeasible, InterruptedException {
        SetUp();
        Pair<ArrayList<DecisionTree>, Double> t = AllMurTreeBuilder.optimalTrees(data, 4);

        for (DecisionTree tree : t.getValue0()) {
            assertEquals(t.getValue1() / data.getSize(), tree.error(data), 0.00001);
            assertTrue(tree.depth() <= 4);
        }
    }

    @Test
    public void testTicTacToeMurTree_DepthThree() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new AllMurTreeBuilder().withDepth(3).build(data);

        assertEquals(0.2254697286012526, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthThree_normalizedWeights() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        data.normalizeWeights();

        Classifier c = new AllMurTreeBuilder().withDepth(3).build(data);

        assertEquals(0.2254697286012526, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFour() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new AllMurTreeBuilder().withDepth(4).build(data);

        assertEquals(0.1430062630480167, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFive() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new AllMurTreeBuilder().withDepth(5).build(data);

        assertEquals(0.06576200417536535, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFive_NoFalseNegatives() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);
            if (r.getActualClass() == 1) {
                r.setWeight(3.0);
            }
        }
        data.normalizeWeights();

        Classifier c = new AllMurTreeBuilder().withDepth(5).build(data);

        double truePositive = 0.0;
        double falsePositive = 0.0;
        double trueNegative = 0.0;
        double falseNegative = 0.0;

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);
            if (c.classify(r) == r.getActualClass()) {
                if (r.getActualClass() == 0) {
                    trueNegative += 1;
                }
                else {
                    truePositive += 1;
                }
            }
            else {
                if (r.getActualClass() == 0) {
                    falsePositive += 1;
                }
                else {
                    falseNegative += 1;
                }
            }
        }

        assertEquals(626.0, truePositive, 0.001);
        assertEquals(255.0, trueNegative, 0.001);
        assertEquals(77.0, falsePositive, 0.001);
        assertEquals(0.0, falseNegative, 0.001);

        // Error should be higher, since it is a bit of a trade-off to penalize false negatives
        assertTrue(0.06576200417536535 < c.error(data));
    }

    @Test
    public void testTicTacToeMurTree_DepthSix() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new AllMurTreeBuilder().withDepth(6).build(data);

        assertEquals(0.012526096033402923, c.error(data), 0.00000001);
    }

    @Test(expected = Infeasible.class)
    public void testOptimalTreeInf() throws Infeasible, FileNotFoundException, NotBinarisedException, InterruptedException {
        SetUp();
        Pair<DecisionTree, Double> res = AllMurTreeBuilder.optimalTree(data, -1);
    }
}
