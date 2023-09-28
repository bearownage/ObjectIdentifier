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
}
