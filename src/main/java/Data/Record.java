package Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data.Record is an entry of Data.DataSet.
 */
public class Record implements Serializable {
    private final ArrayList<Boolean> features;
    private final int actualClass;
    private double weight;

    /**
     * Constructs a new record with the given information.
     * @param features list of booleans
     */
    public Record(ArrayList<Boolean> features) {
        this.features = features;
        actualClass = -1;
        weight = 1;
    }

    /**
     * Constructs a new record with the given information, when the actual class (target) is known.
     * @param features list of booleans
     * @param actualClass target class
     */
    public Record(ArrayList<Boolean> features, int actualClass) {
        this.features = features;
        this.actualClass = actualClass;
        weight = 1;
    }

    /**
     * Getter for the actual class.
     * @return actual class
     */
    public int getActualClass() {
        return actualClass;
    }

    /**
     * Getter for number of features.
     * @return size
     */
    public int getNumberOfFeatures() {
        return features.size();
    }

    /**
     * Evaluates a given predicate.
     * @param i the predicate
     * @return the evaluated predicate
     */
    public Boolean checkPredicate(int i) {
        return features.get(i);
    }

    /**
     * Maps the record to the given subspace.
     * @param subspace subspace
     * @return mapped record
     */
    public Record mapToSubSpace(List<Integer> subspace) {
        ArrayList<Boolean> fs = new ArrayList<>(subspace.size());

        for (int i : subspace) {
            fs.add(features.get(i));
        }

        Record res = new Record(fs, actualClass);
        res.setWeight(getWeight());

        return res;
    }

    /**
     * Getter for weight.
     * @return weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Setter for weight.
     * @param weight new weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Record)) return false;
        Record record = (Record) o;
        return actualClass == record.actualClass
                && Objects.equals(features, record.features);
    }

    @Override
    public String toString() {
        return "Data.Record{" +
                "features=" + features +
                ", actualClass=" + actualClass +
                '}';
    }
}
