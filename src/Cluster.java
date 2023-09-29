import java.util.ArrayList;
import java.util.List;

public class Cluster {

    float hValue;
    double size;
    List<Cluster> neighboringClusters;

    int startX;
    int startY;
    int endX;
    int endY;

    public Cluster(float hValue, double size, int startX, int startY, int endX, int endY) {
        this.hValue = hValue;
        this.size = size;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        neighboringClusters = new ArrayList<>();
    }

    public float getHValue() {
        return hValue;
    }

    public Cluster increaseSize() {
        size++;
        return this;
    }

    public double getSize() {
        return size;
    }

    public Cluster addNeighboringClusters(List<Cluster> clusters) {
        this.neighboringClusters.addAll(clusters);
        return this;
    }

    public Cluster setStartX(int x) {
        this.startX = Math.min(startX, x);
        return this;
    }

    public Cluster setStartY(int y) {
        this.startX = Math.min(startY, y);
        return this;
    }

    public Cluster setEndX(int x) {
        this.startX = Math.max(startX, x);
        return this;
    }

    public Cluster setEndY(int y) {
        this.startX = Math.max(startY, y);
        return this;
    }
}
