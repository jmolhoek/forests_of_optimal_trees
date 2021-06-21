package Plot;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.util.List;

/**
 * Makes nice plots.
 */
public class Plotter {

    /**
     * Plots a given list of lines.
     * @param lines list of lines (Plottables)
     * @param title the big title
     * @param xAxisTitle title of x-axis
     * @param yAxisTitle title of y-axis
     * @param xMax maximum of x-axis
     */
    public static XYChart plot(List<Plottable> lines, String title, String xAxisTitle, String yAxisTitle, int xMax) {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title(title)
                .xAxisTitle(xAxisTitle)
                .yAxisTitle(yAxisTitle)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);

        for (Plottable p : lines) {
            if (p.stdevs[0] == -1.0) {
                chart.addSeries(p.name, p.xData, p.yData);
            }
            else {
                chart.addSeries(p.name, p.xData, p.yData, p.stdevs);
            }
        }

        // These are often appropriate, xMax and yMax are dynamic
        chart.getStyler().setXAxisMin(0.0);

        chart.getStyler().setXAxisMax(xMax + 1.0);

        return chart;
    }

    /**
     * Visualises the given plot.
     * @param plot XYChart containing all the info
     */
    public static void show(XYChart plot) {
        new SwingWrapper(plot).displayChart();
    }
}
