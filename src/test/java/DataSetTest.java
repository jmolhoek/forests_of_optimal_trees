import Data.DataSet;
import Data.Record;
import org.javatuples.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class DataSetTest {
    DataSet data;

    public void SetUp() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet d = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true))),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true))),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)))
        ));

        d.addRecords(records);

        data = d;
    }

    public void SetUp2() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, false)),0)
        ));

        data.addRecords(records);
        this.data = data;
    }

    @Test
    public void testConstructor() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        assertEquals(3, data.getNumberOfFeatures());
        assertArrayEquals(names, data.getAttributeNames().toArray());
        assertEquals("isBlond", data.getTargetName());
    }

    @Test
    public void testAddRecord() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(true, true, true, true))));
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(true, false, true, true))));
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(true, false, false, true))));

        assertEquals(3, data.getSize());
    }

    @Test
    public void testAddRecords() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(false, false, true, false))));

        ArrayList<Record> records = new ArrayList<Record>(Arrays.asList(
                new Record(new ArrayList<Boolean>(Arrays.asList(true, true, true, true))),
                new Record(new ArrayList<Boolean>(Arrays.asList(true, false, true, true))),
                new Record(new ArrayList<Boolean>(Arrays.asList(true, false, false, true)))
        ));

        data.addRecords(records);

        assertEquals(4, data.getSize());
    }

    @Test
    public void testGetEntry() {
        SetUp();

        assertTrue(data.getEntry(0).checkPredicate(0));
        assertTrue(data.getEntry(0).checkPredicate(1));
        assertTrue(data.getEntry(0).checkPredicate(2));
        assertTrue(data.getEntry(0).checkPredicate(3));
        assertTrue(data.getEntry(1).checkPredicate(0));
        assertFalse(data.getEntry(1).checkPredicate(1));
        assertTrue(data.getEntry(1).checkPredicate(2));
        assertTrue(data.getEntry(1).checkPredicate(3));
        assertTrue(data.getEntry(2).checkPredicate(0));
        assertFalse(data.getEntry(2).checkPredicate(1));
        assertFalse(data.getEntry(2).checkPredicate(2));
        assertTrue(data.getEntry(2).checkPredicate(3));
    }

    @Test
    public void testToString() {
        SetUp();

        assertEquals("Data.DataSet{attributeNames=[isTall, isOld, isFat], " +
                "numberOfAttributes=3, targetName=isBlond, size=3, records=[" +
                "Data.Record{features=[true, true, true, true], actualClass=-1}, " +
                "Data.Record{features=[true, false, true, true], actualClass=-1}, " +
                "Data.Record{features=[true, false, false, true], actualClass=-1}]}", data.toString());
    }

    @Test
    public void testSplit1() {
        SetUp();
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(false, false, true, false))));

        Pair<DataSet, DataSet> tup = data.split(0.5);
        DataSet trainingSet = tup.getValue0();
        DataSet testSet = tup.getValue1();

        assertEquals(trainingSet.getSize(), testSet.getSize());
        assertEquals(2, trainingSet.getSize());
    }

    @Test
    public void testSplit2() {
        SetUp();
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(false, false, true, false))));

        Pair<DataSet, DataSet> tup = data.split(0.75);
        DataSet trainingSet = tup.getValue0();
        DataSet testSet = tup.getValue1();

        assertEquals(1, testSet.getSize());
        assertEquals(3, trainingSet.getSize());
    }

    @Test
    public void testSplit3() {
        SetUp();
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(false, false, true, false))));

        Pair<DataSet, DataSet> tup = data.split(0.25);
        DataSet trainingSet = tup.getValue0();
        DataSet testSet = tup.getValue1();

        assertEquals(3, testSet.getSize());
        assertEquals(1, trainingSet.getSize());
    }

    @Test
    public void testSplit4() {
        SetUp();
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(false, false, true, false))));

        Pair<DataSet, DataSet> tup = data.split(-0.2);
        DataSet trainingSet = tup.getValue0();
        DataSet testSet = tup.getValue1();

        assertEquals(4, testSet.getSize());
        assertEquals(0, trainingSet.getSize());
    }

    @Test
    public void testSplit5() {
        SetUp();
        data.addRecord(new Record(new ArrayList<Boolean>(Arrays.asList(false, false, true, false))));

        Pair<DataSet, DataSet> tup = data.split(1.2);
        DataSet trainingSet = tup.getValue0();
        DataSet testSet = tup.getValue1();

        assertEquals(0, testSet.getSize());
        assertEquals(4, trainingSet.getSize());
    }

    @Test
    public void testGetKFolds1() {
        SetUp2();

        List<DataSet> folds = data.getKFolds(3);

        assertEquals(2, folds.get(0).getSize());
        assertEquals(2, folds.get(1).getSize());
        assertEquals(2, folds.get(2).getSize());
    }

    @Test
    public void testGetKFolds2() {
        SetUp2();

        List<DataSet> folds = data.getKFolds(2);

        assertEquals(3, folds.get(0).getSize());
        assertEquals(3, folds.get(1).getSize());
    }

    @Test
    public void testGetKFolds3() {
        SetUp2();

        List<DataSet> folds = data.getKFolds(5);

        assertEquals(1, folds.get(0).getSize());
        assertEquals(1, folds.get(1).getSize());
        assertEquals(1, folds.get(2).getSize());
        assertEquals(1, folds.get(3).getSize());
        assertEquals(2, folds.get(4).getSize());
    }

    @Test
    public void testGetKFolds5() {
        SetUp2();

        List<DataSet> folds = data.getKFolds(4);

        assertEquals(1, folds.get(0).getSize());
        assertEquals(1, folds.get(1).getSize());
        assertEquals(1, folds.get(2).getSize());
        assertEquals(3, folds.get(3).getSize());
    }

    @Test
    public void testHighestPrior1() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, false)), 0)
        ));

        data.addRecords(records);

        assertEquals(1, data.classWithHighestPrior());
    }

    @Test
    public void testHighestPrior2() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, false)), 0)
        ));

        data.addRecords(records);

        assertEquals(0, data.classWithHighestPrior());
    }

    @Test
    public void testHighestPrior3() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, false)), 0)
        ));

        data.addRecords(records);

        assertEquals(1, data.classWithHighestPrior());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testHighestPrior4() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true))),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, false)), 0)
        ));

        data.addRecords(records);

        int c = data.classWithHighestPrior();
    }

    @Test
    public void testBootstrap() {
        SetUp2();

        DataSet bs = data.getBootstrap();

        assertEquals(data.getSize(), bs.getSize());
    }

    @Test
    public void testUnion() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");
        Record r1 = new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1);
        Record r2 = new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1);
        Record r3 = new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1);

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                r1, r2, r3
        ));

        data.addRecords(records);

        DataSet d1 = new DataSet(namesList,"isBlond");
        d1.addRecord(r1);
        DataSet d2 = new DataSet(namesList,"isBlond");
        d1.addRecord(r2);
        DataSet d3 = new DataSet(namesList,"isBlond");
        d1.addRecord(r3);

        DataSet d = DataSet.union(new ArrayList<>(
                Arrays.asList(
                        d1, d2, d3
                )
        ));

        assertEquals(data, d);
    }

    @Test
    public void testSplitOnAttribute() {
        SetUp2();

        Pair<DataSet, DataSet> split = data.splitOnAttribute(0);
        DataSet pos = split.getValue0();
        DataSet neg = split.getValue1();

        assertEquals(3, pos.getSize());
        assertEquals(3, neg.getSize());

        assertTrue(pos.getEntry(0).checkPredicate(0));
        assertTrue(pos.getEntry(1).checkPredicate(0));
        assertTrue(pos.getEntry(2).checkPredicate(0));
        assertFalse(neg.getEntry(0).checkPredicate(0));
        assertFalse(neg.getEntry(1).checkPredicate(0));
        assertFalse(neg.getEntry(2).checkPredicate(0));
    }

    @Test
    public void testGetTrainingSetWithIAsTestSet() {
        List<DataSet> dataSets = new ArrayList<>(Arrays.asList(
                createDataSet(true, true, true, true),
                createDataSet(false, true, true, true),
                createDataSet(true, false, true, true),
                createDataSet(false, false, true, true)
        ));
        DataSet trainingSet = DataSet.getTrainingSetWithIAsTestSet(dataSets, 0);

        assertEquals(3, trainingSet.getSize());

        assertFalse(trainingSet.getEntry(0).checkPredicate(0));
        assertTrue(trainingSet.getEntry(0).checkPredicate(1));
        assertFalse(trainingSet.getEntry(1).checkPredicate(1));
        assertTrue(trainingSet.getEntry(1).checkPredicate(0));
        assertFalse(trainingSet.getEntry(2).checkPredicate(0));
        assertFalse(trainingSet.getEntry(2).checkPredicate(1));
    }

    private DataSet createDataSet(boolean b1, boolean b2, boolean b3, boolean b4) {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Collections.singletonList(
                new Record(new ArrayList<>(Arrays.asList(b1, b2, b3, b4)))
        ));

        data.addRecords(records);

        return data;
    }

    @Test
    public void testEquals() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        DataSet data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1)
        ));

        data.addRecords(records);

        String[] names2 = {"isTall","isOld","isFat"};
        ArrayList<String> namesList2 = new ArrayList<>(Arrays.asList(names2));

        DataSet data2 = new DataSet(namesList2,"isBlond");

        ArrayList<Record> records2 = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1)
        ));

        data2.addRecords(records2);

        String[] names3 = {"isTally","isOld","isFat"};
        ArrayList<String> namesList3 = new ArrayList<>(Arrays.asList(names3));

        DataSet data3 = new DataSet(namesList3,"isBlond");

        ArrayList<Record> records3 = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1)
        ));

        data3.addRecords(records3);

        String[] names4 = {"isTall","isOld","isFat"};
        ArrayList<String> namesList4 = new ArrayList<>(Arrays.asList(names4));

        DataSet data4 = new DataSet(namesList4,"isBlond");

        ArrayList<Record> records4 = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, false)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1)
        ));

        data4.addRecords(records4);

        String[] names5 = {"isTall","isOld","isFat"};
        ArrayList<String> namesList5 = new ArrayList<>(Arrays.asList(names5));

        DataSet data5 = new DataSet(namesList5,"isBlond");

        ArrayList<Record> records5 = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1)
        ));

        data5.addRecords(records5);

        String[] names6 = {"isTall","isOld"};
        ArrayList<String> namesList6 = new ArrayList<>(Arrays.asList(names6));

        DataSet data6 = new DataSet(namesList6,"isBlond");

        ArrayList<Record> records6 = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true)), 1)
        ));

        data6.addRecords(records6);



        assertEquals(data, data);
        assertEquals(data, data2);
        assertNotEquals(data, (Object)"1");
        assertNotEquals(data, data3);
        assertNotEquals(data, data4);
        assertNotEquals(data, data5);
        assertNotEquals(data, data6);
    }

    @Test
    public void testSmartKFolds() {
        SetUp2();

        List<DataSet> folds = data.getSmartKFolds(3);

        assertEquals(2, folds.get(0).getSize());
        assertEquals(2, folds.get(1).getSize());
        assertEquals(2, folds.get(2).getSize());

        if (folds.get(0).getEntry(0).getActualClass() == 0) {
            assertEquals(1, folds.get(0).getEntry(1).getActualClass());
        }
        else {
            assertEquals(0, folds.get(0).getEntry(1).getActualClass());
        }

        if (folds.get(1).getEntry(0).getActualClass() == 0) {
            assertEquals(1, folds.get(1).getEntry(1).getActualClass());
        }
        else {
            assertEquals(0, folds.get(1).getEntry(1).getActualClass());
        }

        if (folds.get(2).getEntry(0).getActualClass() == 0) {
            assertEquals(1, folds.get(2).getEntry(1).getActualClass());
        }
        else {
            assertEquals(0, folds.get(2).getEntry(1).getActualClass());
        }
    }

    @Test
    public void testContains() {
        SetUp();

        assertTrue(data.contains(new Record(new ArrayList<>(Arrays.asList(true, false, true, true)))));
        assertTrue(data.contains(new Record(new ArrayList<>(Arrays.asList(true, false, false, true)))));
        assertFalse(data.contains(new Record(new ArrayList<>(Arrays.asList(false, false, false, true)))));
        assertFalse(data.contains(new Record(new ArrayList<>(Arrays.asList(true, false, false, false)))));
    }

    @Test
    public void testGetOOB() {
        SetUp2();

        Pair<DataSet, DataSet> res = data.getBootstrapAndOOB();
        DataSet bootstrap = res.getValue0();
        DataSet oob = res.getValue1();

        assertEquals(6, bootstrap.getSize());

        Record r1 = new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1);
        Record r2 = new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1);
        Record r3 = new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1);
        Record r4 = new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 0);
        Record r5 = new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0);
        Record r6 = new Record(new ArrayList<>(Arrays.asList(false, false, true, false)),0);

        if (oob.contains(r1)) {
            assertFalse(bootstrap.contains(r1));
        }
        if (oob.contains(r2)) {
            assertFalse(bootstrap.contains(r2));
        }
        if (oob.contains(r3)) {
            assertFalse(bootstrap.contains(r3));
        }
        if (bootstrap.contains(r4)) {
            assertFalse(oob.contains(r4));
        }
        if (bootstrap.contains(r5)) {
            assertFalse(oob.contains(r5));
        }
        if (bootstrap.contains(r6)) {
            assertFalse(oob.contains(r6));
        }
    }

    @Test
    public void testNormalize_SumIsOne() {
        SetUp();

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);
            r.setWeight(ThreadLocalRandom.current().nextDouble());
        }

        data.normalizeWeights();

        double sum = 0.0;

        for (int i = 0; i < data.getSize(); i++) {
            sum += data.getEntry(i).getWeight();
        }

        assertEquals(1.0, sum, 0.001);
    }

    @Test
    public void testResetWeights() {
        SetUp();

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);
            assertEquals(1.0, r.getWeight(), 0.001);
        }

        data.resetWeights();

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);
            assertEquals(1.0/data.getSize(), r.getWeight(), 0.001);
        }
    }

    @Test
    public void testGetSubspaceData() {
        SetUp();

        DataSet d1 = data.getSubspaceData(new ArrayList<>(Arrays.asList(1,2)));

        assertEquals(2, d1.getNumberOfFeatures());
        assertTrue(d1.getEntry(0).checkPredicate(0));
        assertTrue(d1.getEntry(0).checkPredicate(1));
        assertFalse(d1.getEntry(1).checkPredicate(0));
        assertTrue(d1.getEntry(1).checkPredicate(1));
        assertFalse(d1.getEntry(2).checkPredicate(0));
        assertFalse(d1.getEntry(2).checkPredicate(1));
    }

    @Test
    public void testIsPure() {
        SetUp2();

        assertFalse(data.isPure());
    }

    @Test
    public void testIsPure2() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        data = new DataSet(namesList,"isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1)
        ));

        data.addRecords(records);

        assertTrue(data.isPure());
    }
}
