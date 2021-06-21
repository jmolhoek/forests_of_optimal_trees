package Plot;

import java.util.Collections;
import java.util.List;

/**
 * Represents a line (a series of points).
 */
public class Plottable {
    public String name;
    public double[] xData;
    public double[] yData;
    public double[] stdevs;

    /**
     * Constructs a Plot.Plottable from a name and a list of points.
     * @param name name
     * @param coords list of coordinates
     */
    public Plottable(String name, List<Coordinate> coords) {
        Collections.sort(coords);
        int numberOfPoints = coords.size();

        this.name = name;
        this.xData = new double[numberOfPoints];
        this.yData = new double[numberOfPoints];
        this.stdevs = new double[numberOfPoints];

        for (int i = 0; i < numberOfPoints; i++) {
            xData[i] = coords.get(i).x;
            yData[i] = coords.get(i).y;
            stdevs[i] = coords.get(i).stdev;
        }
    }
}
