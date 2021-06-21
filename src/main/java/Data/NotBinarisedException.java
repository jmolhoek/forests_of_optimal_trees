package Data;

/**
 * To be thrown when a dataset contains a non-binary value.
 */
public class NotBinarisedException extends Exception {

    /**
     * Is thrown when a dataset contains a non-binary value.
     * @param s message
     */
    public NotBinarisedException(String s) {
        System.out.println("DATASET NOT BINARISED");
        System.out.println(s);
    }
}