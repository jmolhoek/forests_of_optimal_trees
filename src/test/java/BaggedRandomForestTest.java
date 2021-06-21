import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class BaggedRandomForestTest {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

//    @Test
//    public void testBuildBaggedRandomForest() throws InterruptedException, FileNotFoundException, NotBinarisedException {
//        SetUp();
//        BaggedRandomForest f = new BaggedRandomForest(
//                data,
//                99,
//                new MurTreeBuilder().withMaxNumberOfNodes(3).withDepth(2),
//                new ClassificationNode(0),
//                 WeightType.EQUAL
//        );
//
//        assertEquals(0.27, f.error(data), 0.02);
//    }
//
//    @Test
//    public void testBuildBaggedRandomForest_withTies() throws InterruptedException, FileNotFoundException, NotBinarisedException {
//        SetUp();
//        BaggedRandomForest f = new BaggedRandomForest(
//                data,
//                100,
//                new MurTreeBuilder().withMaxNumberOfNodes(3).withDepth(2),
//                new ClassificationNode(0),
//                WeightType.EQUAL
//        );
//
//        assertEquals(0.27, f.error(data), 0.04);
//    }
}
