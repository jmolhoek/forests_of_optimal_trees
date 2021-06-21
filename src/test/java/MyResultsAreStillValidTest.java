import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class MyResultsAreStillValidTest {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private static final int nRuns = 50;


//    @Test
//    public void testSoyBeanDepthThree() throws FileNotFoundException, NotBinarisedException, InterruptedException {
//        DataSet data = DataLoader.load(path_to_datasets + "/"+ "soybean" +".txt", "matr");
//
//        double errEqualRSM11 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.EQUAL)
//                        .withRandomSubspaceMethod()
//                        .withNumberOfTrees(11),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.10, errEqualRSM11, 0.1);
//
//        double errEqualRSM31 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.EQUAL)
//                        .withRandomSubspaceMethod()
//                        .withNumberOfTrees(31),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.10, errEqualRSM31, 0.1);
//
//        double errSplitRSM11 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.SPLIT)
//                        .withRandomSubspaceMethod()
//                        .withNumberOfTrees(11),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.10, errSplitRSM11, 0.1);
//
//        double errSplitRSM31 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.SPLIT)
//                        .withRandomSubspaceMethod()
//                        .withNumberOfTrees(31),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.10, errSplitRSM31, 0.1);
//
//        double errSplitBag11 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.SPLIT)
//                        .withBagging()
//                        .withNumberOfTrees(11),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.06, errSplitBag11, 0.1);
//
//        double errSplitBag31 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.SPLIT)
//                        .withBagging()
//                        .withNumberOfTrees(31),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.06, errSplitBag31, 0.1);
//
//        double errEqBag11 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.EQUAL)
//                        .withBagging()
//                        .withNumberOfTrees(11),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.06, errEqBag11, 0.1);
//
//        double errEqBag31 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.EQUAL)
//                        .withBagging()
//                        .withNumberOfTrees(31),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.05, errEqBag31, 0.1);
//
//        double errOOBBag11 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.OUTOFBAG)
//                        .withBagging()
//                        .withNumberOfTrees(11),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.06, errOOBBag11, 0.1);
//
//        double errOOBBag31 = Experiment.performance(
//                new MurRandomForestBuilder()
//                        .withDepth(3)
//                        .withWeightType(WeightType.OUTOFBAG)
//                        .withBagging()
//                        .withNumberOfTrees(31),
//                data,
//                nRuns
//        );
//
//        assertEquals(0.06, errOOBBag31, 0.1);
//    }
}
