import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class C45TreeTest {

    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private DataSet data;

    private void SetUp() throws FileNotFoundException, NotBinarisedException {
        data = DataLoader.load(path_to_datasets + "/tic-tac-toe_bin.txt", "matr");
    }

    @Test
    public void testBuilder() throws FileNotFoundException, NotBinarisedException {
        SetUp();
        C45TreeBuilder b = new C45TreeBuilder();

        Classifier t = b.build(data);

        assertTrue(t instanceof C45Tree);
        assertEquals(0.02087, t.error(data), 0.001);
    }
}
