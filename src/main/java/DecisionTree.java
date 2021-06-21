import Data.Record;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A decision tree is a binary tree that can map a given feature vector to a class.
 */
abstract class DecisionTree implements Classifier {
    /**
     * Classifies the given record.
     * @param record the record to be classified (feature vector)
     * @return int representing the class
     */
    public abstract int classify(Record record);

    /**
     * The depth of the tree, not counting the classification nodes (leaf-nodes).
     * @return the dept
     */
    public abstract int depth();

    /**
     * The number of nodes in the tree.
     * @return number of nodes
     */
    public abstract int numberOfNodes();

    /**
     * The number of predicate nodes in the tree.
     * @return number of predicate nodes
     */
    public abstract int numberOfPredicateNodes();

    /**
     * Gives a list of all the features that the tree uses.
     * @return set of features
     */
    public abstract Set<Integer> getConsideredFeatures();

    /**
     * Gives a String representation of the tree.
     * @return String representation of the tree
     */
    public abstract String toString();

    /**
     * Gives a pretty String representation of the tree.
     * @param attributeNames the names of the attributes
     * @param targetName name of the target feature
     * @return String representation of the tree
     */
    public abstract String prettyPrint(ArrayList<String> attributeNames, String targetName);

    /**
     * Gives a pretty String representation of the tree.
     * @param attributeNames the names of the attributes
     * @param indent the level of indentation
     * @param targetName name of the target feature
     * @return String representation of the tree
     */
    protected abstract String prettyPrint(ArrayList<String> attributeNames, String targetName, int indent);

    /**
     * Structural recursive comparison starting from the root
     * @param other other tree (to check equality)
     * @return true iff they are structurally equal
     */
    public abstract boolean equals(Object other);

    protected abstract List<ClassificationNode> getClassificationNodes();

    /**
     * Checks whether two trees are actually equal. See the JavaDoc in SetOfDecisionTrees for more explanation and examples on tree equality.
     * The equality algorithm comes from Hans Zantema (1998).
     *
     * @param tree1 one tree
     * @param tree2 other tree
     * @return true iff tree1 and tree2 are equal
     */
    public static boolean isEqual(DecisionTree tree1, DecisionTree tree2) {
        // small speedup
        if (!tree1.getConsideredFeatures().equals(tree2.getConsideredFeatures())) return false;

        ArrayList<Pair<DecisionTree, DecisionTree>> S = new ArrayList<>();
        S.add(new Pair<>(clean(tree1), clean(tree2)));

        while (!S.isEmpty()) {
            Pair<DecisionTree, DecisionTree> tmp = S.remove(0);
            DecisionTree t1 = tmp.getValue0();
            DecisionTree t2 = tmp.getValue1();

            if (t1 instanceof ClassificationNode) {
                int assignClass = ((ClassificationNode) t1).getAssignClass();
                for (ClassificationNode n : t2.getClassificationNodes()) {
                    if (assignClass != n.getAssignClass()) return false;
                }
            }
            else {
                int predicate = ((PredicateNode) t1).getPredicate();
                DecisionTree left = ((PredicateNode) t1).getLeftTree();
                DecisionTree right = ((PredicateNode) t1).getRightTree();
                S.add(new Pair<>(left, strip(predicate, false, t2)));
                S.add(new Pair<>(right, strip(predicate, true, t2)));
            }
        }

        return true;
    }

    private static DecisionTree strip(int p, boolean b, DecisionTree t) {
        if (t instanceof ClassificationNode) {
            return t;
        }
        else {
            PredicateNode tree = (PredicateNode) t;
            int predicate = tree.getPredicate();
            if (predicate == p) {
                if (b) {
                    return strip(p, true, tree.getRightTree());
                }
                else {
                    return strip(p, false, tree.getLeftTree());
                }
            }
            else {
                return new PredicateNode(predicate, strip(p, b, tree.getLeftTree()), strip(p, b, tree.getRightTree()));
            }
        }
    }

    private static DecisionTree clean(DecisionTree t) {
        if (t instanceof ClassificationNode) {
            return t;
        }
        else {
            PredicateNode tree = (PredicateNode) t;
            int predicate = tree.getPredicate();
            return new PredicateNode(predicate, clean(strip(predicate, false, tree.getLeftTree())), clean(strip(predicate, true, tree.getRightTree())));
        }
    }
}
