import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RSMRandomForestTest {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

    @Test
    public void testConstructRSMRandomForest() throws FileNotFoundException, NotBinarisedException, InterruptedException {
        SetUp();

        RSMRandomForest r = new RSMRandomForest(data,
                100,
                0.5,
                new MurTreeBuilder().withDepth(2).withMaxNumberOfNodes(3),
                new ClassificationNode(0),
                WeightType.EQUAL
                );

        assertEquals(0.295, r.error(data), 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeightOOB() throws InterruptedException, FileNotFoundException, NotBinarisedException {
        SetUp();

        RSMRandomForest r = new RSMRandomForest(data,
                100,
                0.5,
                new MurTreeBuilder().withDepth(2).withMaxNumberOfNodes(3),
                new ClassificationNode(0),
                WeightType.OUTOFBAG
        );
    }
}
