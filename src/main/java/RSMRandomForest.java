import Data.DataSet;
import Data.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;

/**
 * Random forest created using the Random Subspace Method (Ho, 1998).
 */
public class RSMRandomForest extends Forest {
    private final ArrayList<WeightedTree> trees;
    private final Classifier tieResolver;

    /**
     * Constructs a Random Forest making use of the Random Subspace Method.
     * @param d training data-set
     * @param numberOfTrees number of trees in the forest (not counting the tie-resolver, in case it is a tree)
     * @param subspaceFraction size of the subspace (0.5 samples 50% of the columns)
     * @param treeBuilder builder for tree generation
     * @param tieResolver tie resolver
     * @param weightType can be EQUAL or SPLIT
     */
    public RSMRandomForest(DataSet d, int numberOfTrees, double subspaceFraction,
                           MurTreeBuilder treeBuilder, Classifier tieResolver, WeightType weightType) throws InterruptedException {
        if (weightType == WeightType.OUTOFBAG) throw new IllegalArgumentException();
        this.tieResolver = tieResolver;
        this.trees = new ArrayList<>(numberOfTrees);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfTrees);

        for (int i = 0; i < numberOfTrees; i++) {
            executor.execute(() -> {
                List<Integer> subspace = generateRandomSubspace(d.getNumberOfFeatures(), subspaceFraction);
                if (weightType == WeightType.SPLIT) {
                    List<DataSet> split = d.getSmartKFolds(2);
                    DataSet validationSet = split.get(0);
                    DataSet trainingSet = split.get(1);

                    DataSet subspaceData = trainingSet.getSubspaceData(subspace);

                    DecisionTree newTree = treeBuilder.build(subspaceData);
                    double weight = newTree.accuracy(validationSet);
                    this.addTree(newTree, weight, subspace);
                }
                else if (weightType == WeightType.EQUAL) {
                    DataSet subspaceData = d.getSubspaceData(subspace);

                    DecisionTree newTree = treeBuilder.build(subspaceData);
                    this.addTree(newTree, 1, subspace);
                }
            });
        }

        executor.shutdown();
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
            System.out.println("Generation of RSM forest of size " + numberOfTrees + " took too long.");
        }

        // Normalize the weights
        double totalWeight = 0.0;
        for (WeightedTree t : trees) {
            totalWeight += t.getWeight();
        }

        for (WeightedTree t : trees) {
            t.normalizeBy(totalWeight);
        }
    }

    private synchronized void addTree(DecisionTree newTree, double weight, List<Integer> subspace) {
        trees.add(new WeightedTree(newTree, weight, subspace));
    }

    /**
     * Generates a list that represents a random subspace of columns.
     * For example: generateRandomSubspace(8, 0.5) could return [0,2,3,6].
     * @param numberOfFeatures number of columns
     * @param subspaceFraction desired fraction of columns in subspace
     * @return subspace representation (column indices)
     */
    public static List<Integer> generateRandomSubspace(int numberOfFeatures, double subspaceFraction) {
        int subspaceSize = (int) round(subspaceFraction * numberOfFeatures);
        List<Integer> fullSubspace = generateFullSubspace(numberOfFeatures);
        List<Integer> subspace = new ArrayList<>(subspaceSize);

        for (int i = 0; i < subspaceSize; i++) {
            int index = ThreadLocalRandom.current().nextInt(0, fullSubspace.size());
            Integer elem = fullSubspace.remove(index);
            subspace.add(elem);
        }

        Collections.sort(subspace);
        return subspace;
    }

    /**
     * Generates a list [0,1,2,...,dataSize-1].
     * @param dataSize data size
     * @return the list
     */
    public static List<Integer> generateFullSubspace(int dataSize) {
        List<Integer> ints = new LinkedList<>();

        for (int i = 0; i < dataSize; i++) {
            ints.add(i);
        }

        return ints;
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
