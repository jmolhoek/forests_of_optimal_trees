import Data.DataSet;
import Data.Record;

import java.util.ArrayList;

import static java.lang.Math.exp;
import static java.lang.Math.log;

/**
 * AdaBoost with the MurTree algorithm.
 */
public class AdaBoostedForest extends Forest {
    private final ArrayList<WeightedTree> trees;
    private final Classifier tieResolver;

    /**
     * Constructor for a Forest of Optimal Trees generated with AdaBoost.
     * @param d training data
     * @param numberOfTrees number of trees in the forest
     * @param treeBuilder builder for MurTree
     * @param tieResolver classifier to resolve ties
     */
    public AdaBoostedForest(DataSet d, int numberOfTrees, MurTreeBuilder treeBuilder, Classifier tieResolver) {
        // Set-up
        d.resetWeights();
        trees = new ArrayList<>();
        this.tieResolver = tieResolver;

        // Main loop
        for (int i = 0; i < numberOfTrees; i++) {
            DecisionTree tree = treeBuilder.build(d);
            double totalError = tree.totalErrorOnWeightedInstances(d);
            double amountOfSay = amountOfSay(totalError);

            WeightedTree t = new WeightedTree(tree, amountOfSay);
            trees.add(t);

            for (int j = 0; j < d.getSize(); j++) {
                Record r = d.getEntry(j);
                double oldWeight = r.getWeight();
                double newWeight = newWeight(oldWeight, amountOfSay, tree.classify(r) == r.getActualClass());
                r.setWeight(newWeight);
            }
            d.normalizeWeights();
        }
    }

    private double amountOfSay(double totalError) {
        if (totalError == 0) totalError = 0.001;
        if (totalError == 1) totalError = 0.999;
        return log((1.0 - totalError) / totalError) / 2;
    }

    private double newWeight(double oldWeight, double amountOfSay, boolean classifiedCorrectly) {
        if (classifiedCorrectly) return oldWeight * exp(- amountOfSay);
        else return oldWeight * exp(amountOfSay);
    }

    @Override
    public int classify(Record record) {
        return Classifier.majorityVote(trees, record, tieResolver);
    }

    @Override
    public int numberOfTrees() {
        return trees.size();
    }
}
