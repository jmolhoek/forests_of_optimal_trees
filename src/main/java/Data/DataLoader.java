package Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Data.DataLoader reads data from a file and puts them into the Data.DataSet format.
 */
public class DataLoader {

    /**
     * Reads a file and puts it in a usable format.
     * @param path path to the file
     * @param fileType type of file ("csv" or "matr")
     * @return the dataset in usable format
     * @throws FileNotFoundException if the path to the file yields no such file
     * @throws NotBinarisedException if the data contains a non-0/1 value
     */
    public static DataSet load(String path, String fileType) throws FileNotFoundException, NotBinarisedException {
        if (fileType.equals("csv")) return loadCsvData(path);
        else return loadBinaryMatrix(path);
    }

    // credits: https://www.baeldung.com/java-csv-file-array
    /**
     * Reads a csv file and puts it in the desired format.
     *
     * @param path path to the file
     * @return the dataset in a usable format
     * @throws FileNotFoundException if the path to the file yields no such file
     * @throws NotBinarisedException if the data contains a non-0/1 value
     */
    private static DataSet loadCsvData(String path) throws FileNotFoundException, NotBinarisedException {
        ArrayList<String> names = new ArrayList<>();
        DataSet res;

        try (Scanner scanner = new Scanner(new File(path));) {
            String firstLine = scanner.nextLine();
            try (Scanner rowScanner = new Scanner(firstLine)) {
                rowScanner.useDelimiter(",");
                while (rowScanner.hasNext()) {
                    names.add(rowScanner.next());
                }
            }
            res = new DataSet(names, names.remove(names.size() - 1));
            while (scanner.hasNextLine()) {
                res.addRecord(recordFromLine(scanner.nextLine(), ",", names.size()));
            }
        }

        return res;
    }

    /**
     * Reads a binary matrix file and puts it in the desired format.
     *
     * @param path path to the file
     * @return the dataset in a usable format
     * @throws FileNotFoundException if the path to the file yields no such file
     * @throws NotBinarisedException if the data contains a non-0/1 value
     */
    private static DataSet loadBinaryMatrix(String path) throws FileNotFoundException, NotBinarisedException {
        ArrayList<String> names = new ArrayList<>();
        DataSet res;

        try (Scanner scanner = new Scanner(new File(path));) {
            String firstLine = scanner.nextLine();
            Record firstRecord = recordFromLine(firstLine, " ", 0);
            int dataSetSize = firstRecord.getNumberOfFeatures();

            for (int i = 0; i < dataSetSize; i++) {
                names.add("F" + i);
            }

            res = new DataSet(names, "Target");
            res.addRecord(firstRecord);
            while (scanner.hasNextLine()) {
                res.addRecord(recordFromLine(scanner.nextLine(), " ", 0));
            }
        }

        return res;
    }

    /**
     * Reads a line from a file and transforms it to a Data.Record.
     *
     * @param line line from the file
     * @return the record in a usable format
     * @throws NotBinarisedException if the data contains a non-0/1 value
     */
    private static Record recordFromLine(String line, String delimiter, int targetColumn) throws NotBinarisedException {
        ArrayList<Boolean> recordBuilder = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(delimiter);
            while (rowScanner.hasNext()) {
                String next = rowScanner.next();
                if (next.equals("0")) {
                    recordBuilder.add(false);
                }
                else if (next.equals("1")) {
                    recordBuilder.add(true);
                }
                else {
                    throw new NotBinarisedException("Dataset is not properly binarised. Found an entry with: " + next);
                }
            }
        }
        int classification = recordBuilder.remove(targetColumn) ? 1 : 0;
        return new Record(recordBuilder, classification);
    }
}
