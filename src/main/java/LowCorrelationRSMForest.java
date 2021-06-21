import Data.DataSet;
import Data.Record;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random Forest that applies the Random Subspace Method. Each next tree is generated by using the altered MurTree algorithm to get all optimal trees.
 * From this list, the tree with the lowest correlation with the existing trees is picked.
 */
public class LowCorrelationRSMForest extends Forest {
    private final ArrayList<WeightedTree> trees;
    private final Classifier tieResolver;

    /**
     * Constructs the forest.
     * @param d training data
     * @param numberOfTrees number of trees
     * @param treeBuilder builder for optimal trees (sort of carrier for the depth parameter)
     * @param tieResolver classifier to resolve ties
     * @param subspaceFraction size of the subspace (0.5 samples 50% of the columns)
     * @throws InterruptedException if interrupted (e.g. by time-out)
     */
    public LowCorrelationRSMForest(DataSet d, int numberOfTrees, MurTreeBuilder treeBuilder, Classifier tieResolver, double subspaceFraction)
            throws InterruptedException {
        this.trees = new ArrayList<>(numberOfTrees);
        this.tieResolver = tieResolver;

        List<Integer> initSubspace = RSMRandomForest.generateRandomSubspace(d.getNumberOfFeatures(), subspaceFraction);
        DataSet initSubspaceData = d.getSubspaceData(initSubspace);
        DecisionTree initTree = treeBuilder.build(initSubspaceData);
        this.addTree(initTree, initSubspace);

        for (int i = 0; i < numberOfTrees - 1; i++) {
            List<Integer> subspace = RSMRandomForest.generateRandomSubspace(d.getNumberOfFeatures(), subspaceFraction);
            DataSet subspaceData = d.getSubspaceData(subspace);

            ArrayList<DecisionTree> optimalTrees;

            try {
                Pair<ArrayList<DecisionTree>, Double> res = AllMurTreeBuilder.optimalTrees(subspaceData, treeBuilder.getDepth());
                optimalTrees = res.getValue0();
            }
            catch (Infeasible e) {
                e.printStackTrace();
                return;
            }

            ArrayList<DecisionTree> minCorrelationTrees = new ArrayList<>();
            double minCorrelation = Double.MAX_VALUE;
            int treeDuplicates = 0;

            for (DecisionTree t : optimalTrees) {
                try {
                    double correlation = LowCorrelationBaggedForest.correlation(trees, t, d);
                    if (correlation <= minCorrelation) {
                        if (correlation < minCorrelation) minCorrelationTrees.clear();
                        minCorrelation = correlation;
                        minCorrelationTrees.add(t);
                    }
                }
                catch (TreeAlreadyExists e) {
                    treeDuplicates++;
                }
            }

            if (treeDuplicates == optimalTrees.size()) {
                continue;
            }

            this.addTree(
                    minCorrelationTrees.get(ThreadLocalRandom.current().nextInt(minCorrelationTrees.size())),
                    subspace
            );
        }
    }

    private synchronized void addTree(DecisionTree t, List<Integer> subspace) {
        trees.add(new WeightedTree(t, 1.0, subspace));
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