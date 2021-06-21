import Data.DataSet;
import Plot.Coordinate;
import org.apache.commons.math3.util.Precision;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Experiment {

    /**
     * Round to three decimal places.
     * @param d number to round
     * @return rounded number
     */
    public static double round(double d, int precision) {
        return Precision.round(d, precision);
    }

    /**
     * Performs k-fold-crossvalidation with the given classifier-builder and data.
     * @param builder builder for the classifier under analysis
     * @param k k folds
     * @param data data
     * @return the list of k errors
     * @throws InterruptedException if interrupted
     */
    public static List<Double> kFoldCrossValidateRaw(ClassifierBuilder builder, int k, DataSet data) throws InterruptedException {
        if (k <= 1) throw new IllegalArgumentException("k must be bigger than 1.");
        // Get the folds
        List<DataSet> folds = data.getSmartKFolds(k);

        // initialize a list of errors
        List<Double> errors = new LinkedList<>();

        // For each fold i do:
        for (int i = 0; i < k; i++) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
            // take i apart as test set
            DataSet testSet = folds.get(i);

            // Get the training set
            DataSet trainingSet = DataSet.getTrainingSetWithIAsTestSet(folds, i);

            // Train the classifier
            Classifier c = builder.build(trainingSet);

            // add the error to the list
            errors.add(c.error(testSet));
        }
        return errors;
    }

    /**
     * Calculate the performance of the classifier built by the given ClassifierBuilder averaged over nRuns.
     *
     * @param builder classifier builder
     * @param data dataset that will be split for training and testing
     * @param nRuns n runs
     * @return average performance
     * @throws InterruptedException if interrupted (e.g. in case of timeout)
     */
    public static double performance(ClassifierBuilder builder, DataSet data, int nRuns) throws InterruptedException {
        List<Double> errors = new LinkedList<>();

        for (int i = 0; i < nRuns; i++) {
            List<Double> err = kFoldCrossValidateRaw(builder, 2, data);
            errors.addAll(err);
        }

        StatisticsHelper.removeOutliersSmart(errors);

        return StatisticsHelper.meanAndStdOf(errors).getValue0();
    }

    /**
     * Gives a list of coordinates that can be plotted.
     *
     * @param data input dataset
     * @param b builder for forest under analysis
     * @param numOfPoints number of points (evenly spaced over the x-axis)
     * @param nRuns n runs
     * @return line
     * @throws InterruptedException if interrupted (e.g. in case of timeout)
     */
    public static List<Coordinate> forestLine(DataSet data, ForestBuilder b, int numOfPoints, int nRuns) throws InterruptedException {
        List<Coordinate> res = new LinkedList<>();

        for (int numOfTrees = 1; numOfTrees <= numOfPoints; numOfTrees+=2) {
            double err = performance(
                    b.withNumberOfTrees(numOfTrees),
                    data,
                    nRuns
            );

            res.add(new Coordinate(
                        numOfTrees,
                        err
            ));
        }

        return res;
    }

    /**
     * Simple horizontal line at the given height.
     *
     * @param height height
     * @return horizontal line
     */
    public static List<Coordinate> horizontalLineAtHeight(double height) {
        List<Coordinate> res = new LinkedList<>();

        res.add(new Coordinate(-50.0, height));
        res.add(new Coordinate(250.0, height));

        return res;
    }




    //
    //
    // TABLE PRINTS
    //
    //


    /**
     * Prints a row of a table in the paper.
     * @param d dataset
     * @param builder builder for forest under analysis
     * @param nRuns n runs
     * @param w file-writer
     * @throws IOException if something goes wrong with the FileWriter
     */
    public static void printForestTableRow(DataSet d, MurRandomForestBuilder builder, int nRuns, FileWriter w) throws IOException {
        final String[] tableRow = {""};
        final String[] durations = {""};
        int[] numbersOfTrees = new int[]{
                31
        };
        int[] depths = new int[]{
                2,
                3,
                4,
                5
        };

        boolean skipNext = false;

        for (int depth : depths) {
            for (int numberOfTrees : numbersOfTrees) {
                if (skipNext) {
                    if (depth == depths[depths.length - 1]) skipNext = false;
                    System.out.println("Timeout predicted");
                    w.append("Timeout predicted");
                    w.append("\n");
                    w.flush();
                    tableRow[0] += " &t";
                    durations[0] += " t";
                    break;
                }
                final Duration timeout = Duration.ofMinutes(100L);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                final Future handler = executor.submit(new Callable() {
                    @Override
                    public String call() throws InterruptedException, IOException {
                        long start = System.currentTimeMillis();
                        double result = performance(
                                builder.withDepth(depth).withNumberOfTrees(numberOfTrees),
                                d,
                                nRuns
                        );

                        long finish = System.currentTimeMillis();
                        long duration = finish - start;

                        String str = "d=" + depth + ", \tt=" + numberOfTrees + ", \tres=" + round(result, 2) + ", \ttook " + (duration / 1000) + " seconds";
                        tableRow[0] += " &"+round(result, 2);
                        durations[0] += " " + (duration / 1000);

                        System.out.println(str);
                        w.append(str).append("\n");
                        w.flush();
                        return "";
                    }
                });

                try {
                    handler.get(timeout.toMinutes(), TimeUnit.MINUTES);
                }
                catch (Exception e) {
                    handler.cancel(true);
                    e.printStackTrace();
                    System.out.println("Timeout");
                    w.append("Timeout");
                    w.append("\n");
                    w.flush();
                    tableRow[0] += " &t";
                    durations[0] += " t";
                    if (depth < depths[depths.length - 1]) {
                        skipNext = true;
                    }
                }
                executor.shutdownNow();
            }
        }

        System.out.println(tableRow[0]);
        System.out.println(durations[0]);

        w.append(tableRow[0]).append("\n");
        w.append(durations[0]).append("\n");
        w.flush();
    }

    /**
     * Prints a row from the baseline table in the paper.
     * @param d dataset
     * @param nRuns n runs
     * @throws InterruptedException if interrupted (e.g. timeout)
     */
    public static void printBaselineTableRow(DataSet d, int nRuns) throws InterruptedException {
        int[] maxDepths = new int[]{2,3,4,5};
        int[] numbersOfTrees = new int[]{11, 31, 101};

        System.out.println("Data size: " + d.getSize());
        System.out.println("Number of features: " + d.getNumberOfFeatures());

        // C4.5 heuristic tree
        double singleC45Tree = performance(
                new C45TreeBuilder(),
                d,
                nRuns
        );

        System.out.println("C4.5: " + round(singleC45Tree, 2));

        for (int numberOfTrees : numbersOfTrees) {
            long start = System.currentTimeMillis();

            double forest = performance(
                    new HeuristicRandomForestBuilder()
                            .withNumberOfTrees(numberOfTrees),
                    d,
                    nRuns
            );

            long finish = System.currentTimeMillis();
            long duration = finish - start;

            System.out.println("Forest: t=" + numberOfTrees + ", \tres=" + round(forest,2)
                    + ", \ttook " + (duration / 1000) + " seconds");
        }

        for (int maxDepth : maxDepths) {
            final Duration timeout = Duration.ofMinutes(100L);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            final Future handler = executor.submit(new Callable() {
                @Override
                public String call() {
                    long start = System.currentTimeMillis();
                    double singleMurTree;

                    try {
                        singleMurTree = performance(
                                new MurTreeBuilder()
                                        .withDepth(maxDepth)
                                        .withMaxNumberOfNodes(1000),
                                d,
                                nRuns
                        );

                        long finish = System.currentTimeMillis();
                        long duration = finish - start;

                        System.out.println("Murtree: d=" + maxDepth + ", \tres=" + round(singleMurTree, 2)
                                + ", \ttook " + (duration / 1000) + " seconds");

                        return null;
                    }
                    catch (InterruptedException e) {
                        System.out.println("sad");
                        return null;
                    }
                }
            });

            try {
                handler.get(timeout.toMinutes(), TimeUnit.MINUTES);
            } catch (Exception e) {
                handler.cancel(true);
                System.out.println("Timeout");
            }

            executor.shutdownNow();
        }
    }
}
