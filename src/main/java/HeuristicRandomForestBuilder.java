import Data.DataSet;
import Data.Record;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to use the heuristic Random Forest implementation from Weka.
 */
public class HeuristicRandomForestBuilder implements ForestBuilder {
    private int numberOfTrees;

    /**
     * Constructs the builder.
     */
    public HeuristicRandomForestBuilder() {
        this.numberOfTrees = 10;
    }

    /**
     * Setter for number of trees.
     * @param numberOfTrees number of trees
     * @return this
     */
    public HeuristicRandomForestBuilder withNumberOfTrees(int numberOfTrees) {
        this.numberOfTrees = numberOfTrees;
        return this;
    }

    /**
     * Getter for number of trees.
     * @return number of trees
     */
    public int getNumberOfTrees() {
        return this.numberOfTrees;
    }

    /**
     * Converts DataSet to dataset in Weka format.
     * @param data DataSet
     * @return DataSet in Weka format
     */
    public static Instances toInstances(DataSet data) {
        ArrayList<Attribute> attrs = new ArrayList<>();

        for (String s : data.getAttributeNames()) {
            List<String> options = binaryOptions();
            attrs.add(new Attribute(s, options));
        }

        List<String> options = binaryOptions();
        attrs.add(new Attribute(data.getTargetName(), options));

        Instances result = new Instances(
                "data",
                attrs,
                data.getSize()
        );

        result.setClass(result.attribute(data.getTargetName()));

        for (int i = 0; i < data.getSize(); i++) {
            result.add(toInstance(data.getEntry(i), result, false));
        }

        return result;
    }

    private static List<String> binaryOptions() {
        List<String> nominalValues = new ArrayList<>(2);
        nominalValues.add("0");
        nominalValues.add("1");

        return nominalValues;
    }

    /**
     * Converts record to Weka format.
     * @param r record
     * @param is instances (dataset)
     * @param isUnknown is unknown
     * @return record in Weka format
     */
    public static Instance toInstance(Record r, Instances is, boolean isUnknown) {
        Instance res = new DenseInstance(r.getNumberOfFeatures() + 1);
        res.setDataset(is);

        for (int i = 0; i < r.getNumberOfFeatures(); i++) {
            res.setValue(i, r.checkPredicate(i) ? "1" : "0");
        }

        if (!isUnknown) res.setClassValue(""+r.getActualClass());

        return res;
    }

    @Override
    public Classifier build(DataSet data) {
        Instances instances = toInstances(data);

        RandomForest r = new RandomForest();
        r.setBreakTiesRandomly(false);
        r.setNumIterations(numberOfTrees);

        try {
            r.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HeuristicRandomForest(r, numberOfTrees, instances);
    }
}
