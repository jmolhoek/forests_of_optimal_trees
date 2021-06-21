import Data.DataSet;
import Data.Record;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Forest of Optimal Trees generated using Bagging.
 */
public class BaggedRandomForest extends Forest {
    private final ArrayList<WeightedTree> trees;
    private final Classifier tieResolver;

    /**
     * Constructs a Random Forest.
     * @param d data
     * @param numberOfTrees number of trees in forest
     * @param treeBuilder optimal tree builder
     * @param tieResolver classifier to resolve ties
     * @param weightType can be EQUAL, OOB or SPLIT
     */
    public BaggedRandomForest(DataSet d, int numberOfTrees, MurTreeBuilder treeBuilder,
                              Classifier tieResolver, WeightType weightType) throws InterruptedException {
        this.tieResolver = tieResolver;
        trees = new ArrayList<>();

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfTrees);

        for (int i = 0; i < numberOfTrees; i++) {
            executor.execute(() -> {
                DecisionTree newTree;
                double weight;

                if (weightType == WeightType.SPLIT) {
                    List<DataSet> split = d.getSmartKFolds(2);
                    DataSet validationSet = split.get(0);
                    DataSet trainingSet = split.get(1);

                    DataSet bootStrap = trainingSet.getBootstrap();

                    newTree = treeBuilder.build(bootStrap);
                    weight = newTree.accuracy(validationSet);
                }
                else if (weightType == WeightType.OUTOFBAG) {
                    Pair<DataSet, DataSet> query = d.getBootstrapAndOOB();
                    DataSet bootstrap = query.getValue0();
                    DataSet oob = query.getValue1();

                    newTree = treeBuilder.build(bootstrap);
                    weight = newTree.accuracy(oob);
                }
                else { // WeightType.EQUAL
                    DataSet bootStrap = d.getBootstrap();

                    newTree = treeBuilder.build(bootStrap);
                    weight = 1.0;
                }

                this.addTree(newTree, weight);
            });
        }

        executor.shutdown();
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        if(!executor.awaitTermination(60, TimeUnit.MINUTES)) {
            System.out.println("Generation of bagged forest of size " + numberOfTrees + " took too long.");
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

    private synchronized void addTree(DecisionTree newTree, double weight) {
        trees.add(new WeightedTree(newTree, weight));
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
