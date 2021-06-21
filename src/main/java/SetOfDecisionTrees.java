import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents a set of decision trees. Sets do not have equal things in them twice.
 * There is no official definition of equality for decision trees. A structural comparison is possible (where you first
 * check whether the roots are equal, and recursively check if the left subtrees are equal etc.
 *
 * A more appropriate way to define equality is if they partition the solution space equally. This is best explained
 * using examples.
 *
 * Imagine that a decision tree is needed that does the following:
 *  * if feature i is true and j is false, then classify to 1
 *  * else classify to 0
 *
 * The tree
 *      PredicateNode(
 *          i,
 *          ClassificationNode(0),
 *          PredicateNode(
 *              j,
 *              ClassificationNode(1),
 *              ClassificationNode(0)
 *          )
 *      )
 * fulfills this requirement.
 *
 * But also the tree
 *      PredicateNode(
 *          j,
 *          PredicateNode(
 *              i,
 *              ClassificationNode(0),
 *              ClassificationNode(1)
 *          ),
 *          ClassificationNode(0)
 *      )
 * does the exact same thing.
 *
 * These trees map the exact same input the the exact same output in all cases. However a simple structural comparison will say that they are different.
 *
 * This set implementation takes the advanced definition of equality, and uses the algorithm from Hans Zantema (1999) to check for equality.
 */
public class SetOfDecisionTrees implements Set<DecisionTree>, Iterable<DecisionTree> {
    private final ArrayList<DecisionTree> items;

    /**
     * Constructs an empty set.
     */
    public SetOfDecisionTrees() {
        items = new ArrayList<>();
    }

    /**
     * Instantaneously converts the set to an ArrayList
     * @return set as an ArrayList
     */
    public ArrayList<DecisionTree> getItems() {
        return items;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof DecisionTree) {
            for (DecisionTree d : items) {
                if (d.equals(o)) return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<DecisionTree> iterator() {
        return items.iterator();
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }


    /**
     * Adds tree to the set of trees if:
     *  * Tree does not exist yet in the set
     *  * It is the same size as the other trees in the set
     *  * If the tree is smaller than the existing trees, the existing trees are removed
     */
    @Override
    public boolean add(DecisionTree decisionTree) {
        for (DecisionTree d : items) {
            if (d.numberOfPredicateNodes() < decisionTree.numberOfPredicateNodes()) return false;
            if (decisionTree.numberOfNodes() < d.numberOfPredicateNodes()) {
                items.clear();
                break;
            }
            if (d.equals(decisionTree)) return false;
        }

        return items.add(decisionTree);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof DecisionTree) {
            for (DecisionTree d : items) {
                if (d.equals(o)) {
                    items.remove(d);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        for (Object t : c) {
            if (!(t instanceof DecisionTree)) return false;
            this.add((DecisionTree) t);
        }
        return true;
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public boolean removeAll(Collection c) {
        boolean agg = true;
        for (Object t : c) {
            if (!(t instanceof DecisionTree)) return false;
            agg &= this.remove(t);
        }
        return agg;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        for (Object t : c) {
            if (!(t instanceof DecisionTree)) return false;
            if (!this.contains(t)) return false;
        }
        return true;
    }

    @Override
    public Object[] toArray(Object[] a) {
        return new Object[0];
    }
}
