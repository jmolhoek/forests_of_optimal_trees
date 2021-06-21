import Data.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Classification node is a leaf node of a decision tree. It contains an integer value that represent the class
 * that a feature vector that reaches the leaf should be classified to.
 */
public class ClassificationNode extends DecisionTree {
    private int assignClass;

    /**
     * Constructs a classification node that classifies to c.
     * @param c the class
     */
    public ClassificationNode (int c) {
        assignClass = c;
    }

    /**
     * Returns what instances will be classified to.
     * @return assignClass
     */
    public int getAssignClass() {
        return assignClass;
    }

    /**
     * Setter for the class.
     * @param assignClass the new class
     */
    public void setAssignClass(int assignClass) {
        this.assignClass = assignClass;
    }

    @Override
    public int classify(Record record) {
        return assignClass;
    }

    @Override
    public int depth() {
        return 0;
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public int numberOfPredicateNodes() {
        return 0;
    }

    @Override
    public Set<Integer> getConsideredFeatures() {
        return new HashSet<>();
    }

    @Override
    public String toString() {
        return "[Classify to " + assignClass + "]";
    }

    @Override
    public String prettyPrint(ArrayList<String> attributeNames, String targetName) {
        return prettyPrint(attributeNames, targetName, 0);
    }

    @Override
    protected String prettyPrint(ArrayList<String> attributeNames, String targetName, int indent) {
        if (assignClass == 1) {
            return "[Classify to " + targetName + "]";
        }
        else {
            return "[Classify to NOT " + targetName + "]";
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ClassificationNode)) return false;
        ClassificationNode that = (ClassificationNode) other;
        return assignClass == that.assignClass;
    }

    @Override
    public List<ClassificationNode> getClassificationNodes() {
        return new ArrayList<>(Collections.singletonList(this));
    }
}
