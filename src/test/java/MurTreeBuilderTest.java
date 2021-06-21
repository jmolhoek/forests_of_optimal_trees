import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import Data.Record;
import org.javatuples.Pair;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class MurTreeBuilderTest {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

    @Test
    public void testTicTacToeMurTree_DepthOne() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(1).withMaxNumberOfNodes(10000).build(data);

        assertEquals(0.30062630480167013, c.error(data), 0.00000001);
    }

    @Test
    public void testMurTreeBuilder() {
        MurTreeBuilder b = new MurTreeBuilder().withDepth(1).withMaxNumberOfNodes(10000);

        assertEquals(1, b.getDepth());
        assertEquals(10000, b.getNumberOfNodes());
    }

    @Test
    public void testMurTreeBuilderInfeasible() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        Classifier c = new MurTreeBuilder().withDepth(1).withMaxNumberOfNodes(-10000).build(data);

        assertNull(c);
    }

    @Test
    public void testMurTreeBuilderInfeasible2() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        Classifier c = new MurTreeBuilder().withDepth(-1).withMaxNumberOfNodes(10000).build(data);

        assertNull(c);
    }

    @Test
    public void testTicTacToeMurTree_DepthTwo() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(2).withMaxNumberOfNodes(10000).build(data);

        assertEquals(0.29436325678496866, c.error(data), 0.00000001);
    }


    @Test
    public void testTicTacToeMurTree_DepthTwo_twoNodes() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(2).withMaxNumberOfNodes(2).build(data);

        assertEquals(0.29436325678496866, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthThree() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(3).withMaxNumberOfNodes(10000).build(data);

        assertEquals(0.2254697286012526, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthThree_normalizedWeights() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        data.normalizeWeights();

        Classifier c = new MurTreeBuilder().withDepth(3).withMaxNumberOfNodes(10000).build(data);

        assertEquals(0.2254697286012526, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFour() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(4).withMaxNumberOfNodes(10000).build(data);

        assertEquals(0.1430062630480167, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFive() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(5).withMaxNumberOfNodes(10000).build(data);

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

        Classifier c = new MurTreeBuilder().withDepth(5).withMaxNumberOfNodes(10000).build(data);

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
    public void testTicTacToeMurTree_DepthFive_11Nodes() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(5).withMaxNumberOfNodes(11).build(data);

        assertEquals(0.1409185803757829, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFive_10Nodes() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(5).withMaxNumberOfNodes(10).build(data);

        assertEquals(0.14822546972860126, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthFive_5Nodes() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(5).withMaxNumberOfNodes(5).build(data);

        assertEquals(0.19832985386221294, c.error(data), 0.00000001);
    }

    @Test
    public void testTicTacToeMurTree_DepthSix() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        Classifier c = new MurTreeBuilder().withDepth(6).withMaxNumberOfNodes(10000).build(data);

        assertEquals(0.012526096033402923, c.error(data), 0.00000001);
    }

    @Test(expected = Infeasible.class)
    public void testOptimalTreeInf() throws Infeasible, FileNotFoundException, NotBinarisedException, InterruptedException {
        SetUp();
        Pair<DecisionTree, Double> res = MurTreeBuilder.optimalTree(data, -1, 10);
    }

    @Test(expected = Infeasible.class)
    public void testOptimalTreeInf2() throws Infeasible, FileNotFoundException, NotBinarisedException, InterruptedException {
        SetUp();
        Pair<DecisionTree, Double> res = MurTreeBuilder.optimalTree(data, 1, -10);
    }
}
