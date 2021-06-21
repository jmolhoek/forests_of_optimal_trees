import Data.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Decision Tree with a weight.
 * This weight can be seen as an "amount of say".
 * Possibly there is also a subspace that was used to build the tree.
 */
public class WeightedTree extends DecisionTree {
    private final DecisionTree tree;
    private double weight;
    private final List<Integer> subspace;

    /**
     * Constructor for a tree with a weight (an "amount of say"). This constructor takes the subspace into account.
     *
     * @param tree the decision tree itself
     * @param weight weight of the tree
     * @param subspace list of columns (that the tree was allowed to use)
     */
    public WeightedTree(DecisionTree tree, double weight, List<Integer> subspace) {
        this.tree = tree;
        this.weight = weight;
        this.subspace = subspace;
    }

    /**
     * Constructor for a tree with a weight (an "amount of say").
     *
     * @param tree the decision tree itself
     * @param weight weight of the tree
     */
    public WeightedTree(DecisionTree tree, double weight) {
        this.tree = tree;
        this.weight = weight;
        this.subspace = null;
    }

    /**
     * Getter for tree.
     * @return tree
     */
    public DecisionTree getTree() {
        return tree;
    }

    /**
     * Getter for weight.
     * @return weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Getter for subspace (can be null).
     * @return subspace (can be null)
     */
    public List<Integer> getSubspace() {
        return subspace;
    }

    /**
     * Normalizes the weight, given the total of all the weights of the trees under consideration.
     * @param total total weight
     */
    public void normalizeBy(double total) {
            weight = weight / total;
        }

    @Override
    public int classify(Record record) {
        if (subspace != null) {
            Record mappedRecord = record.mapToSubSpace(subspace);
            return tree.classify(mappedRecord);
        }
        else return tree.classify(record);
    }

    @Override
    public int depth() {
        return tree.depth();
    }

    @Override
    public int numberOfNodes() {
        return tree.numberOfNodes();
    }

    @Override
    public int numberOfPredicateNodes() {
        return tree.numberOfPredicateNodes();
    }

    @Override
    public Set<Integer> getConsideredFeatures() {
        return tree.getConsideredFeatures();
    }

    @Override
    public String toString() {
        return tree.toString();
    }

    @Override
    public String prettyPrint(ArrayList<String> attributeNames, String targetName) {
        return tree.prettyPrint(attributeNames, targetName);
    }

    @Override
    protected String prettyPrint(ArrayList<String> attributeNames, String targetName, int indent) {
        return tree.prettyPrint(attributeNames, targetName, indent);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof WeightedTree)) return false;
        WeightedTree that = (WeightedTree) other;
        return tree.equals(that.tree)
                && (Math.abs(weight - that.getWeight()) < 0.0001)
                && ((subspace == null && that.subspace == null)
                    || (subspace != null && subspace.equals(that.getSubspace())));
    }

    @Override
    public List<ClassificationNode> getClassificationNodes() {
        return tree.getClassificationNodes();
    }
}
