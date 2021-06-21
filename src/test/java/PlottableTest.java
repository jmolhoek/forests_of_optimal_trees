import Plot.Coordinate;
import Plot.Plottable;
import Plot.Plotter;
import org.junit.Test;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.Styler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PlottableTest {

    @Test
    public void testConstructor() {
        Coordinate c1 = new Coordinate(1.0, 10.3);
        Coordinate c2 = new Coordinate(2.0, 10.4);
        Coordinate c3 = new Coordinate(3.0, 10.5);
        Coordinate c4 = new Coordinate(4.0, 10.6);

        Plottable p = new Plottable("line", new ArrayList<>(Arrays.asList(c1, c2, c3, c4)));

        assertEquals(1.0, p.xData[0], 0.0001);
        assertEquals(10.3, p.yData[0], 0.0001);
        assertEquals(3.0, p.xData[2], 0.0001);
        assertEquals(10.5, p.yData[2], 0.0001);
    }

    @Test
    public void testConstructorUnsorted() {
        Coordinate c1 = new Coordinate(1.0, 10.3);
        Coordinate c2 = new Coordinate(2.0, 10.4);
        Coordinate c3 = new Coordinate(3.0, 10.5);
        Coordinate c4 = new Coordinate(4.0, 10.6);

        Plottable p = new Plottable("line", new ArrayList<>(Arrays.asList(c4, c2, c1, c3)));

        assertEquals(1.0, p.xData[0], 0.0001);
        assertEquals(10.3, p.yData[0], 0.0001);
        assertEquals(3.0, p.xData[2], 0.0001);
        assertEquals(10.5, p.yData[2], 0.0001);
    }

    @Test
    public void testConstructorStdev() {
        Coordinate c1 = new Coordinate(1.0, 10.3, 10.9);
        Coordinate c2 = new Coordinate(2.0, 10.4, 9.9);
        Coordinate c3 = new Coordinate(3.0, 10.5, 8.6);
        Coordinate c4 = new Coordinate(4.0, 10.6, 7.1);

        Plottable p = new Plottable("line", new ArrayList<>(Arrays.asList(c1, c3, c2, c4)));

        assertEquals(1.0, p.xData[0], 0.0001);
        assertEquals(10.3, p.yData[0], 0.0001);
        assertEquals(10.9, p.stdevs[0], 0.0001);
        assertEquals(3.0, p.xData[2], 0.0001);
        assertEquals(10.5, p.yData[2], 0.0001);
        assertEquals(8.6, p.stdevs[2], 0.0001);
    }

    @Test
    public void testPlotPlottable() {
        Coordinate c1 = new Coordinate(1.0, 10.3, 10.9);
        Coordinate c2 = new Coordinate(2.0, 10.4, 9.9);
        Coordinate c3 = new Coordinate(3.0, 10.5, 8.6);
        Coordinate c4 = new Coordinate(4.0, 10.6, 7.1);

        Plottable p = new Plottable("line", new ArrayList<>(Arrays.asList(c1, c3, c2, c4)));

        XYChart plot = Plotter.plot(new ArrayList<>(Collections.singletonList(p)), "title", "x", "y", 30);

        assertEquals("x", plot.getXAxisTitle());
        assertEquals("y", plot.getYAxisTitle());
        assertEquals("title", plot.getTitle());
        assertEquals(Styler.LegendPosition.OutsideE, plot.getStyler().getLegendPosition());
        assertEquals(1, plot.getSeriesMap().size());
        assertEquals(0.0, plot.getStyler().getXAxisMin(), 0.0001);
    }

    @Test
    public void testPlotPlottable2() {
        Coordinate c1 = new Coordinate(1.0, 10.3, -1.0);
        Coordinate c2 = new Coordinate(2.0, 10.4, -1.0);
        Coordinate c3 = new Coordinate(3.0, 10.5, -1.0);
        Coordinate c4 = new Coordinate(4.0, 10.6, -1.0);

        Plottable p = new Plottable("line", new ArrayList<>(Arrays.asList(c1, c3, c2, c4)));

        XYChart plot = Plotter.plot(new ArrayList<>(Collections.singletonList(p)), "title", "x", "y", 30);

        assertEquals("x", plot.getXAxisTitle());
        assertEquals("y", plot.getYAxisTitle());
        assertEquals("title", plot.getTitle());
        assertEquals(Styler.LegendPosition.OutsideE, plot.getStyler().getLegendPosition());
        assertEquals(1, plot.getSeriesMap().size());
        assertEquals(0.0, plot.getStyler().getXAxisMin(), 0.0001);
    }

    @Test
    public void testShowPlottable() {
        Coordinate c1 = new Coordinate(1.0, 10.3, 10.9);
        Coordinate c2 = new Coordinate(2.0, 10.4, 9.9);
        Coordinate c3 = new Coordinate(3.0, 10.5, 8.6);
        Coordinate c4 = new Coordinate(4.0, 10.6, 7.1);

        Plottable p = new Plottable("line", new ArrayList<>(Arrays.asList(c1, c3, c2, c4)));

        XYChart plot = Plotter.plot(new ArrayList<>(Collections.singletonList(p)), "title", "x", "y", 30);

        Plotter.show(plot);

        assertEquals("x", plot.getXAxisTitle());
    }
}
