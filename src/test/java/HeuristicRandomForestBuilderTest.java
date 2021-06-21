import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;
import weka.core.Instances;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class HeuristicRandomForestBuilderTest {
    private final String PATH_TO_RESOURCES = "src/test/resources";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(PATH_TO_RESOURCES + "/bogusdata.txt","matr");
    }

    @Test
    public void testToInstances() throws FileNotFoundException, NotBinarisedException {
        SetUp();

        Instances is = HeuristicRandomForestBuilder.toInstances(data);

        assertEquals(4, is.numAttributes());
        assertEquals(3, is.classIndex());
        assertEquals(4, is.numInstances());
    }

    @Test
    public void testGetSetNumberOfTrees() {
        HeuristicRandomForestBuilder h = new HeuristicRandomForestBuilder();

        assertEquals(10, h.getNumberOfTrees());

        HeuristicRandomForestBuilder h2 = new HeuristicRandomForestBuilder().withNumberOfTrees(101);

        assertEquals(101, h2.getNumberOfTrees());
    }
}
