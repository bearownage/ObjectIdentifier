import java.util.ArrayList;
import java.util.List;

public class Cluster {

    float hValue;
    double size;
    List<Cluster> neighboringClusters;

    int startX = 1000;
    int startY = 1000;
    int endX = -1;
    int endY = -1;

    public Cluster(float hValue, double size) {
        this.hValue = hValue;
        this.size = size;
        neighboringClusters = new ArrayList<>();
    }

    public void increaseSize() {
        size++;
    }

    public void addNeighboringCluster(Cluster cluster) {
        this.neighboringClusters.add(cluster);
    }

    public void setStartX(int x) {
        this.startX = Math.min(startX, x);
    }

    public void setStartY(int y) {
        this.startX = Math.min(startY, y);
    }

    public void setEndX(int x) {
        this.startX = Math.max(startX, x);
    }

    public void setEndY(int y) {
        this.startX = Math.max(startY, y);

    }
}
