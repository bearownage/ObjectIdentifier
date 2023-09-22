import java.util.*;

public class Histogram {

    private Double totalPixels;
    Hashtable<Integer, Integer> rawTable;
    Hashtable<Integer, Double> ratioTable;

    public Histogram() {
        this.rawTable = new Hashtable<>();
        this.ratioTable = new Hashtable<>();
        totalPixels = 0.0;
    }

    public Hashtable<Integer, Integer> getRawTable() {
        return rawTable;
    }

    public Hashtable<Integer, Double> getRatioTable() {
        return ratioTable;
    }

    public void calculateTotalPixels() {
        for (Integer value : rawTable.values()) {
            totalPixels+=value;
        }

        System.out.println(totalPixels);
    }

    public void initRatioTable() {
        List<Double> values = new ArrayList<>();
        rawTable.forEach((key, value) -> {
            double ratioValue = value / totalPixels;
            values.add(ratioValue);
            ratioTable.put(key, ratioValue);
        });
        Collections.sort(values);
        //System.out.println(values.toString());

    }




}
