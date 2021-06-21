package Data;

import org.apache.commons.lang.SerializationUtils;
import org.javatuples.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.round;

/**
 * Dataset keeps the data in a usable format.
 */
public class DataSet implements Serializable {
    private final ArrayList<Record> records;
    private final ArrayList<String> attributeNames;
    private final int numberOFeatures;
    private final String targetName;
    private int size;

    /**
     * Constructs an empty dataset.
     * @param names the names of the attributes (columns)
     */
    public DataSet(ArrayList<String> names, String targetName) {
        attributeNames = names;
        numberOFeatures = names.size();
        records = new ArrayList<>();
        this.targetName = targetName;
        size = 0;
    }

    /**
     * Adds a given record to the dataset.
     * @param r the record
     */
    public void addRecord(Record r) {
        records.add(r);
        size++;
    }

    /**
     * Adds all the given records to the dataset.
     * @param rs list of records
     */
    public void addRecords(ArrayList<Record> rs) {
        records.addAll(rs);
        size += rs.size();
    }

    /**
     * Returns the list of attribute names.
     * @return attribute names
     */
    public ArrayList<String> getAttributeNames() {
        return attributeNames;
    }

    /**
     * Returns the name of the target.
     * @return target name
     */
    public String getTargetName() { return targetName; }

    /**
     * Returns the number of attributes.
     * @return number of attributes
     */
    public int getNumberOfFeatures() {
        return numberOFeatures;
    }

    /**
     * Returns the number of records in the dataset.
     * @return size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the record at the given index.
     * @param i index
     * @return record at index i
     */
    public Record getEntry(int i) {
        return records.get(i);
    }

    @Override
    public String toString() {
        return "Data.DataSet{" +
                "attributeNames=" + attributeNames +
                ", numberOfAttributes=" + numberOFeatures +
                ", targetName=" + targetName +
                ", size=" + size +
                ", records=" + records +
                '}';
    }

    /**
     * Normalizes the weights of the instances such that the total is 1 (+/- small rounding error).
     */
    public void normalizeWeights() {
        double total = 0.0;

        for (Record r : records) {
            total += r.getWeight();
        }

        for (Record r: records) {
            r.setWeight(r.getWeight() / total);
        }
    }

    /**
     * Resets all the weights to 1/size.
     */
    public void resetWeights() {
        for (Record r: records) {
            r.setWeight(1);
        }
        normalizeWeights();
    }

    /**
     * Checks if the DataSet contains the record.
     * @param r the record
     * @return true if the DataSet contains the record
     */
    public boolean contains(Record r) {
        return records.contains(r);
    }

    /**
     * Splits the data on attribute i. First value is all instances where i is true, second value is all instances where i is false.
     * @param i the attribute
     * @return (all instances where i, all instances where not i)
     */
    public Pair<DataSet, DataSet> splitOnAttribute(int i) {
        DataSet pos = new DataSet(attributeNames, targetName);
        DataSet neg = new DataSet(attributeNames, targetName);

        for (int j = 0; j < size; j++) {
            Record r = records.get(j);
            if (r.checkPredicate(i)) {
                pos.addRecord(r);
            }
            else {
                neg.addRecord(r);
            }
        }

        return new Pair<>(pos, neg);
    }

    /**
     * Splits the dataset into a set of instances where the target is 1, and a set where the target is 0.
     * Unknown target instances are deleted.
     *
     * @return D^+ and D^-
     */
    public Pair<DataSet, DataSet> splitOnTarget() {
        DataSet pos = new DataSet(attributeNames, targetName);
        DataSet neg = new DataSet(attributeNames, targetName);

        for (int j = 0; j < size; j++) {
            Record r = records.get(j);
            if (r.getActualClass() == 0) {
                neg.addRecord(r);
            }
            else {
                pos.addRecord(r);
            }
        }

        return new Pair<>(pos, neg);
    }

    /**
     * Splits the dataset into a training set and a test set.
     * @param fractionTrainingData number between 0 and 1, representing the fraction of the data that enters the training set
     * @return a pair where the zeroth element is the training dataset and the first element is the test dataset
     */
    public synchronized Pair<DataSet, DataSet> split(double fractionTrainingData) {
        DataSet copyOfThis = (DataSet) SerializationUtils.clone(this);

        if (fractionTrainingData >= 1) {
            return new Pair<>(copyOfThis, new DataSet(attributeNames, targetName));
        }
        if (fractionTrainingData <= 0) {
            return new Pair<>(new DataSet(attributeNames, targetName), copyOfThis);
        }
        else {
            int s = size;
            long trainingSize = round(fractionTrainingData * s);
            long testSize = s - trainingSize;

            DataSet trainingSet = new DataSet(attributeNames, targetName);
            DataSet testSet = new DataSet(attributeNames, targetName);

            for (long i = 0; i < testSize; i++) {
                int randomNum = ThreadLocalRandom.current().nextInt(0, s);
                Record r = copyOfThis.records.remove(randomNum);
                s--;
                testSet.addRecord(r);
            }

            trainingSet.addRecords(copyOfThis.records);

            return new Pair<>(trainingSet, testSet);
        }
    }

    /**
     * Creates a bootstrap of the dataset (with replacement). The bootstrap is of size N.
     * @return bootstrap
     */
    public DataSet getBootstrap() {
        DataSet result = new DataSet(attributeNames, targetName);

        for (long i = 0; i < size; i++) {
            int index = ThreadLocalRandom.current().nextInt(0, size);
            result.addRecord((Record) SerializationUtils.clone(records.get(index)));
        }

        return result;
    }

    /**
     * Creates a bootstrap of the dataset (with replacement) of size N. Also returns the Out-Of-Bag set.
     * @return respectively bootstrap and out-of-bag-data
     */
    public Pair<DataSet, DataSet> getBootstrapAndOOB() {
        DataSet bootstrap = new DataSet(attributeNames, targetName);
        DataSet oob = new DataSet(attributeNames, targetName);

        Set<Integer> notUsedIndices = new HashSet<>();

        for (int i = 0; i < size; i++) {
            notUsedIndices.add(i);
        }

        for (int i = 0; i < size; i++) {
            int index = ThreadLocalRandom.current().nextInt(0, size);

            bootstrap.addRecord((Record) SerializationUtils.clone(records.get(index)));

            notUsedIndices.remove(index);
        }

        for (Integer index : notUsedIndices) {
            oob.addRecord((Record) SerializationUtils.clone(records.get(index)));
        }

        return new Pair<>(bootstrap, oob);

    }

    /**
     * Returns true if the dataset is pure.
     * (If all entries have the same target)
     * @return whether the data is pure
     */
    public boolean isPure() {
        int firstClass = this.records.get(0).getActualClass();

        for (Record r : this.records) {
            if (r.getActualClass() != firstClass) return false;
        }

        return true;
    }

    /**
     * Returns the data mapped to the provided subspace.
     * E.g. [0,1] means only the first and second feature.
     *
     * @param subspace the subspace
     * @return data mapped to the subspace
     */
    public DataSet getSubspaceData(List<Integer> subspace) {
        DataSet result = new DataSet(
                this.getSubSpaceNames(subspace),
                targetName
        );

        for (Record r : this.records) {
            result.addRecord(r.mapToSubSpace(subspace));
        }

        return result;
    }

    private ArrayList<String> getSubSpaceNames(List<Integer> subspace) {
        ArrayList<String> result = new ArrayList<>(subspace.size());

        for (int i : subspace) {
            result.add(attributeNames.get(i));
        }

        return result;
    }

    /**
     * Returns the class that is most frequent in the data (highest prior probability).
     * This method assumes that there are only two classes (0 / 1).
     * @return the most frequent class
     */
    public int classWithHighestPrior() {
        int zeroCounter = 0;
        int oneCounter = 0;
        for (Record r : records) {
            if (r.getActualClass() == 0) {
                zeroCounter++;
            }
            else if (r.getActualClass() == 1) {
                oneCounter++;
            }
            else {
                throw new IllegalArgumentException("class unknown");
            }
        }

        if (zeroCounter > oneCounter) {
            return 0;
        }
        else {
            return 1;
        }
    }

    /**
     * Splits the dataset into k folds at random.
     *
     * @param k number of folds
     * @return the folds
     */
    public List<DataSet> getKFolds(int k) {
        // Create a shuffled copy of the dataset
        DataSet copyOfThis = (DataSet) SerializationUtils.clone(this);
        Collections.shuffle(copyOfThis.records);

        // Result
        List<DataSet> dataSets = new LinkedList<>();

        int sizeOfFold = copyOfThis.size / k;

        for (int i = 0; i < k; i++) {
            dataSets.add(new DataSet(copyOfThis.attributeNames, copyOfThis.targetName));

            // Ensure that the last fold gets the rest of the data.
            int upperbound = i == k - 1 ? copyOfThis.size : i * sizeOfFold + sizeOfFold;

            // take from i*sizeOfFold until i*sizeOfFold + sizeOfFold (exclusive)
            for (int j = i * sizeOfFold; j < upperbound; j++) {
                dataSets.get(i).addRecord(copyOfThis.getEntry(j));
            }

        }

        return dataSets;
    }

    /**
     * Splits the dataset into k folds at random, but preserving the ratio of D^+/D^-.
     * @param k number of folds
     * @return folds
     */
    public List<DataSet> getSmartKFolds(int k) {
        Pair<DataSet, DataSet> p = this.splitOnTarget();
        DataSet pos = p.getValue0();
        DataSet neg = p.getValue1();

        List<DataSet> posFolds = pos.getKFolds(k);
        List<DataSet> negFolds = neg.getKFolds(k);

        return mergeFolds(posFolds, negFolds);
    }

    private static List<DataSet> mergeFolds(List<DataSet> left, List<DataSet> right) {
        if (left.size() != right.size()) throw new IllegalArgumentException();
        List<DataSet> result = new LinkedList<>();

        for (int i = 0; i < left.size(); i++) {
            DataSet d = new DataSet(left.get(i).attributeNames, left.get(i).targetName);
            d.addRecords(left.get(i).records);
            d.addRecords(right.get(i).records);
            result.add(d);
        }

        return result;
    }

    /**
     * Merges all the datasets.
     * @param dataSets datasets
     * @return merged data
     */
    public static DataSet union(List<DataSet> dataSets) {
        DataSet result = new DataSet(dataSets.get(0).attributeNames, dataSets.get(0).targetName);

        for (DataSet d : dataSets) {
            result.addRecords(d.records);
        }

        return result;
    }

    /**
     * Given a list of folds and desired test-fold index i, this method returns
     * the union of all folds except i.
     *
     * @param folds list of folds
     * @param i index of test set
     * @return union of all but i
     */
    public static DataSet getTrainingSetWithIAsTestSet(List<DataSet> folds, int i) {
        List<DataSet> dataForTraining = new LinkedList<>();

        for (int j = 0; j < folds.size(); j++) {
            if (i != j) {
                dataForTraining.add(folds.get(j));
            }
        }

        return DataSet.union(dataForTraining);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSet)) return false;
        DataSet dataSet = (DataSet) o;
        return numberOFeatures == dataSet.numberOFeatures
                && size == dataSet.size
                && records.equals(dataSet.records)
                && attributeNames.equals(dataSet.attributeNames);
    }
}
