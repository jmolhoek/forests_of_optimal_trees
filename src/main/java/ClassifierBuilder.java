import Data.DataSet;

/**
 * Object that supports the method build(data), to build a classifier.
 */
public interface ClassifierBuilder {
    Classifier build(DataSet data) throws InterruptedException;
}
