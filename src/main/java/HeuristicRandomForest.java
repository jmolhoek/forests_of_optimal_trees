import Data.Record;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/**
 * Wrapper to use the heuristic Random Forest implementation from Weka.
 */
public class HeuristicRandomForest extends Forest {
    private final RandomForest forest;
    private final int numberOfTrees;
    private final Instances instances;

    /**
     * Constructs the forest.
     * @param f forest
     * @param numberOfTrees number of trees
     * @param instances dataset in Weka format
     */
    public HeuristicRandomForest(RandomForest f, int numberOfTrees, Instances instances) {
        this.forest = f;
        this.numberOfTrees = numberOfTrees;
        this.instances = instances;
    }

    @Override
    public int classify(Record record){
        int res = -1;
        try {
            double classification = forest.classifyInstance(HeuristicRandomForestBuilder.toInstance(record, instances, true));
            res = (int)classification;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public int numberOfTrees() {
        return numberOfTrees;
    }
}
