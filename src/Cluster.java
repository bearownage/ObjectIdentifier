import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cluster {

    int hValue;
    double size;
    List<Cluster> neighboringClusters;
    List<int[]> points;

    int startX;
    int startY;
    int endX;
    int endY;

    private Set<Integer> colorsInCluster;

    public Cluster(int hValue, double size, int startX, int startY, int endX, int endY) {
        this.hValue = hValue;
        this.size = size;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        points = new ArrayList<>();
        neighboringClusters = new ArrayList<>();
        colorsInCluster = new HashSet<>();
    }

    public Cluster addPoint(int x, int y) {
        points.add(new int[]{x, y});
        return this;
    }

    public List<int[]> getPoints() {
        return points;
    }

    public Cluster addPoints(List<int[]> points) {
        this.points.addAll(points);
        return this;
    }

    public int getHValue() {
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
        for (Cluster cluster : clusters) {
            if (!this.neighboringClusters.contains(cluster)) {
                this.neighboringClusters.add(cluster);
            }
        }

        //this.neighboringClusters.addAll(clusters);
        return this;
    }

    public Cluster addColorsInCluster(Set<Integer> colorsInCluster) {
        this.colorsInCluster.addAll(colorsInCluster);
        return this;
    }

    public Set<Integer> getColorsInCluster() {
        return colorsInCluster;
    }

    public List<Cluster> getNeighboringClusters() {
        return neighboringClusters;
    }

    public Cluster setStartX(int x) {
        this.startX = Math.min(startX, x);
        return this;
    }

    public Cluster setStartY(int y) {
        this.startY = Math.min(startY, y);
        return this;
    }

    public Cluster setEndX(int x) {
        this.endX = Math.max(endX, x);
        return this;
    }

    public Cluster setEndY(int y) {
        this.endY = Math.max(endY, y);
        return this;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "hValue=" + hValue +
                ", size=" + size +
                //", neighboringClusters=" + neighboringClusters +
                ", points=" + points +
                ", startX=" + startX +
                ", startY=" + startY +
                ", endX=" + endX +
                ", endY=" + endY +
                //", colorsInCluster=" + colorsInCluster +
                '}';
    }
}
