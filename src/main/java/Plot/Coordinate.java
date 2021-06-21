package Plot;

/**
 * Represents a simple (x,y)-coordinate.
 */
public class Coordinate implements Comparable<Coordinate> {
    public double x;
    public double y;
    public double stdev;

    /**
     * Constructs a coordinate.
     * @param x x
     * @param y y
     */
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
        this.stdev = -1.0;
    }

    /**
     * Constructs a coordinate.
     * @param x x
     * @param y y
     * @param stdev standard deviation
     */
    public Coordinate(double x, double y, double stdev) {
        this.x = x;
        this.y = y;
        this.stdev = stdev;
    }

    @Override
    public int compareTo(Coordinate o) {
        return Double.compare(this.x, o.x);
    }
}
