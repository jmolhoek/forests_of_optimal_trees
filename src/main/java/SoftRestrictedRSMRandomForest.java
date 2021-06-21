import Data.DataSet;
import Data.Record;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.round;
import static weka.core.Utils.sum;

/**
 * Forest of Optimal Trees generated using the
 */
public class SoftRestrictedRSMRandomForest extends Forest {
    private final ArrayList<WeightedTree> trees;
    private final Classifier tieResolver;
    private final int[] featureUsages;

    /**
     * Constructs the forest.
     * @param d training data
     * @param numberOfTrees number of trees
     * @param subspaceFraction fraction of features that forms the subspace
     * @param treeBuilder builds MurTree
     * @param tieResolver classifier to resolve ties
     */
    public SoftRestrictedRSMRandomForest(DataSet d, int numberOfTrees, double subspaceFraction,
                                         MurTreeBuilder treeBuilder, Classifier tieResolver) {
        this.trees = new ArrayList<>(numberOfTrees);
        this.featureUsages = new int[d.getNumberOfFeatures()];
        this.tieResolver = tieResolver;

        Arrays.fill(featureUsages, 1);

        for (int i = 0; i < numberOfTrees; i++) {
            List<Integer> subspace = generateRandomSubspaceDependent(d.getNumberOfFeatures(), subspaceFraction);
            DataSet subspaceData = d.getSubspaceData(subspace);

            DecisionTree newTree = treeBuilder.build(subspaceData);
            trees.add(new WeightedTree(newTree, 1, subspace));

            Set<Integer> consideredFeatures = newTree.getConsideredFeatures();
            for (int j : consideredFeatures) {
                featureUsages[j]++;
            }
        }
    }

    private List<Integer> generateRandomSubspaceDependent(int numberOfFeatures, double subspaceFraction) {
        int subspaceSize = (int) round(subspaceFraction * numberOfFeatures);
        List<Integer> subspace = new ArrayList<>(subspaceSize);

        ArrayList<Pair<Integer, Double>> subspaceIntsWithWeights = new ArrayList<>(numberOfFeatures);

        int totalUsage = sum(featureUsages);

        for (int i = 0; i < numberOfFeatures; i++) {
            subspaceIntsWithWeights.add(i, new Pair<>(i, totalUsage - featureUsages[i] + 0.0));
        }

        for (int i = 0; i < subspaceSize; i++) {
            subspace.add(sampleOne(subspaceIntsWithWeights));
        }

        Collections.sort(subspace);

        return subspace;
    }

    /**
     * Given a list of pairs, where a pair represents (Element, relative probability),
     * one element is sampled and removed from the list. This takes the relative probabilities into account.
     * @param elements list of pairs
     * @return one random element
     */
    public static int sampleOne(ArrayList<Pair<Integer, Double>> elements) {
        ArrayList<Pair<Integer, Double>> rouletteWheel = toRouletteWheel(elements);

        double rand = ThreadLocalRandom.current().nextDouble();
        double lb = 0.0;
        double ub;

        for (int i = 0; i < rouletteWheel.size(); i++) {
            ub = rouletteWheel.get(i).getValue1();

            if (rand > lb && rand < ub) {
                int result = elements.get(i).getValue0();
                elements.remove(i);
                return result;
            }

            lb = ub;
        }
        return 0;
    }

    private static ArrayList<Pair<Integer, Double>> toRouletteWheel(ArrayList<Pair<Integer, Double>> elements) {
        double totalWeight = 0.0;

        for (Pair<Integer, Double> el : elements) {
            totalWeight += el.getValue1();
        }

        ArrayList<Pair<Integer, Double>> result = new ArrayList<>(elements.size());
        double previousProbability = 0.0;

        for (int i = 0; i < elements.size(); i++) {
            Pair<Integer, Double> el = elements.get(i);
            int subspaceInt = el.getValue0();
            double probability = el.getValue1() / totalWeight;

            result.add(i, new Pair<>(subspaceInt, previousProbability + probability));

            previousProbability += probability;
        }

        return result;
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
