import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeuristicRandomForestTest {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

    @Test
    public void testBuilder() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        HeuristicRandomForestBuilder b = new HeuristicRandomForestBuilder().withNumberOfTrees(100);
        Classifier c = b.build(data);

        assertTrue(c instanceof HeuristicRandomForest);
        assertEquals(0, c.error(data), 0.00000001);
        assertEquals(100, ((HeuristicRandomForest) c).numberOfTrees());
    }
}
