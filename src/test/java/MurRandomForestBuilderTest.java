import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class MurRandomForestBuilderTest {
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(Main.path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

    @Test
    public void testNoMaxNumNodes() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3);

        assertEquals(7, b.getMaxNumberOfNodes());
        assertEquals(3, b.getMaxDepth());
    }

    @Test
    public void testGetSetBagging() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withBagging();
        assertTrue(b.appliesBagging());
    }

    @Test
    public void testGetNotSetBagging() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesBagging());
    }

    @Test
    public void testGetSetRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withRandomSubspaceMethod();
        assertTrue(b.appliesRandomSubspaceMethod());
    }

    @Test
    public void testGetNotSetRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesRandomSubspaceMethod());
    }

    @Test
    public void testGetSetPRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withSoftRestrictedRSM();
        assertTrue(b.appliesSoftRestrictedRSM());
    }

    @Test
    public void testGetNotSetPRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesSoftRestrictedRSM());
    }

    @Test
    public void testGetSetHardRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withHardRestrictedRSM();
        assertTrue(b.appliesHardRestrictedRSM());
    }

    @Test
    public void testGetNotSetHardRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesHardRestrictedRSM());
    }

    @Test
    public void testGetSetAdaBoost() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withAdaBoost();
        assertTrue(b.appliesAdaBoost());
    }

    @Test
    public void testGetNotSetAdaBoost() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesAdaBoost());
    }

    @Test
    public void testGetSetRandomRoot() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withRandomRoot();
        assertTrue(b.appliesRandomRoot());
    }

    @Test
    public void testGetNotSetRandomRoot() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesRandomRoot());
    }

    @Test
    public void testGetSetLowCorrelationBagging() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withLowCorrelationBagging();
        assertTrue(b.appliesLowCorrelationBagging());
    }

    @Test
    public void testGetNotSetLowCorrelationBagging() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesLowCorrelationBagging());
    }

    @Test
    public void testGetSetLowCorrelationRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withLowCorrelationRSM();
        assertTrue(b.appliesLowCorrelationRSM());
    }

    @Test
    public void testGetNotSetLowCorrelationRSM() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertFalse(b.appliesLowCorrelationRSM());
    }

    @Test
    public void testBasicConstructor() {
        MurRandomForestBuilder b = new MurRandomForestBuilder();

        assertEquals(TieResolvingStrategy.HIGHESTPRIOR, b.getTieResolvingStrategy());
        assertEquals(WeightType.EQUAL, b.getWeightType());
        assertFalse(b.appliesBagging());
        assertFalse(b.appliesRandomSubspaceMethod());
        assertFalse(b.appliesAdaBoost());
        assertFalse(b.appliesSoftRestrictedRSM());
        assertFalse(b.appliesHardRestrictedRSM());
    }

    @Test
    public void testGetSetWeightType() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10).withBagging().withWeightType(WeightType.OUTOFBAG);
        assertTrue(b.appliesBagging());
        assertEquals(WeightType.OUTOFBAG, b.getWeightType());
    }

    @Test
    public void testSetDepth() {
        MurRandomForestBuilder b = new MurRandomForestBuilder().withDepth(3);

        assertEquals(7, b.getMaxNumberOfNodes());
        assertEquals(3, b.getMaxDepth());
    }

    @Test
    public void testGetSetTieResolvingStrategy() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10)
                .withTieResolvingStrategy(TieResolvingStrategy.TREE);
        assertEquals(TieResolvingStrategy.TREE, b.getTieResolvingStrategy());
    }

    @Test
    public void testGetNotSetTieResolvingStrategy() {
        MurRandomForestBuilder b = new MurRandomForestBuilder(3, 10).withNumberOfTrees(10);
        assertEquals(TieResolvingStrategy.HIGHESTPRIOR, b.getTieResolvingStrategy());
    }

    @Test
    public void testBuild_Bagging_equal_weights() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3)
                .withBagging()
                .withNumberOfTrees(10);
        Forest f = b.build(data);

        assertTrue(f instanceof BaggedRandomForest);
        assertEquals(10, f.numberOfTrees());
    }

    @Test
    public void testBuild_Bagging_split_weights() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3)
                .withBagging()
                .withNumberOfTrees(10)
                .withWeightType(WeightType.SPLIT);
        Forest f = b.build(data);

        assertTrue(f instanceof BaggedRandomForest);
        assertEquals(10, f.numberOfTrees());
    }

    @Test
    public void testBuild_Bagging_oob_weights() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3)
                .withBagging()
                .withNumberOfTrees(10)
                .withWeightType(WeightType.OUTOFBAG);
        Forest f = b.build(data);

        assertTrue(f instanceof BaggedRandomForest);
        assertEquals(10, f.numberOfTrees());
    }

    @Test
    public void testBuild_RSM_equal_weights() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withRandomSubspaceMethod().withNumberOfTrees(11);
        Forest f = b.build(data);

        assertTrue(f instanceof RSMRandomForest);
        assertEquals(11, f.numberOfTrees());
    }

    @Test
    public void testBuild_RSM_split_weights() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withWeightType(WeightType.SPLIT).withRandomSubspaceMethod().withNumberOfTrees(11);
        Forest f = b.build(data);

        assertTrue(f instanceof RSMRandomForest);
        assertEquals(11, f.numberOfTrees());
    }

    @Test
    public void testBuild_adaBoost() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withAdaBoost().withNumberOfTrees(40);
        Forest f = b.build(data);

        assertTrue(f instanceof AdaBoostedForest);
        assertEquals(40, f.numberOfTrees());
    }

    @Test
    public void testBuild_RandomRoot_MoreThanF() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withRandomRoot().withNumberOfTrees(20);
        Forest f = b.build(data);

        assertTrue(f instanceof RandomRootForest);
        assertEquals(18, f.numberOfTrees());
    }

    @Test
    public void testBuild_RandomRoot_LessThanF() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withRandomRoot().withNumberOfTrees(15);
        Forest f = b.build(data);

        assertTrue(f instanceof RandomRootForest);
        assertEquals(15, f.numberOfTrees());
    }

    @Test
    public void testBuild_ProbabilityRestrictedRSM() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withSoftRestrictedRSM().withNumberOfTrees(15);
        Forest f = b.build(data);

        assertTrue(f instanceof SoftRestrictedRSMRandomForest);
        assertEquals(15, f.numberOfTrees());
    }

    @Test
    public void testBuild_HardRestrictedRSM() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withHardRestrictedRSM().withNumberOfTrees(15);
        Forest f = b.build(data);

        assertTrue(f instanceof HardRestrictedRandomForest);
        assertEquals(15, f.numberOfTrees());
    }

    @Test
    public void testBuild_LowCorrelationBagging() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withLowCorrelationBagging().withNumberOfTrees(15);
        Forest f = b.build(data);

        assertTrue(f instanceof LowCorrelationBaggedForest);
        assertTrue(f.numberOfTrees() <= 15);
    }

    @Test
    public void testBuild_LowCorrelationRSM() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withLowCorrelationRSM().withNumberOfTrees(15);
        Forest f = b.build(data);

        assertTrue(f instanceof LowCorrelationRSMForest);
        assertTrue(f.numberOfTrees() <= 15);
    }

    @Test
    public void testBuild_noStrategy() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3).withNumberOfTrees(10);
        Forest f = b.build(data);

        assertNull(f);
    }

    @Test
    public void testBuild2_TieTree() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        MurRandomForestBuilder b = new MurRandomForestBuilder(3)
                .withRandomSubspaceMethod()
                .withTieResolvingStrategy(TieResolvingStrategy.TREE)
                .withNumberOfTrees(11);

        Forest f = b.build(data);

        assertTrue(f instanceof RSMRandomForest);
        assertEquals(11, f.numberOfTrees());
        assertEquals(TieResolvingStrategy.TREE, b.getTieResolvingStrategy());
    }
}
