import Data.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.max;

/**
 * Predicate nodes are non-leaf nodes in a decision tree. They contain a predicate that needs to be evaluated for a
 * given feature vector. If it evaluates to 0, take the left child. If it evaluates to 1, take the right child.
 */
public class PredicateNode extends DecisionTree {
    private int predicate;
    private DecisionTree leftTree;          // if predicate results to 0/false
    private DecisionTree rightTree;         // if predicate evaluates to 1/true

    /**
     * Constructs a predicate node with the given predicate and children.
     * @param p the predicate (evaluate f_p)
     * @param l left child
     * @param r right child
     */
    public PredicateNode (int p, DecisionTree l, DecisionTree r) {
        predicate = p;
        leftTree = l;
        rightTree = r;
    }

    /**
     * Getter for predicate.
     * @return predicate
     */
    public int getPredicate() {
        return predicate;
    }

    /**
     * Setter for predicate.
     * @param predicate new predicate
     */
    public void setPredicate(int predicate) {
        this.predicate = predicate;
    }

    /**
     * Getter for left child.
     * @return left child
     */
    public DecisionTree getLeftTree() {
        return leftTree;
    }

    /**
     * Setter for left child.
     * @param leftTree new left child
     */
    public void setLeftTree(DecisionTree leftTree) {
        this.leftTree = leftTree;
    }

    /**
     * Getter for right child.
     * @return right child
     */
    public DecisionTree getRightTree() {
        return rightTree;
    }

    /**
     * Setter for right child.
     * @param rightTree new right child
     */
    public void setRightTree(DecisionTree rightTree) {
        this.rightTree = rightTree;
    }

    @Override
    public int classify(Record record) {
        if (record.checkPredicate(predicate)) {
            return rightTree.classify(record);
        }
        else {
            return leftTree.classify(record);
        }
    }

    @Override
    public int depth() {
        return 1 + max(leftTree.depth(), rightTree.depth());
    }

    @Override
    public int numberOfNodes() {
        return 1 + leftTree.numberOfNodes() + rightTree.numberOfNodes();
    }

    @Override
    public int numberOfPredicateNodes() {
        return 1 + leftTree.numberOfPredicateNodes() + rightTree.numberOfPredicateNodes();
    }

    @Override
    public Set<Integer> getConsideredFeatures() {
        Set<Integer> l = leftTree.getConsideredFeatures();
        Set<Integer> r = rightTree.getConsideredFeatures();

        l.addAll(r);
        l.add(predicate);

        return l;
    }

    @Override
    public String toString() {
        return "[If not feature " + predicate + " then " + leftTree.toString() + " else " + rightTree.toString() + "]";
    }

    @Override
    public String prettyPrint(ArrayList<String> attributeNames, String targetName) {
        return prettyPrint(attributeNames, targetName, 0);
    }

    @Override
    protected String prettyPrint(ArrayList<String> attributeNames, String targetName, int indent) {
        return "[Consider: " + attributeNames.get(predicate) + "]"
            + "\n" + indent("if false: ", indent + 1) + leftTree.prettyPrint(attributeNames, targetName, indent + 1)
            + "\n" + indent("if true: ", indent + 1) + rightTree.prettyPrint(attributeNames, targetName, indent + 1);
    }

    /**
     * Indents the given string by 8*n dashes.
     * @param s string
     * @param n level of indentation
     * @return indented string
     */
    private static String indent(String s, int n) {
        StringBuilder sBuilder = new StringBuilder(s);
        for (int i = 0; i < n; i++) {
            sBuilder.insert(0, "--------");
        }
        s = sBuilder.toString();
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PredicateNode)) return false;
        PredicateNode that = (PredicateNode) o;
        return (predicate == that.predicate
                && leftTree.equals(that.leftTree)
                && rightTree.equals(that.rightTree))
                || DecisionTree.isEqual(this, that);
    }

    @Override
    public List<ClassificationNode> getClassificationNodes() {
        List<ClassificationNode> onTheLeft = leftTree.getClassificationNodes();
        List<ClassificationNode> onTheRight = rightTree.getClassificationNodes();
        onTheLeft.addAll(onTheRight);
        return onTheLeft;
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate, leftTree, rightTree);
    }
}
