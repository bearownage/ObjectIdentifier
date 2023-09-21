import java.util.Hashtable;

public class Histogram {

    Hashtable<Integer, Integer> histogram;

    public Histogram() {
        this.histogram = new Hashtable<>();
    }

    public Hashtable<Integer, Integer> getTable() {
        return histogram;
    }
}
