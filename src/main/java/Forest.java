import Data.Record;

/**
 * Can be both a heuristic Forest or a Forest of Optimal Trees.
 */
public abstract class Forest implements Classifier {
    /**
     * Classifies the given record.
     * @param record the record to be classified (feature vector)
     * @return int representing the class
     */
    public abstract int classify(Record record);

    /**
     * Returns the number of trees in the forest.
     * @return number of trees in the forest.
     */
    public abstract int numberOfTrees();
}
