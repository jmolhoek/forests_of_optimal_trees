import Data.DataSet;
import Data.Record;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.pow;

/**
 * Builds an optimal decision tree following the MurTree algorithm (Demirovic et al. 2020).
 */
public class MurTreeBuilder implements ClassifierBuilder {
    private int depth;
    private int numberOfNodes;

    /**
     * Constructor for MurTreeBuilder.
     */
    public MurTreeBuilder() {
        depth = 0;
        numberOfNodes = 0;
    }

    /**
     * Set the max depth. Depth only counts predicate nodes, not leaves/classification nodes.
     * @param depth max depth of resulting tree
     * @return this
     */
    public MurTreeBuilder withDepth(int depth) {
        this.depth = depth;
        return this;
    }

    /**
     * Set the max number of predicate nodes.
     * @param numberOfNodes max number of predicate nodes
     * @return this
     */
    public MurTreeBuilder withMaxNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
        return this;
    }

    /**
     * Getter for depth.
     * @return max depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Getter for number of nodes.
     * @return max number of nodes
     */
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    /**
     * Builds the optimal decision tree with the given restrictions.
     * @param data training data-set
     * @return optimal decision tree
     */
    public DecisionTree build(DataSet data) {
        try {
            return optimalTree(data, depth, numberOfNodes).getValue0();
        } catch (Infeasible | InterruptedException infeasible) {
            infeasible.printStackTrace();
            return null;
        }
    }

    /**
     * Inline helper class to represent trees without building them.
     */
    private static class TreeRepresentative {
        private double misclassifications;
        private int feature;
        private int classifyLeft;
        private int classifyRight;

        private TreeRepresentative() {
            misclassifications = Double.MAX_VALUE;
            feature = -1;
            classifyLeft = -1;
            classifyRight = -1;
        }
    }

    protected static ArrayList<Integer> getCopyOfBranchAndSort(List<Integer> branch) {
        ArrayList<Integer> b = new ArrayList<>(branch.size());
        b.addAll(branch);
        Collections.sort(b);
        return b;
    }

    /**
     * Cache for memoization.
     */
    private static class Cache {
        private final ArrayList<HashMap<ArrayList<Integer>, HashMap<Integer, Pair<DecisionTree, Double>>>> mem;
        private final int maxDepth;

        /**
         * Constructor for the cache.
         * @param maxDepth maximum depth of the tree
         */
        private Cache(int maxDepth) {
            this.maxDepth = maxDepth;
            if (maxDepth > 2) {
                mem = new ArrayList<>(maxDepth - 1);

                for (int i = 0; i < maxDepth - 1; i++) {
                    mem.add(new HashMap<>());
                }
            }
            else {
                mem = new ArrayList<>();
            }
        }

        /**
         * Calculates where in the mem-list the relevant data is stored.
         * d=3 trees are stored on index 0.
         * @param branch branch
         * @return index of the location
         */
        private int memIndex(List<Integer> branch) {
            return maxDepth - branch.size() - 2;
        }

        /**
         * Finds the relevant result if it exists. If it does not exist yet, an exception is thrown.
         * @param branch branch
         * @param maxNumberOfNodes maximum number of nodes
         * @return relevant pair of respectively the tree and its absolute error on the training set
         * @throws Exception if the tree is small and the specialised methods can handle it, or if no result is found in the cache
         */
        private Pair<DecisionTree, Double> find(ArrayList<Integer> branch, int maxNumberOfNodes) throws Exception {
            if (maxDepth <= 2) throw new Exception("Small tree, no caching needed.");

            ArrayList<Integer> b = getCopyOfBranchAndSort(branch);

            HashMap<ArrayList<Integer>, HashMap<Integer, Pair<DecisionTree, Double>>> hashmap = mem.get(memIndex(b));
            HashMap<Integer, Pair<DecisionTree, Double>> queryResult = hashmap.get(b);

            if (queryResult == null) throw new Exception("No element found");

            Pair<DecisionTree, Double> res = queryResult.get(maxNumberOfNodes);

            if (res == null) throw new Exception("No element found");

            return res;
        }

        /**
         * Adds the calculated tree to the cache.
         * @param branch branch
         * @param newItem the calculated result that needs to be stored
         * @param maxNumberOfNodes maximum number of nodes
         */
        private void add(ArrayList<Integer> branch, Pair<DecisionTree, Double> newItem, Integer maxNumberOfNodes) {
            if (maxDepth > 2) {
                ArrayList<Integer> b = getCopyOfBranchAndSort(branch);

                HashMap<ArrayList<Integer>, HashMap<Integer, Pair<DecisionTree, Double>>> hashmap = mem.get(memIndex(b));
                hashmap.computeIfAbsent(b, k -> new HashMap<>());

                HashMap<Integer, Pair<DecisionTree, Double>> queryResult = hashmap.get(b);

                // from the size of the tree until the restriction of the max number of nodes, this is the best solution.
                for (int i = newItem.getValue0().numberOfPredicateNodes(); i <= maxNumberOfNodes; i++) {
                    queryResult.put(i, newItem);
                }
            }
        }
    }

    /**
     * In one loop over the dataset (so O(N)), calculates the size of D^+, D^-, all individual frequency counters and
     * all pairwise frequency counters.
     *
     * FQ^+(i) is accessed by res.getValue2()[1][i]
     *      In the case where weights are not all 1, this is not FQ^+(i), but this is the sum of all weights
     *      of positive instances for which i == true
     * FQ^-(i) is accessed by res.getValue2()[0][i]
     *      In the case where weights are not all 1, this is not FQ^-(i), but this is the sum of all weights
     *      of negative instances for which i == true
     *
     * FQ^+(i,j) is accessed by res.getValue3()[1][i][j]
     *      In the case where weights are not all 1, this is not FQ^+(i,j), but this is the sum of all weights
     *      of positive instances for which i == j == true
     * FQ^-(i,j) is accessed by res.getValue3()[0][i][j]
     *      In the case where weights are not all 1, this is not FQ^-(i,j), but this is the sum of all weights
     *      of negative instances for which i == j == true
     *
     * Watch out that i and j need to be swapped if j > i
     *
     * @param data the dataset
     * @return <positiveInstances, negativeInstances, individual frequency counters, pairwise frequency counters>
     */
    protected static Quartet<Double, Double, double[][], double[][][]> calculateFrequenciesWeightedInstances(DataSet data) {
        double[][] individualResult = new double[2][data.getNumberOfFeatures()];
        double[][][] pairWiseResult = new double[2][data.getNumberOfFeatures()][data.getNumberOfFeatures()];
        double positiveInstanceWeights = 0.0;
        double negativeInstanceWeights = 0.0;

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);

            double classificationWeight = r.getWeight();

            int posOrNeg;
            if (r.getActualClass() == 0) {
                posOrNeg = 0;
                negativeInstanceWeights += classificationWeight;
            }
            else {
                posOrNeg = 1;
                positiveInstanceWeights += classificationWeight;
            }

            for (int j1 = 0; j1 < r.getNumberOfFeatures(); j1++) {
                if (r.checkPredicate(j1)) {
                    individualResult[posOrNeg][j1] += classificationWeight;
                }
                for (int j2 = 0; j2 < j1; j2++) {
                    if (r.checkPredicate(j1) && r.checkPredicate(j2)) {
                        pairWiseResult[posOrNeg][j1][j2] += classificationWeight;
                    }
                }
            }
        }

        return new Quartet<>(positiveInstanceWeights, negativeInstanceWeights, individualResult, pairWiseResult);
    }

    /**
     * Watch out, this function does a lot. It does only one pass over the data. From that it calculates:
     *
     *     optimal tree of size 0,
     *     misclassification score of the optimal tree of size 0,
     *     optimal tree of size 1,
     *     misclassification score of the optimal tree of size 1,
     *     optimal tree of size 2,
     *     misclassification score of the optimal tree of size 2,
     *     optimal tree of size 3 where the depth is 2 at max,
     *     misclassification score of the optimal tree of size 3,
     *
     * @param data the dataset for which a tree needs to be calcukated
     * @return The eight values described above, in that order
     */
    private static Octet<DecisionTree, Double, DecisionTree, Double, DecisionTree, Double, DecisionTree, Double>
                                                constructZeroOneTwoThree_maxDepthTwo(DataSet data) {
        int numberOfFeatures = data.getNumberOfFeatures();

        Quartet<Double, Double, double[][], double[][][]> frequencyCalculator = calculateFrequenciesWeightedInstances(data);
        double positiveInstanceWeights = frequencyCalculator.getValue0();
        double negativeInstanceWeights = frequencyCalculator.getValue1();
        double[][] individualFrequencies = frequencyCalculator.getValue2();
        double[][][] pairwiseFrequencies = frequencyCalculator.getValue3();

        TreeRepresentative[] bestLeftSubtree = new TreeRepresentative[numberOfFeatures];
        TreeRepresentative[] bestRightSubtree = new TreeRepresentative[numberOfFeatures];

        for (int i = 0; i < numberOfFeatures; i++) {
            bestLeftSubtree[i] = new TreeRepresentative();
            bestRightSubtree[i] = new TreeRepresentative();
        }

        for (int i = 0; i < numberOfFeatures; i++) {
            for (int j = 0; j < numberOfFeatures; j++) {
                if (i != j) {
                    // swap i and j if j > i
                    // formulae come from section 4 of the MurTree paper
                    // take i as root and j as left child
                    double w_plus_ni_j = individualFrequencies[1][j] - (j > i ? pairwiseFrequencies[1][j][i] : pairwiseFrequencies[1][i][j]);
                    double w_minus_ni_j = individualFrequencies[0][j] - (j > i ? pairwiseFrequencies[0][j][i] : pairwiseFrequencies[0][i][j]);
                    double w_plus_ni_nj = positiveInstanceWeights - individualFrequencies[1][i] - individualFrequencies[1][j] + (j > i ? pairwiseFrequencies[1][j][i] : pairwiseFrequencies[1][i][j]);
                    double w_minus_ni_nj = negativeInstanceWeights - individualFrequencies[0][i] - individualFrequencies[0][j] + (j > i ? pairwiseFrequencies[0][j][i] : pairwiseFrequencies[0][i][j]);

                    double cs_ni_j = min(w_plus_ni_j,w_minus_ni_j);
                    double cs_ni_nj = min(w_plus_ni_nj,w_minus_ni_nj);

                    double ms_left_ij = cs_ni_nj + cs_ni_j;
                    if (bestLeftSubtree[i].misclassifications > ms_left_ij) {
                        bestLeftSubtree[i].misclassifications = ms_left_ij;
                        bestLeftSubtree[i].feature = j;

                        if (w_plus_ni_j > w_minus_ni_j) {
                            bestLeftSubtree[i].classifyRight = 1;
                        }
                        else {
                            bestLeftSubtree[i].classifyRight = 0;
                        }

                        if (w_plus_ni_nj > w_minus_ni_nj) {
                            bestLeftSubtree[i].classifyLeft = 1;
                        }
                        else {
                            bestLeftSubtree[i].classifyLeft = 0;
                        }
                    }


                    // take i as root and j as right child
                    double w_plus_i_nj = individualFrequencies[1][i] - (j > i ? pairwiseFrequencies[1][j][i] : pairwiseFrequencies[1][i][j]);
                    double w_minus_i_nj = individualFrequencies[0][i] - (j > i ? pairwiseFrequencies[0][j][i] : pairwiseFrequencies[0][i][j]);
                    double w_plus_i_j = (j > i ? pairwiseFrequencies[1][j][i] : pairwiseFrequencies[1][i][j]);
                    double w_minus_i_j = (j > i ? pairwiseFrequencies[0][j][i] : pairwiseFrequencies[0][i][j]);

                    double cs_i_nj = min(w_plus_i_nj,w_minus_i_nj);
                    double cs_i_j = min(w_plus_i_j,w_minus_i_j);

                    double ms_right_ij = cs_i_nj + cs_i_j;
                    if (bestRightSubtree[i].misclassifications > ms_right_ij) {
                        bestRightSubtree[i].misclassifications = ms_right_ij;
                        bestRightSubtree[i].feature = j;

                        // determine what the classifications are
                        if (w_plus_i_j > w_minus_i_j) {
                            bestRightSubtree[i].classifyRight = 1;
                        }
                        else {
                            bestRightSubtree[i].classifyRight = 0;
                        }

                        if (w_plus_i_nj > w_minus_i_nj) {
                            bestRightSubtree[i].classifyLeft = 1;
                        }
                        else {
                            bestRightSubtree[i].classifyLeft = 0;
                        }
                    }
                }
            }
        }

        //
        //
        // Calculate the optimal size-3 (depth two) tree
        //
        //

        int bestRoot = Integer.MAX_VALUE;
        double currentRootMS = Double.MAX_VALUE;

        for (int i = 0; i < numberOfFeatures; i++) {
            double ms = bestLeftSubtree[i].misclassifications + bestRightSubtree[i].misclassifications;
            if (ms < currentRootMS) {
                bestRoot = i;
                currentRootMS = ms;
            }
        }

        int leftPredicate = bestLeftSubtree[bestRoot].feature;
        int rightPredicate = bestRightSubtree[bestRoot].feature;

        DecisionTree optimalDepthTwoSizeThree =  new PredicateNode(
                bestRoot,
                new PredicateNode(leftPredicate, new ClassificationNode(bestLeftSubtree[bestRoot].classifyLeft), new ClassificationNode(bestLeftSubtree[bestRoot].classifyRight)),
                new PredicateNode(rightPredicate, new ClassificationNode(bestRightSubtree[bestRoot].classifyLeft), new ClassificationNode(bestRightSubtree[bestRoot].classifyRight))
        );
        double optimalDepthTwoSizeThree_misclassification = currentRootMS;

        //
        //
        // Now that we already have all the information available, it is possible to calculate the optimal tree with two nodes in O(#features) (aka almost instantly)
        //
        //

        bestRoot = Integer.MAX_VALUE;
        currentRootMS = Double.MAX_VALUE;

        for (int i = 0; i < numberOfFeatures; i++) {
            // consider putting the best left tree
            double msLeftTree = bestLeftSubtree[i].misclassifications + min(individualFrequencies[1][i], individualFrequencies[0][i]);

            // consider putting the best right tree
            double msRightTree = bestRightSubtree[i].misclassifications + min(positiveInstanceWeights - individualFrequencies[1][i], negativeInstanceWeights - individualFrequencies[0][i]);

            double ms = min(msLeftTree, msRightTree);

            if (ms < currentRootMS) {
                bestRoot = i;
                currentRootMS = ms;
            }
        }

        double optimalDepthTwoSizeTwo_misclassification = currentRootMS;

        DecisionTree optimalDepthTwoSizeTwo;
        if (currentRootMS == bestLeftSubtree[bestRoot].misclassifications + min(individualFrequencies[1][bestRoot], individualFrequencies[0][bestRoot])) {
            // we now know that we need to construct a Predicate-node on the left side
            int rightClassification = individualFrequencies[1][bestRoot] > individualFrequencies[0][bestRoot] ? 1 : 0;
            optimalDepthTwoSizeTwo = new PredicateNode(
                    bestRoot,
                    new PredicateNode(bestLeftSubtree[bestRoot].feature, new ClassificationNode(bestLeftSubtree[bestRoot].classifyLeft), new ClassificationNode(bestLeftSubtree[bestRoot].classifyRight)),
                    new ClassificationNode(rightClassification)
            );
        }
        else {
            // we now know that we need to construct a Predicate-node on the right side
            int leftClassification = (positiveInstanceWeights - individualFrequencies[1][bestRoot]) > (negativeInstanceWeights - individualFrequencies[0][bestRoot]) ? 1 : 0;
            optimalDepthTwoSizeTwo = new PredicateNode(
                    bestRoot,
                    new ClassificationNode(leftClassification),
                    new PredicateNode(bestRightSubtree[bestRoot].feature, new ClassificationNode(bestRightSubtree[bestRoot].classifyLeft), new ClassificationNode(bestRightSubtree[bestRoot].classifyRight))
            );
        }

        //
        //
        // Now that we already have all the information available, it is possible to calculate the optimal tree with one node in O(#features) (aka almost instantly)
        //
        //
        DecisionTree optimalSizeOne = null;
        double optimalSizeOne_misclassifications = Double.MAX_VALUE;

        for (int i = 0; i < numberOfFeatures; i++) {
            double ms = min(positiveInstanceWeights - individualFrequencies[1][i], negativeInstanceWeights - individualFrequencies[0][i]) + min(individualFrequencies[1][i], individualFrequencies[0][i]);

            if (ms < optimalSizeOne_misclassifications) {
                DecisionTree classifyLeft, classifyRight;

                if (positiveInstanceWeights - individualFrequencies[1][i] > negativeInstanceWeights - individualFrequencies[0][i]) {
                    classifyLeft = new ClassificationNode(1);
                }
                else {
                    classifyLeft = new ClassificationNode(0);
                }

                if (individualFrequencies[1][i] > individualFrequencies[0][i]) {
                    classifyRight = new ClassificationNode(1);
                }
                else {
                    classifyRight = new ClassificationNode(0);
                }


                optimalSizeOne = new PredicateNode(i, classifyLeft, classifyRight);
                optimalSizeOne_misclassifications = ms;
            }
        }

        //
        //
        // Now that we already have all the information available, it is possible to calculate the optimal tree with zero nodes in O(1)
        //
        //
        DecisionTree optimalSizeZero;
        double optimalSizeZero_misclassifications;

        if (positiveInstanceWeights > negativeInstanceWeights) {
            optimalSizeZero = new ClassificationNode(1);
            optimalSizeZero_misclassifications = negativeInstanceWeights;
        }
        else {
            optimalSizeZero = new ClassificationNode(0);
            optimalSizeZero_misclassifications = positiveInstanceWeights;
        }

        return new Octet<>(
                optimalSizeZero,
                optimalSizeZero_misclassifications,
                optimalSizeOne,
                optimalSizeOne_misclassifications,
                optimalDepthTwoSizeTwo,
                optimalDepthTwoSizeTwo_misclassification,
                optimalDepthTwoSizeThree,
                optimalDepthTwoSizeThree_misclassification
        );
    }


    private static Octet<DecisionTree, Double, DecisionTree, Double, DecisionTree, Double, DecisionTree, Double>
        prune(Octet<DecisionTree, Double, DecisionTree, Double, DecisionTree, Double, DecisionTree, Double> inp) {

        DecisionTree optimalSizeZero = inp.getValue0();
        double optimalSizeZero_misclassifications = inp.getValue1();
        DecisionTree optimalSizeOne = inp.getValue2();
        double optimalSizeOne_misclassifications = inp.getValue3();
        DecisionTree optimalDepthTwoSizeTwo = inp.getValue4();
        double optimalDepthTwoSizeTwo_misclassification = inp.getValue5();
        DecisionTree optimalDepthTwoSizeThree = inp.getValue6();
        double optimalDepthTwoSizeThree_misclassification = inp.getValue7();

        // prune if possible

        if (optimalSizeZero_misclassifications <= optimalSizeOne_misclassifications) {
            optimalSizeOne = optimalSizeZero;
            optimalSizeOne_misclassifications = optimalSizeZero_misclassifications;
        }
        if (optimalSizeOne_misclassifications <= optimalDepthTwoSizeTwo_misclassification) {
            optimalDepthTwoSizeTwo = optimalSizeOne;
            optimalDepthTwoSizeTwo_misclassification = optimalSizeOne_misclassifications;
        }
        if (optimalDepthTwoSizeTwo_misclassification <= optimalDepthTwoSizeThree_misclassification) {
            optimalDepthTwoSizeThree = optimalDepthTwoSizeTwo;
            optimalDepthTwoSizeThree_misclassification = optimalDepthTwoSizeTwo_misclassification;
        }

        return new Octet<>(
                optimalSizeZero,
                optimalSizeZero_misclassifications,
                optimalSizeOne,
                optimalSizeOne_misclassifications,
                optimalDepthTwoSizeTwo,
                optimalDepthTwoSizeTwo_misclassification,
                optimalDepthTwoSizeThree,
                optimalDepthTwoSizeThree_misclassification
        );
    }

    private static Triplet<DecisionTree, Double, Integer> best(Triplet<DecisionTree, Double, Integer> a, Triplet<DecisionTree, Double, Integer> b) {
        // a has a lower misclassification score
        if (a.getValue1() < b.getValue1()) {
            return a;
        }
        // same misclassification score, but a is smaller
        else if (a.getValue1().equals(b.getValue1()) && a.getValue2() < b.getValue2()) {
            return a;
        }
        // else b is better
        else {
            return b;
        }
    }

    /**
     * Returns respectively the optimal binary decision tree for the given data, and the number of misclassifications.
     * Given that all the weights are 1, the second number is the absolute number of misclassifications. Otherwise it
     * is the sum of weights of misclassified instances.
     *
     * @param data the training data-set
     * @param depth maximum depth of the tree (only counting predicate nodes, not leaves
     * @param numberOfNodes maximum number of predicate nodes
     * @return (optimal binary decision tree, number of misclassifications on training data)
     * @throws Infeasible if depth < 0 or numberOfNodes < 0
     * @throws InterruptedException if interrupted
     */
    public static Pair<DecisionTree, Double> optimalTree(DataSet data, int depth, int numberOfNodes) throws Infeasible, InterruptedException {
        if (depth < 0 || numberOfNodes < 0) throw new Infeasible();
        else return constructOptimalTree(data, depth, numberOfNodes, new Cache(depth), new ArrayList<>(depth));
    }

    protected static Pair<ArrayList<Integer>, ArrayList<Integer>> nextBranches(ArrayList<Integer> branch, int predicate) {
        ArrayList<Integer> bl = new ArrayList<>(branch.size());
        ArrayList<Integer> br = new ArrayList<>(branch.size());

        bl.addAll(branch);
        br.addAll(branch);

        bl.add(2*predicate);
        br.add(2*predicate+1);

        return new Pair<>(bl, br);
    }

    private static Pair<DecisionTree, Double> constructOptimalTree(DataSet data, int depth, int numberOfNodes, Cache cache, ArrayList<Integer> branch) throws Infeasible, InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        int numberOfFeatures = data.getNumberOfFeatures();

        //
        // base cases
        //
        if (depth < 0 || numberOfNodes < 0)     throw new Infeasible();
        if (numberOfNodes > pow(2, depth) - 1)  return constructOptimalTree(data, depth, (int) pow(2, depth) - 1, cache, branch);
        if (depth > numberOfNodes)              return constructOptimalTree(data, numberOfNodes, numberOfNodes, cache, branch);
        if (data.getSize() == 0)                throw new Infeasible();

        //
        // Memoization
        //
        try {
            return cache.find(branch, numberOfNodes);
        } catch (Exception ignored) {}
        // not found: run the actual algorithm

        //
        // Specialised method
        //
        if (depth <= 2) {
            // Use specialised algorithm, also prune (without loss of optimality)
            Octet<DecisionTree, Double, DecisionTree, Double, DecisionTree, Double, DecisionTree, Double> query = prune(constructZeroOneTwoThree_maxDepthTwo(data));

            // return the appropriate tree
            if (depth == 0 || depth == 1) {
                Pair<DecisionTree, Double> result = new Pair<>((DecisionTree) query.getValue(depth * 2), (Double) query.getValue(depth * 2 + 1));
                cache.add(branch, result, numberOfNodes);
                return result;
            }
            else if (numberOfNodes == 2) {
                Pair<DecisionTree, Double> result = new Pair<>((DecisionTree) query.getValue(4), (Double) query.getValue(5));
                cache.add(branch, result, numberOfNodes);
                return result;
            }
            else {
                Pair<DecisionTree, Double> result = new Pair<>((DecisionTree) query.getValue(6), (Double) query.getValue(7));
                cache.add(branch, result, numberOfNodes);
                return result;
            }
        }

        //
        // general case
        //

        else {
            if (data.isPure()) return new Pair<>(new ClassificationNode(data.getEntry(0).getActualClass()), (double)data.getSize());

            // Final solution, best so far
            Triplet<DecisionTree, Double, Integer> best = new Triplet<>(null, Double.MAX_VALUE, numberOfNodes);

            // We can deduce the size boundaries on the subtrees
            int max_size_subtree = min((int) pow(2, depth - 1) - 1, numberOfNodes - 1);
            int min_size_subtree = numberOfNodes - 1 - max_size_subtree;

            // Consider every feature for a split
            for (int i = 0; i < numberOfFeatures; i++) {
                // Split the data on feature i
                Pair<DataSet, DataSet> tmp = data.splitOnAttribute(i);
                DataSet iTrue = tmp.getValue0();
                DataSet iFalse = tmp.getValue1();

                if (iTrue.getSize() == 0 || iFalse.getSize() == 0) {
                    continue;
                }

                // Check every allowed distribution of nodes
                for (int nodesToTheRight = min_size_subtree; nodesToTheRight <= max_size_subtree; nodesToTheRight++) {
                    int nodesToTheLeft = numberOfNodes - 1 - nodesToTheRight;

                    // recursively find best left and right tree (can be optimized separately), both need to be feasible
                    Pair<DecisionTree, Double> leftResult;
                    Pair<DecisionTree, Double> rightResult;
                    try {
                        Pair<ArrayList<Integer>, ArrayList<Integer>> newBranches = nextBranches(branch, i);
                        ArrayList<Integer> bl = newBranches.getValue0();
                        ArrayList<Integer> br = newBranches.getValue1();

                        leftResult = constructOptimalTree(iFalse, depth - 1, nodesToTheLeft, cache, bl);
                        rightResult = constructOptimalTree(iTrue, depth - 1, nodesToTheRight, cache, br);
                    }
                    catch (Infeasible inf) {
                        continue;
                    }

                    Triplet<DecisionTree, Double, Integer> localBest = new Triplet<>(
                            new PredicateNode(i, leftResult.getValue0(), rightResult.getValue0()),
                            leftResult.getValue1() + rightResult.getValue1(),
                            leftResult.getValue0().numberOfPredicateNodes() + rightResult.getValue0().numberOfPredicateNodes() + 1
                    );

                    // If it is better, update best
                    best = best(best, localBest);
                }
            }

            // Check if we actually have a result
            if (best.getValue1() == Double.MAX_VALUE) throw new Infeasible();

            Pair<DecisionTree, Double> result = new Pair<>(
                    best.getValue0(),
                    best.getValue1()
            );
            cache.add(branch, result, numberOfNodes);
            return result;
        }
    }
}
