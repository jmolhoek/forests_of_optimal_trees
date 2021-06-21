import Data.DataSet;
import Data.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Forest of Optimal Trees generated using a hard restriction: if a tree has a predicate node with f_i,
 * no other tree may have a predicate node with f_i.
 */
public class HardRestrictedRandomForest extends Forest {
    private final ArrayList<WeightedTree> trees;
    private final Classifier tieResolver;

    /**
     * Constructor for hard restricted Random Forest of Optimal Trees.
     * @param d training data
     * @param numberOfTrees maximum number of trees
     * @param treeBuilder MurTree builder
     * @param tieResolver classifier to resolve ties
     */
    public HardRestrictedRandomForest(DataSet d, int numberOfTrees,
                                      MurTreeBuilder treeBuilder, Classifier tieResolver) {
        this.trees = new ArrayList<>(numberOfTrees);
        this.tieResolver = tieResolver;

        List<Integer> allowedColumns = RSMRandomForest.generateFullSubspace(d.getNumberOfFeatures());

        for (int i = 0; i < numberOfTrees; i++) {
            if (allowedColumns.size() < 3) break;
            List<Integer> subspace = copyOfSubspace(allowedColumns);
            DataSet subspaceData = d.getSubspaceData(subspace);

            DecisionTree newTree = treeBuilder.build(subspaceData);
            this.addTree(newTree, subspace);

            Set<Integer> consideredFeatures = newTree.getConsideredFeatures();
            for (int j : consideredFeatures) {
                allowedColumns.remove((Object) j);
            }
        }
    }

    private static List<Integer> copyOfSubspace(List<Integer> subspace) {
        List<Integer> res = new ArrayList<>(subspace.size());

        res.addAll(subspace);

        return res;
    }

    private synchronized void addTree(DecisionTree newTree, List<Integer> subspace) {
        trees.add(new WeightedTree(newTree, 1, subspace));
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
