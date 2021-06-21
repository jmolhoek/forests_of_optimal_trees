import Data.DataSet;
import Data.Record;
import weka.classifiers.trees.J48;
import weka.core.Instances;

/**
 * Wrapper for C4.5 heuristic decision tree from Weka.
 */
public class C45Tree implements Classifier {
    private final J48 tree;
    private final Instances instances;

    /**
     * Constructor for C4.5 tree.
     * @param data training data
     */
    public C45Tree(DataSet data) {
        tree = new J48();
        instances = HeuristicRandomForestBuilder.toInstances(data);

        try {
            tree.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int classify(Record record) {
        int res = -1;
        try {
            double classification = tree.classifyInstance(
                    HeuristicRandomForestBuilder.toInstance(record, instances, true));
            res = (int)classification;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
