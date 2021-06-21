import Data.DataLoader;
import Data.DataSet;
import Data.NotBinarisedException;
import Plot.Coordinate;
import Plot.Plottable;
import Plot.Plotter;
import org.knowm.xchart.XYChart;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final String path_to_datasets = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\datasets";
    private static final String path_to_results = "C:\\Users\\jordm\\Documents\\PROJECTS\\rp-group-29-common\\optimal_forests\\raw_data";
    private static final int nRuns = 50;

    public static void main(String[] args) throws IOException, InterruptedException, NotBinarisedException, Infeasible {
        String[] datasetNames = new String[]{
                "anneal",
                "hepatitis",
                "hypothyroid",
                "kr-vs-kp",
                "lymph",
                "primary-tumor",
                "soybean",
                "tic-tac-toe",
                "vote",
                "yeast"
        };

//        printTables(datasetNames);

//        showPlot("tic-tac-toe", 10, 2);
    }

    /**
     * Displays a plot of performances of many different algorithms.
     *
     * @param datasetName name of dataset
     * @param numOfPoints number of points (evenly spaced over x-axis)
     * @param maxDepth max depth of optimal trees
     * @throws FileNotFoundException if dataset is not found
     * @throws NotBinarisedException if the dataset is not binary
     * @throws InterruptedException if interrupted
     */
    public static void showPlot(String datasetName, int numOfPoints, int maxDepth) throws FileNotFoundException, NotBinarisedException, InterruptedException {

        DataSet data = DataLoader.load(path_to_datasets + "/"+ datasetName +".txt", "matr");
        long start = System.currentTimeMillis();

        // Horizontal line for a single MurTree
        double singleMurTreeErr = Experiment.performance(
                new MurTreeBuilder()
                        .withDepth(maxDepth)
                        .withMaxNumberOfNodes(1000),
                data,
                nRuns
        );
        List<Coordinate> singleMurTreeLine = Experiment.horizontalLineAtHeight(singleMurTreeErr);

        // Horizontal line for a C4.5 heuristic tree
        double singleC45TreeErr = Experiment.performance(
                new C45TreeBuilder(),
                data,
                nRuns
        );
        List<Coordinate> singleC45Line = Experiment.horizontalLineAtHeight(singleC45TreeErr);

        // Line for Random-Subspace-Method MurForest
        List<Coordinate> RSMMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withRandomSubspaceMethod().withWeightType(WeightType.EQUAL),
                numOfPoints,
                nRuns
        );

        // Line for Random-Subspace-Method MurForest with weights
        List<Coordinate> WeightedRSMMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withRandomSubspaceMethod().withWeightType(WeightType.SPLIT),
                numOfPoints,
                nRuns
        );

        // Line for Random-Subspace-Method MurForest with weights
        List<Coordinate> SoftRestrictedRSMMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withSoftRestrictedRSM(),
                numOfPoints,
                nRuns
        );

        // Line for Random-Subspace-Method MurForest with weights
        List<Coordinate> HardRestrictedRSMMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withHardRestrictedRSM(),
                numOfPoints,
                nRuns
        );

        // Line for Bagged MurForest
        List<Coordinate> BagMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withBagging().withWeightType(WeightType.EQUAL),
                numOfPoints,
                nRuns
        );

        // Line for Bagged MurForest with weights based on set-aside validation set
        List<Coordinate> SplitWeightedBagMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withBagging().withWeightType(WeightType.SPLIT),
                numOfPoints,
                nRuns
        );

        // Line for Bagged MurForest with weights based on out-of-bag-set
        List<Coordinate> OobWeightedBagMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withBagging().withWeightType(WeightType.OUTOFBAG),
                numOfPoints,
                nRuns
        );

        // Line for correlation decrease
        List<Coordinate> correlationDecreaseBagLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withLowCorrelationBagging(),
                numOfPoints,
                nRuns
        );

        // Line for correlation decrease
        List<Coordinate> correlationDecreaseRSMLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withLowCorrelationRSM(),
                numOfPoints,
                nRuns
        );

        data.resetWeights();

         // Line for
        List<Coordinate> AdaBoostHeuristicLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(1).withAdaBoost(),
                numOfPoints,
                nRuns
        );

        data.resetWeights();

        // Line for
        List<Coordinate> AdaBoostMurForestLine = Experiment.forestLine(
                data,
                new MurRandomForestBuilder(maxDepth).withAdaBoost(),
                numOfPoints,
                nRuns
        );

        data.resetWeights();

        // Line for heuristic forest
        List<Coordinate> HeuristicForestLine = Experiment.forestLine(
                data,
                new HeuristicRandomForestBuilder(),
                numOfPoints,
                nRuns
        );

        long finish = System.currentTimeMillis();
        long duration = finish - start;
        System.out.println("Total duration: " + (duration / 1000) + " seconds");

        XYChart plot = Plotter.plot(
                new ArrayList<>(Arrays.asList(
                        new Plottable("Single MurTree", singleMurTreeLine),
                        new Plottable("Single C4.5 tree", singleC45Line),
                        new Plottable("Bagged MurTree forest", BagMurForestLine),
                        new Plottable("Split-weighted bagged MurTree forest", SplitWeightedBagMurForestLine),
                        new Plottable("Oob-weighted bagged MurTree forest", OobWeightedBagMurForestLine),
                        new Plottable("RSM MurTree forest", RSMMurForestLine),
                        new Plottable("Split-weighted RSM MurTree forest", WeightedRSMMurForestLine),
                        new Plottable("Correlation decrease bag MurTree forest", correlationDecreaseBagLine),
                        new Plottable("Correlation decrease RSM MurTree forest", correlationDecreaseRSMLine),
                        new Plottable("SRestricted RSM MurTree forest", SoftRestrictedRSMMurForestLine),
                        new Plottable("HRestricted RSM MurTree forest", HardRestrictedRSMMurForestLine),
                        new Plottable("AdaBoosted MurTree forest", AdaBoostMurForestLine),
                        new Plottable("AdaBoosted heuristic forest", AdaBoostHeuristicLine),
                        new Plottable("Heuristic forest", HeuristicForestLine)
                )),
                "Error comparison, dataset: " + datasetName + " (max-depth of optimal trees: " + maxDepth + ")",
                "Number of trees in the forest",
                "Error rate",
                numOfPoints
        );

        Plotter.show(plot);
    }


    /**
     * This method can print all tables from the paper. This will, however, take an insane amount of time.
     * @param datasetNames array of dataset names
     * @throws IOException if a dataset is not found
     * @throws NotBinarisedException if a dataset is not binarised
     * @throws InterruptedException if interrupted
     */
    public static void printTables(String[] datasetNames) throws IOException, NotBinarisedException, InterruptedException {
        FileWriter w = new FileWriter(path_to_results + "\\tables.txt", true);

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");
            System.out.println("================BASELINE=============================\n" + datasetName + ":");
            Experiment.printBaselineTableRow(data, nRuns);
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================RANDOMROOT===================\n").append(datasetName).append(":\n");
            System.out.println("==================RANDOMROOT===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withRandomRoot(),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================BAGGING WITH EQUAL WEIGHTS===================\n").append(datasetName).append(":\n");
            System.out.println("==================BAGGING WITH EQUAL WEIGHTS===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withBagging().withWeightType(WeightType.EQUAL),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================BAGGING WITH SPLIT===================\n").append(datasetName).append(":\n");
            System.out.println("==================BAGGING WITH SPLIT===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withBagging().withWeightType(WeightType.SPLIT),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================BAGGING WITH OOB===================\n").append(datasetName).append(":\n");
            System.out.println("==================BAGGING WITH OOB===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withBagging().withWeightType(WeightType.OUTOFBAG),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================RSM WITH EQUAL WEIGHTS===================\n").append(datasetName).append(":\n");
            System.out.println("==================RSM WITH EQUAL WEIGHTS===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withRandomSubspaceMethod().withWeightType(WeightType.EQUAL),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================RSM WITH SPLIT===================\n").append(datasetName).append(":\n");
            System.out.println("==================RSM WITH SPLIT===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withRandomSubspaceMethod().withWeightType(WeightType.SPLIT),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("======================ADA=======================\n").append(datasetName).append(":\n");
            System.out.println("================ADA=============================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withAdaBoost().withWeightType(WeightType.EQUAL),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("===================Hard==========================\n").append(datasetName).append(":\n");
            System.out.println("===========Hard==================================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withHardRestrictedRSM().withWeightType(WeightType.EQUAL),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("===================Soft restricted==========================\n").append(datasetName).append(":\n");
            System.out.println("===========Soft restricted==================================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withSoftRestrictedRSM().withWeightType(WeightType.EQUAL),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================LC RSM==================\n").append(datasetName).append(":\n");
            System.out.println("==================LC RSM===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withLowCorrelationRSM(),
                    nRuns,
                    w
            );
        }

        for (String datasetName : datasetNames) {
            DataSet data = DataLoader.load(path_to_datasets + "/" + datasetName + ".txt", "matr");

            w.append("==========================LC BAGGING==================\n").append(datasetName).append(":\n");
            System.out.println("==================LC BAGGING===========================\n" + datasetName + ":");
            Experiment.printForestTableRow(
                    data,
                    new MurRandomForestBuilder().withLowCorrelationBagging(),
                    nRuns,
                    w
            );
        }

        w.flush();
        w.close();
    }

}
