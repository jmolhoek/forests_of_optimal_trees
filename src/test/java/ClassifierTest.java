import Data.DataSet;
import Data.Record;
import org.javatuples.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class ClassifierTest {
    DataSet data;

    public void setUp() {
        String[] names = {"isTall","isOld","isFat"};
        ArrayList<String> namesList = new ArrayList<>(Arrays.asList(names));

        data = new DataSet(namesList, "isBlond");

        ArrayList<Record> records = new ArrayList<>(Arrays.asList(
                new Record(new ArrayList<>(Arrays.asList(true, true, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, true, true, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(true, false, true, true)), 1),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(true, false, false, true)), 0),
                new Record(new ArrayList<>(Arrays.asList(false, false, true, false)), 0)
        ));

        data.addRecords(records);
    }

    @Test
    public void testAccuracy() {
        setUp();
        Classifier c = new ClassificationNode(0);
        double a = c.accuracy(data);

        assertEquals(0.6667, a, 0.01);
    }

    @Test
    public void testError() {
        setUp();
        Classifier c = new ClassificationNode(0);
        double e = c.error(data);

        assertEquals(0.3333, e, 0.01);
    }

    @Test
    public void testErrorAndAccuracy() {
        setUp();
        Classifier c = new ClassificationNode(0);
        double e = c.error(data);
        double a = c.accuracy(data);

        assertEquals(1, e + a, 0.0001);
    }

    @Test
    public void testWeightedTotalError() {
        setUp();

        Classifier c = new ClassificationNode(0);

        data.getEntry(0).setWeight(0.3);
        data.getEntry(1).setWeight(0.1);
        data.getEntry(2).setWeight(0.3);
        data.getEntry(3).setWeight(0.1);
        data.getEntry(4).setWeight(0.1);
        data.getEntry(5).setWeight(0.1);

        double err = c.totalErrorOnWeightedInstances(data);

        assertEquals(0.6, err, 0.0001);
    }

    @Test
    public void testMajorityVote_allZero() {
        setUp();

        DecisionTree c1 = new ClassificationNode(0);
        DecisionTree c2 = new ClassificationNode(0);
        DecisionTree ties = new ClassificationNode(0);

        int result = Classifier.majorityVote(
                new ArrayList<>(Arrays.asList(new WeightedTree(c1, 1.0), new WeightedTree(c2, 1.0))),
                data.getEntry(0),
                ties
        );

        assertEquals(result, 0);
    }

    @Test
    public void testMajorityVote_allOne() {
        setUp();

        DecisionTree c1 = new ClassificationNode(1);
        DecisionTree c2 = new ClassificationNode(1);
        DecisionTree ties = new ClassificationNode(1);

        int result = Classifier.majorityVote(
                new ArrayList<>(Arrays.asList(new WeightedTree(c1, 1.0), new WeightedTree(c2, 1.0))),
                data.getEntry(0),
                ties
        );

        assertEquals(result, 1);
    }

    @Test
    public void testMajorityVote_Tie() {
        setUp();

        DecisionTree c1 = new ClassificationNode(0);
        DecisionTree c2 = new ClassificationNode(1);
        DecisionTree ties = new ClassificationNode(2);

        int result = Classifier.majorityVote(
                new ArrayList<>(Arrays.asList(new WeightedTree(c1, 1.0), new WeightedTree(c2, 1.0))),
                data.getEntry(0),
                ties
        );

        assertEquals(result, 2);
    }

    @Test
    public void testMajorityVote_tieButWeight() {
        setUp();

        DecisionTree c1 = new ClassificationNode(0);
        DecisionTree c2 = new ClassificationNode(1);
        DecisionTree ties = new ClassificationNode(2);

        int result = Classifier.majorityVote(
                new ArrayList<>(Arrays.asList(new WeightedTree(c1, 2.0), new WeightedTree(c2, 1.0))),
                data.getEntry(0),
                ties
        );

        assertEquals(result, 0);
    }

    @Test
    public void testMajorityVote_onlyTieResolver() {
        setUp();

        DecisionTree ties = new ClassificationNode(2);

        int result = Classifier.majorityVote(
                new ArrayList<>(),
                data.getEntry(0),
                ties
        );

        assertEquals(result, 2);
    }

    @Test
    public void testMajorityVote_invalidClassifier() {
        setUp();

        DecisionTree c1 = new ClassificationNode(0);
        DecisionTree c2 = new ClassificationNode(2);
        DecisionTree ties = new ClassificationNode(1);

        int result = Classifier.majorityVote(
                new ArrayList<>(Arrays.asList(new WeightedTree(c1, 1.0), new WeightedTree(c2, 1.0))),
                data.getEntry(0),
                ties
        );

        assertEquals(result, 0);
    }
}
