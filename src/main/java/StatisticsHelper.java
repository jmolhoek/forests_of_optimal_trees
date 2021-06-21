import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class containing some helper methods for statistics.
 */
public class StatisticsHelper {

    /**
     * Removes the outliers. Outliers here are data below Q1 - IQR or data above Q3 + IQR
     * based on: https://stackoverflow.com/a/47892110/15168559
     *
     * @param input input data
     */
    public static void removeOutliersSmart(List<Double> input) {
        if (input.size() <= 2) return;

        Collections.sort(input);

        List<Double> outliers = new ArrayList<>();
        List<Double> data1;
        List<Double> data2;

        if (input.size() % 2 == 0) {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2, input.size());
        } else {
            data1 = input.subList(0, input.size() / 2);
            data2 = input.subList(input.size() / 2 + 1, input.size());
        }
        double q1 = getMedianOfSortedList(data1);
        double q3 = getMedianOfSortedList(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - iqr;
        double upperFence = q3 + iqr;
        for (Double d : input) {
            if (d < lowerFence || d > upperFence)
                outliers.add(d);
        }

        for (double outlier : outliers) {
            input.remove(outlier);
        }
    }

    /**
     * Calculates the median value of the data.
     * Based on: https://stackoverflow.com/a/47892110/15168559
     *
     * @param data data of which the median value is needed
     * @return median of the data
     */
    public static double getMedianOfSortedList(List<Double> data) {
        if (data == null || data.size() == 0)
            throw new IllegalArgumentException("No data -> no median");
        if (data.size() == 1)
            return data.get(0);
        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
    }

    /**
     * Calculates the mean and the (sample-) standard deviation of the items in the list.
     *
     * @param li the list of numbers
     * @return mean and standard deviation respectively
     */
    public static Pair<Double, Double> meanAndStdOf(List<Double> li) {
        int size = li.size();
        double sum = 0.0;
        for (double d : li) {
            sum += d;
        }

        double mean = sum/size;

        double aggregator = 0.0;
        for (double d : li) {
            aggregator += Math.pow(d - mean, 2);
        }

        double sample_variance = aggregator/(size - 1);

        return new Pair<>(mean, Math.sqrt(sample_variance));
    }

}
