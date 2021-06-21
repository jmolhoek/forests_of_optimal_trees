import Data.DataSet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.min;

/**
 * Builds a set of all optimal decision trees, following the MurTree algorithm (adapted from Demirovic et al. 2020).
 */
public class AllMurTreeBuilder implements ClassifierBuilder {
    private int depth;

    /**
     * Constuctor for AllMurTreeBuilder.
     */
    public AllMurTreeBuilder() {
        depth = 0;
    }

    /**
     * Set the max depth. Depth only counts predicate nodes, not leaves/classification nodes.
     * @param depth max depth of resulting trees
     * @return this
     */
    public AllMurTreeBuilder withDepth(int depth) {
        this.depth = depth;
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
     * Builds one optimal decision tree with the given restrictions.
     * @param data training data-set
     * @return optimal decision tree
     */
    public DecisionTree build(DataSet data) {
        try {
            return optimalTree(data, depth).getValue0();
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
        private final ArrayList<Triplet<Integer, Integer, Integer>> trees;
        // Triplet<Integer, Integer, Integer> stores: (feature, classifyLeft, classifyRight)

        private TreeRepresentative() {
            misclassifications = Double.MAX_VALUE;
            trees = new ArrayList<>();
        }
    }

    /**
     * Cache for memoization.
     *
     * The cache of the original MurTree algorithm maps a branch to an optimal tree. In this updated case, the cache maps:
     * * branches of length (maxDepth - 2) to a set of optimal decision trees of max-depth 2
     * * branches shorter than that to a list of predicates.
     *
     * At the end of the algorithm, this cache is traversed to construct the final Set of all possible decision trees.
     */
    private static class Cache {
        private final ArrayList<HashMap<ArrayList<Integer>, Pair<ArrayList<Integer>, Double>>> mem;
        private final HashMap<ArrayList<Integer>, Pair<SetOfDecisionTrees, Double>> memDepth2;
        private final int maxDepth;

        /**
         * Constructor for the cache.
         *
         * @param maxDepth maximum depth of the tree
         */
        private Cache(int maxDepth) {
            this.maxDepth = maxDepth;
            memDepth2 = new HashMap<>();
            if (maxDepth > 2) {
                mem = new ArrayList<>(maxDepth - 1);

                for (int i = 0; i < maxDepth - 1; i++) {
                    mem.add(new HashMap<>());
                }
            } else {
                mem = new ArrayList<>();
            }
        }

        /**
         * Calculates where in the mem-list the relevant data is stored.
         * d=3 trees are stored on index 0.
         *
         * @param branch branch
         * @return index of the location
         */
        private int memIndex(List<Integer> branch) {
            return maxDepth - branch.size() - 2;
        }

        /**
         * Finds the relevant result if it exists. If it does not exist (yet), an exception is thrown.
         *
         * @param branch branch
         * @return relevant pair of respectively the list of possible predicates and the absolute error on the training set
         * @throws Exception if the tree is small and the specialised methods can handle it, or if no result is found in the cache
         */
        private Pair<ArrayList<Integer>, Double> find(ArrayList<Integer> branch) throws Exception {
            if (maxDepth <= 2) throw new Exception("Small tree, no caching needed.");

            ArrayList<Integer> b = MurTreeBuilder.getCopyOfBranchAndSort(branch);

            HashMap<ArrayList<Integer>, Pair<ArrayList<Integer>, Double>> hashmap = mem.get(memIndex(b));
            Pair<ArrayList<Integer>, Double> queryResult = hashmap.get(b);

            if (queryResult == null) throw new Exception("No element found");

            return queryResult;
        }

        private void add(ArrayList<Integer> branch, Pair<ArrayList<Integer>, Double> newItems) {
            if (maxDepth > 2) {
                ArrayList<Integer> b = MurTreeBuilder.getCopyOfBranchAndSort(branch);

                HashMap<ArrayList<Integer>, Pair<ArrayList<Integer>, Double>> hashmap = mem.get(memIndex(b));

                hashmap.putIfAbsent(b, newItems);
            }
        }

        /**
         * Adds the set of optimal trees to the cache.
         * @param branch branch
         * @param newItems pair of respectively
         */
        private void addDepth2(ArrayList<Integer> branch, Pair<SetOfDecisionTrees, Double> newItems) {
            if (maxDepth > 2) {
                ArrayList<Integer> b = MurTreeBuilder.getCopyOfBranchAndSort(branch);

                memDepth2.putIfAbsent(b, newItems);
            }
        }

        /**
         *
         * @param branch branch
         * @return Pair of respectively all optimal trees for that branch
         * @throws Exception if no result is found in the cache
         */
        private Pair<SetOfDecisionTrees, Double> findDepth2(ArrayList<Integer> branch) throws Exception {
            if (maxDepth <= 2) throw new Exception("Small tree, no caching needed.");

            ArrayList<Integer> b = MurTreeBuilder.getCopyOfBranchAndSort(branch);

            Pair<SetOfDecisionTrees, Double> queryResult = memDepth2.get(b);

            if (queryResult == null) throw new Exception("No element found");

            return queryResult;
        }
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
     * @param data the dataset for which a tree needs to be calculated
     * @return The eight values described above, in that order
     */
    private static Sextet<SetOfDecisionTrees, Double, SetOfDecisionTrees, Double, SetOfDecisionTrees, Double>
                    constructZeroOneTwoThree_maxDepthTwo(DataSet data) {
        int numberOfFeatures = data.getNumberOfFeatures();

        Quartet<Double, Double, double[][], double[][][]> frequencyCalculator = MurTreeBuilder.calculateFrequenciesWeightedInstances(data);
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
                    if (bestLeftSubtree[i].misclassifications >= ms_left_ij) {
                        if (bestLeftSubtree[i].misclassifications > ms_left_ij) bestLeftSubtree[i].trees.clear();
                        bestLeftSubtree[i].misclassifications = ms_left_ij;

                        int classifyLeft;
                        int classifyRight;

                        if (w_plus_ni_j > w_minus_ni_j) {
                            classifyRight = 1;
                        }
                        else {
                            classifyRight = 0;
                        }

                        if (w_plus_ni_nj > w_minus_ni_nj) {
                            classifyLeft = 1;
                        }
                        else {
                            classifyLeft = 0;
                        }

                        bestLeftSubtree[i].trees.add(new Triplet<>(j, classifyLeft, classifyRight));
                    }


                    // take i as root and j as right child
                    double w_plus_i_nj = individualFrequencies[1][i] - (j > i ? pairwiseFrequencies[1][j][i] : pairwiseFrequencies[1][i][j]);
                    double w_minus_i_nj = individualFrequencies[0][i] - (j > i ? pairwiseFrequencies[0][j][i] : pairwiseFrequencies[0][i][j]);
                    double w_plus_i_j = (j > i ? pairwiseFrequencies[1][j][i] : pairwiseFrequencies[1][i][j]);
                    double w_minus_i_j = (j > i ? pairwiseFrequencies[0][j][i] : pairwiseFrequencies[0][i][j]);

                    double cs_i_nj = min(w_plus_i_nj,w_minus_i_nj);
                    double cs_i_j = min(w_plus_i_j,w_minus_i_j);

                    double ms_right_ij = cs_i_nj + cs_i_j;
                    if (bestRightSubtree[i].misclassifications >= ms_right_ij) {
                        if (bestRightSubtree[i].misclassifications > ms_right_ij) bestRightSubtree[i].trees.clear();
                        bestRightSubtree[i].misclassifications = ms_right_ij;

                        int classifyLeft;
                        int classifyRight;

                        // determine what the classifications are
                        if (w_plus_i_j > w_minus_i_j) {
                            classifyRight = 1;
                        }
                        else {
                            classifyRight = 0;
                        }

                        if (w_plus_i_nj > w_minus_i_nj) {
                            classifyLeft = 1;
                        }
                        else {
                            classifyLeft = 0;
                        }

                        bestRightSubtree[i].trees.add(new Triplet<>(j, classifyLeft, classifyRight));
                    }
                }
            }
        }

        //
        //
        // Calculate the optimal size-3 (depth two) tree
        //
        //

        ArrayList<Integer> bestRoots_2_3 = new ArrayList<>();
        double currentBestMS = Double.MAX_VALUE;

        for (int i = 0; i < numberOfFeatures; i++) {
            double ms = bestLeftSubtree[i].misclassifications + bestRightSubtree[i].misclassifications;
            if (ms < currentBestMS) {
                bestRoots_2_3.clear();
                bestRoots_2_3.add(i);
                currentBestMS = ms;
            } else if (ms == currentBestMS) {
                bestRoots_2_3.add(i);
            }
        }

        SetOfDecisionTrees optimalDepthTwoSizeThree = new SetOfDecisionTrees();
        double optimalDepthTwoSizeThree_misclassification = currentBestMS;

        for (int root : bestRoots_2_3) {
            for (Triplet<Integer, Integer, Integer> left : bestLeftSubtree[root].trees) {
                for (Triplet<Integer, Integer, Integer> right : bestRightSubtree[root].trees) {
                    // left tree has redundant predicate node
                    if (left.getValue1().equals(left.getValue2())) {
                        // right tree also has redundant predicate node
                        if (right.getValue1().equals(right.getValue2())) {
                            // left and right are EQUALLY CLASSIFYING redundant predicate nodes
                            if (left.getValue1().equals(right.getValue1())) {
                                optimalDepthTwoSizeThree.add(
                                        new ClassificationNode(left.getValue1())
                                );
                            }
                            // left and right are DIFFERENTLY CLASSIFYING redundant predicate nodes
                            else {
                                optimalDepthTwoSizeThree.add(
                                        new PredicateNode(
                                                root,
                                                new ClassificationNode(left.getValue1()),
                                                new ClassificationNode(right.getValue1())
                                        )
                                );
                            }
                        }
                        // right tree has no redundant predicate node (but left does)
                        else {
                            optimalDepthTwoSizeThree.add(
                                    new PredicateNode(
                                            root,
                                            new ClassificationNode(left.getValue1()),
                                            new PredicateNode(right.getValue0(), new ClassificationNode(right.getValue1()), new ClassificationNode(right.getValue2()))
                                    )
                            );
                        }
                    }
                    // right tree has redundant predicate node (left does not)
                    else if (right.getValue1().equals(right.getValue2())) {
                        optimalDepthTwoSizeThree.add(
                                new PredicateNode(
                                        root,
                                        new PredicateNode(left.getValue0(), new ClassificationNode(left.getValue1()), new ClassificationNode(left.getValue2())),
                                        new ClassificationNode(right.getValue1())
                                )
                        );
                    }
                    // both have no redundant predicate node
                    else {
                        optimalDepthTwoSizeThree.add(
                                new PredicateNode(
                                        root,
                                        new PredicateNode(left.getValue0(), new ClassificationNode(left.getValue1()), new ClassificationNode(left.getValue2())),
                                        new PredicateNode(right.getValue0(), new ClassificationNode(right.getValue1()), new ClassificationNode(right.getValue2()))
                                )
                        );
                    }
                }
            }
        }

        //
        //
        // Now that we already have all the information available, it is possible to calculate the optimal tree with one node in O(#features) (aka almost instantly)
        //
        //
        SetOfDecisionTrees optimalSizeOne = new SetOfDecisionTrees();
        double optimalSizeOne_misclassifications = Double.MAX_VALUE;

        for (int i = 0; i < numberOfFeatures; i++) {
            double ms = min(positiveInstanceWeights - individualFrequencies[1][i], negativeInstanceWeights - individualFrequencies[0][i]) + min(individualFrequencies[1][i], individualFrequencies[0][i]);

            if (ms < optimalSizeOne_misclassifications) {
                optimalSizeOne.clear();
            }
            if (ms <= optimalSizeOne_misclassifications) {
                ClassificationNode classifyLeft, classifyRight;

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

                if (classifyLeft.getAssignClass() == classifyRight.getAssignClass()) {
                    optimalSizeOne.add(classifyLeft);
                }
                else {
                    optimalSizeOne.add(new PredicateNode(i, classifyLeft, classifyRight));
                }

                optimalSizeOne_misclassifications = ms;
            }
        }

        //
        //
        // Now that we already have all the information available, it is possible to calculate the optimal tree with zero nodes in O(1)
        //
        //
        SetOfDecisionTrees optimalSizeZero = new SetOfDecisionTrees();
        double optimalSizeZero_misclassifications;

        if (positiveInstanceWeights == negativeInstanceWeights) {
            optimalSizeZero.add(new ClassificationNode(1));
            optimalSizeZero.add(new ClassificationNode(0));
            optimalSizeZero_misclassifications = negativeInstanceWeights;
        }
        else if (positiveInstanceWeights > negativeInstanceWeights) {
            optimalSizeZero.add(new ClassificationNode(1));
            optimalSizeZero_misclassifications = negativeInstanceWeights;
        }
        else {
            optimalSizeZero.add(new ClassificationNode(0));
            optimalSizeZero_misclassifications = positiveInstanceWeights;
        }

        return new Sextet<>(
                optimalSizeZero,
                optimalSizeZero_misclassifications,
                optimalSizeOne,
                optimalSizeOne_misclassifications,
                optimalDepthTwoSizeThree,
                optimalDepthTwoSizeThree_misclassification
        );
    }

    /**
     * Returns respectively the optimal binary decision tree for the given data, and the number of misclassifications.
     * Given that all the weights are 1, the second number is the absolute number of misclassifications. Otherwise it
     * is the sum of weights of misclassified instances.
     *
     * @param data the training data-set
     * @param depth maximum depth of the tree (only counting predicate nodes, not leaves
     * @return (optimal binary decision tree, number of misclassifications on training data)
     * @throws Infeasible if depth < 0 or numberOfNodes < 0
     * @throws InterruptedException if interrupted
     */
    public static Pair<DecisionTree, Double> optimalTree(DataSet data, int depth) throws Infeasible, InterruptedException {
        if (depth < 0) throw new Infeasible();
        else if (depth < 3) {
            // Use specialised algorithm
            Pair<SetOfDecisionTrees, Double> result = specialisedAlgorithm(data, depth);

            return new Pair<>(
                    result.getValue0().getItems().get(0),
                    result.getValue1()
            );
        }
        else {
            // Note that the algorithm does not return a list of optimal trees. This is on purpose.
            // The algorithm returns only the misclassification score, and it fills the cache. From this cache,
            // a list of all optimal trees can be generated, but it is also possible to just generate one tree (much faster).
            Cache cache = new Cache(depth);
            Double res = constructOptimalTrees(data, depth, cache, new ArrayList<>(depth));

            DecisionTree tree = null;
            try {
                tree = constructTreeFromCache(cache, depth, new ArrayList<>());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Pair<>(
                    tree,
                    res
            );
        }
    }

    /**
     * Returns a list of all optimal decision trees. Note that a list is not a set, so it can contain some duplicates.
     * The advantage is that it is faster than generating a set.
     * @param data training data
     * @param depth max depth
     * @return list of optimal trees and misclassification score
     * @throws Infeasible if parameters are infeasible (like depth < 0)
     * @throws InterruptedException if interrupted
     */
    public static Pair<ArrayList<DecisionTree>, Double> optimalTrees(DataSet data, int depth) throws Infeasible, InterruptedException {
        if (depth < 0) throw new Infeasible();
        else if (depth < 3) {
            // Use specialised algorithm
            Pair<SetOfDecisionTrees, Double> result = specialisedAlgorithm(data, depth);

            return new Pair<>(
                    result.getValue0().getItems(),
                    result.getValue1()
            );
        }
        else {
            Cache cache = new Cache(depth);

            // Note that the algorithm does not return a list of optimal trees. This is on purpose.
            // The algorithm returns only the misclassification score, and it fills the cache. From this cache,
            // a list of all optimal trees is generated.
            Double res = constructOptimalTrees(data, depth, cache, new ArrayList<>(depth));

            // Generate the list of decision trees from the cache!!
            // Note that it is a list of trees instead of a SetOfDecisionTrees. This is because otherwise too many equality-checks need to be done and the runtime goes through the roof.
            ArrayList<DecisionTree> trees = null;
            try {
                trees = constructTreeListFromCache(cache, depth, new ArrayList<>());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Pair<>(
                    trees,
                    res
            );
        }
    }

    /**
     * Returns a set of all optimal decision trees. A set does not contain duplicates.
     *
     * @param data training data
     * @param depth max depth
     * @return set of optimal trees and misclassification score
     * @throws Infeasible if parameters are infeasible (like depth < 0)
     * @throws InterruptedException if interrupted
     */
    public static Pair<SetOfDecisionTrees, Double> optimalTreesSet(DataSet data, int depth) throws Infeasible, InterruptedException {
        if (depth < 0) throw new Infeasible();
        else if (depth < 3) {
            // Use specialised algorithm
            Pair<SetOfDecisionTrees, Double> result = specialisedAlgorithm(data, depth);

            return new Pair<>(
                    result.getValue0(),
                    result.getValue1()
            );
        }
        else {
            Cache cache = new Cache(depth);

            // Note that the algorithm does not return a list of optimal trees. This is on purpose.
            // The algorithm returns only the misclassification score, and it fills the cache. From this cache,
            // a list of all optimal trees is generated.
            Double res = constructOptimalTrees(data, depth, cache, new ArrayList<>(depth));

            // Generate the list of decision trees from the cache!!
            // Note that it is a list of trees instead of a SetOfDecisionTrees. This is because otherwise too many equality-checks need to be done and the runtime goes through the roof.
            SetOfDecisionTrees trees = null;
            try {
                trees = constructTreeSetFromCache(cache, depth, new ArrayList<>());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Pair<>(
                    trees,
                    res
            );
        }
    }

    private static DecisionTree constructTreeFromCache(Cache c, int d, ArrayList<Integer> branch) throws Exception {
        if (d == 2) {
            Pair<SetOfDecisionTrees, Double> res = c.findDepth2(branch);
            return res.getValue0().getItems().get(0);
        }
        else {
            ArrayList<Integer> predicates = c.find(branch).getValue0();
            int predicate = predicates.get(0);

            Pair<ArrayList<Integer>, ArrayList<Integer>> newBranches = MurTreeBuilder.nextBranches(branch, predicate);
            ArrayList<Integer> bl = newBranches.getValue0();
            ArrayList<Integer> br = newBranches.getValue1();

            return new PredicateNode(
                    predicate,
                    constructTreeFromCache(c, d - 1, bl),
                    constructTreeFromCache(c, d - 1, br)
            );
        }
    }

    private static ArrayList<DecisionTree> constructTreeListFromCache(Cache c, int d, ArrayList<Integer> branch) throws Exception {
        if (d == 2) {
            Pair<SetOfDecisionTrees, Double> res = c.findDepth2(branch);
            return res.getValue0().getItems();
        }
        else {
            ArrayList<Integer> predicates = c.find(branch).getValue0();

            ArrayList<DecisionTree> result = new ArrayList<>();

            for (int p : predicates) {
                Pair<ArrayList<Integer>, ArrayList<Integer>> newBranches = MurTreeBuilder.nextBranches(branch, p);
                ArrayList<Integer> bl = newBranches.getValue0();
                ArrayList<Integer> br = newBranches.getValue1();

                ArrayList<DecisionTree> leftTrees = constructTreeListFromCache(c, d - 1, bl);
                ArrayList<DecisionTree> rightTrees = constructTreeListFromCache(c, d - 1, br);

                for (DecisionTree leftTree : leftTrees) {
                    for (DecisionTree rightTree : rightTrees) {
                        result.add(new PredicateNode(
                                p,
                                leftTree,
                                rightTree
                        ));
                    }
                }
            }
            return result;
        }
    }

    private static SetOfDecisionTrees constructTreeSetFromCache(Cache c, int d, ArrayList<Integer> branch) throws Exception {
        if (d == 2) {
            Pair<SetOfDecisionTrees, Double> res = c.findDepth2(branch);
            return res.getValue0();
        }
        else {
            ArrayList<Integer> predicates = c.find(branch).getValue0();

            SetOfDecisionTrees result = new SetOfDecisionTrees();

            for (int p : predicates) {
                Pair<ArrayList<Integer>, ArrayList<Integer>> newBranches = MurTreeBuilder.nextBranches(branch, p);
                ArrayList<Integer> bl = newBranches.getValue0();
                ArrayList<Integer> br = newBranches.getValue1();

                SetOfDecisionTrees leftTrees = constructTreeSetFromCache(c, d - 1, bl);
                SetOfDecisionTrees rightTrees = constructTreeSetFromCache(c, d - 1, br);

                for (DecisionTree leftTree : leftTrees) {
                    for (DecisionTree rightTree : rightTrees) {
                        result.add(new PredicateNode(
                                p,
                                leftTree,
                                rightTree
                        ));
                    }
                }
            }
            return result;
        }
    }

    private static Pair<SetOfDecisionTrees, Double> specialisedAlgorithm(DataSet data, int depth) {
        // Use specialised algorithm, also prune (without loss of optimality)
        Sextet<SetOfDecisionTrees, Double, SetOfDecisionTrees, Double, SetOfDecisionTrees, Double> query = constructZeroOneTwoThree_maxDepthTwo(data);

        // return the appropriate tree
        Pair<SetOfDecisionTrees, Double> result;
        if (depth == 0) {
            result = new Pair<>(query.getValue0(), query.getValue1());
        } else if (depth == 1) {
            result = new Pair<>(query.getValue2(), query.getValue3());
        } else {
            result = new Pair<>(query.getValue4(), query.getValue5());
        }
        return result;
    }




    private static Double constructOptimalTrees(DataSet data, int depth, Cache cache, ArrayList<Integer> branch) throws Infeasible, InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        int numberOfFeatures = data.getNumberOfFeatures();

        //
        // base cases
        //
        if (depth < 0)              throw new Infeasible();
        if (data.getSize() == 0)    throw new Infeasible();

        //
        // Memoization
        //
        try {
            if (depth == 2) return cache.findDepth2(branch).getValue1();
            else return cache.find(branch).getValue1();
        } catch (Exception ignored) {}
        // not found: run the actual algorithm

        //
        // Specialised method
        //
        if (depth <= 2) {
            // Use specialised algorithm, also prune
            Pair<SetOfDecisionTrees, Double> result = specialisedAlgorithm(data, depth);
            cache.addDepth2(branch, result);
            return result.getValue1();
        }

        //
        // general case
        //

        else {
            if (data.isPure()) {
                cache.add(branch, new Pair<>(new ArrayList<>(), (double)data.getSize()));
                return (double)data.getSize();
            }

            // Final solution, best so far
            Pair<ArrayList<Integer>, Double> best = new Pair<>(new ArrayList<>(), Double.MAX_VALUE);

            // Consider every feature for a split
            for (int i = 0; i < numberOfFeatures; i++) {
                // Split the data on feature i
                Pair<DataSet, DataSet> tmp = data.splitOnAttribute(i);
                DataSet iTrue = tmp.getValue0();
                DataSet iFalse = tmp.getValue1();

                if (iTrue.getSize() == 0 || iFalse.getSize() == 0) {
                    continue;
                }

                // recursively find best left and right tree (can be optimized separately), both need to be feasible
                Double leftResult;
                Double rightResult;
                try {
                    Pair<ArrayList<Integer>, ArrayList<Integer>> newBranches = MurTreeBuilder.nextBranches(branch, i);
                    ArrayList<Integer> bl = newBranches.getValue0();
                    ArrayList<Integer> br = newBranches.getValue1();

                    leftResult = constructOptimalTrees(iFalse, depth - 1, cache, bl);
                    rightResult = constructOptimalTrees(iTrue, depth - 1, cache, br);
                }
                catch (Infeasible inf) {
                    continue;
                }

                double localBestMS = leftResult + rightResult;

                if (Math.abs(best.getValue1() - localBestMS) < 0.0000001) {
                    best.getValue0().add(i);
                }
                else if (best.getValue1() > localBestMS) {
                    // discard local best
                    best = new Pair<>(
                            new ArrayList<>(),
                            localBestMS
                    );
                    best.getValue0().add(i);
                }
                // else discard local best

            }

            // Check if we actually have a result
            if (best.getValue1() == Double.MAX_VALUE) throw new Infeasible();

            cache.add(branch, best);
            return best.getValue1();
        }
    }
}

