import Data.DataSet;
import Data.Record;
import org.javatuples.Pair;

import java.util.List;

/**
 * Classifier (machine learning).
 */
public interface Classifier {

    /**
     * Classifies the given Record.
     * @param record the record
     * @return the estimated class
     */
    int classify(Record record);

    /**
     * Gives the accuracy of the classifier on the given dataset.
     * @param data the data
     * @return the accuracy [0,1]
     */
    default double accuracy(DataSet data) {
        Pair<Integer, Integer> res = performance(this, data);
        int right = res.getValue0();
        int wrong = res.getValue1();

        return right / (double) (right + wrong);
    }

    /**
     * Gives the accuracy of the classifier on the given dataset.
     * @param data the data
     * @return the error [0,1]
     */
    default double error(DataSet data) {
        Pair<Integer, Integer> res = performance(this, data);
        int right = res.getValue0();
        int wrong = res.getValue1();

        return wrong / (double) (right + wrong);
    }

    /**
     * Sum of weights of misclassified instances.
     *
     * @param data dataset
     * @return sum of weights of misclassified instances
     */
    default double totalErrorOnWeightedInstances(DataSet data) {
        return weightedPerformance(this, data).getValue1();
    }

    /**
     * Does a majority vote with the ensemble of decision trees.
     *
     * @param classifiers the forest
     * @param record the record to be classified
     * @param tieResolver classifier that jumps in in case of a tie
     * @return the classification
     */
    static int majorityVote(List<WeightedTree> classifiers, Record record, Classifier tieResolver) {
        double positiveVotes = 0;
        double negativeVotes = 0;

        if (classifiers.size() == 0) {
            return tieResolver.classify(record);
        }

        for (WeightedTree c : classifiers) {
            int vote = c.classify(record);

            if (vote == 0) negativeVotes += c.getWeight();
            else if (vote == 1) positiveVotes += c.getWeight();
        }

        if (positiveVotes > negativeVotes) {
            return 1;
        }
        else if (positiveVotes < negativeVotes) {
            return 0;
        }
        else {
            return tieResolver.classify(record);
        }
    }

    private static Pair<Integer,Integer> performance(Classifier c, DataSet data) {
        int right = 0;
        int wrong = 0;

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);
            if (c.classify(r) == r.getActualClass()) {
                right++;
            }
            else {
                wrong++;
            }
        }

        return new Pair<>(right, wrong);
    }

    private static Pair<Double,Double> weightedPerformance(Classifier c, DataSet data) {
        double right = 0.0;
        double wrong = 0.0;

        for (int i = 0; i < data.getSize(); i++) {
            Record r = data.getEntry(i);

            double weight = r.getWeight();

            if (c.classify(r) == r.getActualClass()) {
                right += weight;
            }
            else {
                wrong += weight;
            }
        }

        return new Pair<>(right, wrong);
    }
}
