import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class DataLoaderTest {
    private final String PATH_TO_RESOURCES = "src/test/resources";

    @Test(expected = NotBinarisedException.class)
    public void testLoadNonBinary() throws FileNotFoundException, NotBinarisedException {
        DataLoader.load(PATH_TO_RESOURCES + "/not_binarised_data.txt","matr");
    }

    @Test(expected = FileNotFoundException.class)
    public void testLoadNonExistingFile() throws FileNotFoundException, NotBinarisedException {
        DataLoader.load(PATH_TO_RESOURCES + "/gfsgsegrsgs.txt","matr");
    }

    @Test
    public void testLoadBinMatrix() throws FileNotFoundException, NotBinarisedException {
        DataSet data = DataLoader.load(PATH_TO_RESOURCES + "/bogusdata.txt","matr");
        assertEquals(4, data.getSize());
        assertEquals(3, data.getNumberOfFeatures());
    }

    @Test
    public void testLoadCsv() throws FileNotFoundException, NotBinarisedException {
        DataSet data = DataLoader.load(PATH_TO_RESOURCES + "/bogusdatacsv.csv","csv");
        assertEquals(4, data.getSize());
        assertEquals(3, data.getNumberOfFeatures());
    }
}
