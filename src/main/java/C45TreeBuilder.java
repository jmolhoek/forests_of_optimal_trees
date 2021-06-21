import Data.DataSet;

/**
 * Builder for heuristic decision tree (C4.5).
 */
public class C45TreeBuilder implements ClassifierBuilder {
    @Override
    public Classifier build(DataSet data) {
        return new C45Tree(data);
    }
}
