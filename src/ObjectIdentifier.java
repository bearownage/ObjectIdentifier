import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.List;

public class ObjectIdentifier {

    BufferedImage inputImage;

    int width = 640; // default image width and height
    int height = 480;

    Cluster[][] clusters;

    Hashtable<String, Histogram> objectHistograms;
    List<String> objectNames;
    Histogram imageHistogram;
    List<Cluster> objectInImageClusters;

    ObjectIdentifier() {
        objectHistograms = new Hashtable<>();
        imageHistogram = new Histogram();
        objectNames = new ArrayList<>();
        objectInImageClusters = new ArrayList<>();
        clusters = new Cluster[width + 1][height + 1];
    }

    /**
     * Read Image RGB
     * Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readObjectRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            //Create a new HashTable for this object
            System.out.println("Reading Object: " + imgPath);
            objectHistograms.put(imgPath, new Histogram());
            objectNames.add(imgPath);

            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            //BufferedImage d = ImageIO.read(new File(imgPath));
            //System.out.println(d);
            int awidth = img.getWidth();
            int aheight = img.getHeight();
            //System.out.println(awidth);
            //System.out.println(aheight);

            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;

            int topLeftCornerX = width;
            int topLeftCornerY = height;
            int bottomRightCornerX = 0;
            int bottomRightCornerY = 0;

            int greenBackgroundPix = 0;
            int illegalGreens = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];
                    //Setings the a, then masks r using the least significant 8 bits rtc.
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    if (x == 0 && y == 0) {
                        greenBackgroundPix = pix;
                        /*System.out.println((pix >> 16) & 0xFF);
                        System.out.println((pix >> 8) & 0xFF);
                        System.out.println((pix) & 0xFF);
                        System.out.println("h of green: " + Math.round(ColorConverter.RGBtoHSV((pix >> 16) & 0xFF, (pix >> 8) & 0xFF, (pix) & 0xFF, 0, 0, 0).get(0)));
                    */
                    }

                    if (pix != greenBackgroundPix) {
                        topLeftCornerX = Math.min(topLeftCornerX, x);
                        topLeftCornerY = Math.min(topLeftCornerY, y);
                        bottomRightCornerX = Math.max(bottomRightCornerX, x);
                        bottomRightCornerY = Math.max(bottomRightCornerY, y);

                        int rR = (pix >> 16) & 0xFF;
                        int gG = (pix >> 8) & 0xFF;
                        int bB = (pix) & 0xFF;
                        List<Integer> hsv = ColorConverter.RGBtoHSV(rR, gG, bB);
                        int h = Math.round(hsv.get(0));
                        //float h = hsv.get(0);
/*                        if (h == 106) {
                            illegalGreens++;
                            System.out.println("Found another pixel that matches real h: " + 106);
                            System.out.println("Greenbackground pix: " + greenBackgroundPix);
                            System.out.println(pix);
                            System.out.println(rR);
                            System.out.println(gG);
                            System.out.println(bB);
                            System.out.println(hsv.get(1));
                            System.out.println(hsv.get(2));
                        }*/
                        if (objectHistograms.get(imgPath).getRawTable().containsKey(h)) {
                            objectHistograms.get(imgPath).getRawTable().put(h, objectHistograms.get(imgPath).getRawTable().get(h) + 1);
                        } else {
                            objectHistograms.get(imgPath).getRawTable().put(h, 1);
                        }
                        /*if (objectHistograms.get(imgPath).getRawTable().containsKey((float) pix)) {
                            objectHistograms.get(imgPath).getRawTable().put((float) pix, objectHistograms.get(imgPath).getRawTable().get((float) pix) + 1);
                        } else {
                            objectHistograms.get(imgPath).getRawTable().put((float) pix, 1);
                        }*/
                    }
                    //System.out.println(pix);
                    img.setRGB(x, y, pix);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    ind++;
                }
            }

            objectHistograms.get(imgPath).calculateTotalPixels(topLeftCornerX, topLeftCornerY, bottomRightCornerX, bottomRightCornerY);
            objectHistograms.get(imgPath).initRatioTable();
            //System.out.println(objectHistograms.get(imgPath).getRatioTable().toString());
            //System.out.println("Size of histogram : " + objectHistograms.get(imgPath).getRatioTable().size());
            //System.out.println("Illegal greens: " + illegalGreens);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createImageClusters(String imagePath) throws FileNotFoundException {
        System.out.println("Creating the image clusters");
        inputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        try {
            int frameLength = width * height * 3;

            File file = new File(imagePath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");

            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            boolean once = false;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];
                    //Setings the a, then masks r using the least significant 8 bits rtc.

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    int rR = (pix >> 16) & 0xFF;
                    int gG = (pix >> 8) & 0xFF;
                    int bB = (pix) & 0xFF;
                    List<Integer> hsv = ColorConverter.RGBtoHSV(rR, gG, bB);
                    int h = Math.round(hsv.get(0));
                    //float h = hsv.get(0);
                    /*if (objectHistograms.get(objectNames.get(0)).getMostCommonColors().contains(h) && !once) {
                        System.out.println("Found a match in image!");
                        System.out.println(rR);
                        System.out.println(gG);
                        System.out.println(bB);
                        System.out.println("Coords");
                        System.out.println(x);
                        System.out.println(y);


                        once = true;
                    }*/
                    //Initiate cluster

                    if (clusters[x][y] == null) {
                        if (x > 0) {
                            if (y > 0) {
                                //System.out.println(x + " " + y);
                                if (clusters[x - 1][y].getHValue() == h) {
                                    clusters[x - 1][y].increaseSize().setStartX(x).setStartY(y).setEndY(y).setEndX(x);
                                    clusters[x][y] = clusters[x - 1][y];
                                } else if (clusters[x][y - 1].getHValue() == h) {
                                    clusters[x][y - 1].increaseSize().setStartX(x).setStartY(y).setEndY(y).setEndX(x);
                                    clusters[x][y] = clusters[x][y - 1];
                                } else {
                                    clusters[x][y] = new Cluster(h, 1, x, y, x, y);
                                    clusters[x][y].addNeighboringClusters(Arrays.asList(clusters[x - 1][y], clusters[x][y - 1]));
                                    clusters[x - 1][y].addNeighboringClusters(List.of(clusters[x][y]));
                                    clusters[x][y - 1].addNeighboringClusters(List.of(clusters[x][y]));
                                    clusters[x - 1][y - 1].addNeighboringClusters(List.of(clusters[x][y]));
                                }
                            } else {
                                if (clusters[x - 1][y].getHValue() == h) {
                                    clusters[x - 1][y].increaseSize().setStartX(x).setStartY(y).setEndY(y).setEndX(x);
                                    clusters[x][y] = clusters[x - 1][y];
                                } else {
                                    clusters[x][y] = new Cluster(h, 1, x, y, x, y);
                                    clusters[x - 1][y].addNeighboringClusters(List.of(clusters[x][y]));
                                }
                            }
                        } else {
                            // x == 0
                            if (y > 0) {
                                if (clusters[x][y - 1].getHValue() == h) {
                                    clusters[x][y - 1].increaseSize().setStartX(x).setStartY(y).setEndY(y).setEndX(x);
                                    clusters[x][y] = clusters[x][y - 1];
                                } else {
                                    clusters[x][y] = new Cluster(h, 1, x, y, x, y);
                                    clusters[x][y - 1].addNeighboringClusters(List.of(clusters[x][y]));
                                }
                            } else {
                                clusters[x][y] = new Cluster(h, 1, x, y, x, y);
                            }
                        }
                        //Check left and top of datapoint
                    }

                    //Add this point to the cluster
                    clusters[x][y].addPoint(x, y);

                    if (imageHistogram.getRawTable().containsKey(h)) {
                        imageHistogram.getRawTable().put(h, imageHistogram.getRawTable().get(h) + 1);
                    } else {
                        imageHistogram.getRawTable().put(h, 1);
                    }

                    //System.out.println(h);
                    inputImage.setRGB(x, y, pix);
                    ind++;
                }
            }

            /*Set<Cluster> seenCluster = new HashSet<>();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (objectHistograms.get(objectNames.get(0)).getRatioTable().containsKey(clusters[x][y].getHValue()) && !seenCluster.contains(clusters[x][y])) {
                        if (clusters[x][y].getSize() > 100) {
                            //System.out.println("cluster: " + clusters[x][y] + " size of cluster " + clusters[x][y].getSize() + " sx " + clusters[x][y].startX + " sy " + clusters[x][y].startY + " ex " + clusters[x][y].endX + " ey " + clusters[x][y].endY);
                        }
                        seenCluster.add(clusters[x][y]);
                    }
                }
            }
            System.out.println("Total number of clusters : " + seenCluster.size());*/

            //imageHistogram.calculateTotalPixels(0, 0, width, height);
            //imageHistogram.initRatioTable();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findObjectInImage() {
        System.out.println("Finding object");
        boolean[][] visited = new boolean[width + 1][height + 1];
        Histogram objectHistogram = objectHistograms.get(objectNames.get(0));
        Set<Cluster> clustersAccountedFor = new HashSet<>();
        //System.out.println(objectHistogram.getRatioTable().toString());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //System.out.println(visited[x][y]);
                if (objectHistogram.getMostCommonColors().contains(clusters[x][y].getHValue()) && !visited[x][y]) {
                    //System.out.println("Match!");
                    //System.out.println(clustersAccountedFor.size());
                    Cluster mergedCluster = mergeClusters(x, y, objectHistogram, clusters[x][y], visited, clustersAccountedFor);
                    //System.out.println(x + " " + y);
                    //System.out.println(visited[x][y]);
                    if (mergedCluster.getColorsInCluster().size() >= objectHistogram.getMostCommonColors().size()) {
 //                       if (mergedCluster.getSize() > 1000) {

                            //if ((x == 270 && y == 164) || (x == 269 && y == 163)) {
                            //if (x == 181 && y == 275) {
/*                            System.out.println("Possible object found!");
                            System.out.println("Started in cluster: " + clusters[x][y] + " that has info " + clusters[x][y].getStartX() + " " + clusters[x][y].getStartY() + " " + clusters[x][y].getEndX() + " " + clusters[x][y].getEndY());
                            System.out.println("Original neighboring cluster: " + clusters[x][y].getNeighboringClusters());
                            System.out.println("Starting point of func: " + x + " " + y);
                            System.out.println("startx: " + mergedCluster.getStartX());
                            System.out.println("starty: " + mergedCluster.getStartY());
                            System.out.println("endx: " + mergedCluster.getEndX());
                            System.out.println("endy: " + mergedCluster.getEndY());
                            System.out.println("width: " + (mergedCluster.getEndX() - mergedCluster.getStartX()));
                            System.out.println("height: " + (mergedCluster.getEndY() - mergedCluster.getStartY()));
                            System.out.println("size: " + mergedCluster.getSize());
                            System.out.println("Neighbor facts");
*//*                        for (Cluster neighbor : visitedNeighbors) {
                            System.out.println(neighbor + " " + neighbor.getSize() + " n: " + neighbor.getNeighboringClusters().toString());
                            System.out.println(neighbor.getStartX() + " " + neighbor.getStartY() + " " + neighbor.getEndX() + " " + neighbor.getEndY());
                        }*//*

                            System.out.println("points in cluster: " + mergedCluster.getPoints().size());
                            //xSystem.out.println("colors in cluster: " + colorsInCluster.toString());
                            System.out.println("---------------------------------------");*/
                            createClusterHistogramAndCompare(objectHistogram, mergedCluster);
                        //}
                    }
                }
            }
        }

    }

    public Cluster mergeClusters(int x, int y, Histogram objectHistogram, Cluster cluster, boolean[][] visited, Set<Cluster> clustersAccountedFor) {
        //System.out.println(x + " " + y);
        List<Cluster> neighbors = new ArrayList<>(cluster.getNeighboringClusters());

        int startX = cluster.getStartX();
        int startY = cluster.getStartY();
        int endX = cluster.getEndX();
        int endY = cluster.getEndY();
        double size = cluster.getSize();

        Set<Cluster> visitedNeighbors = new HashSet<>(List.of(cluster));
        List<int[]> pointsInCluster = new ArrayList<>(cluster.getPoints());
        Set<Integer> colorsInCluster = new HashSet<>(List.of(cluster.getHValue()));

        while (neighbors.size() != 0) {
            Cluster neighbor = neighbors.get(0);

            //System.out.println("Going through neighbors: " + neighbor.getStartX() + " " + neighbor.getStartY());
            //visitedNeighbors.add(neighbor);
            neighbors.remove(0);
            colorsInCluster.add(neighbor.getHValue());

            if (objectHistogram.getMostCommonColors().contains(neighbor.getHValue())) {
/*                if (Objects.equals(neighbor.toString(), "Cluster@6a8b94b2")) {
                    System.out.println("hmm hmm hmm");
                }*/
                /*if ((x == 270 && y == 164) || (x == 269 && y == 163)) {
                    System.out.println("Neighbor passed vibe check: " + neighbor);
                    System.out.println("Current set of neighbors to go through after: " + neighbors);
                    System.out.println("Grabbing neighbors: " + neighbor.getNeighboringClusters());
                }*/
                for (Cluster newNeighbor : neighbor.getNeighboringClusters()) {
                    if (!visitedNeighbors.contains(newNeighbor) && !clustersAccountedFor.contains(newNeighbor)) {
                        neighbors.add(newNeighbor);
                        visitedNeighbors.add(newNeighbor);
                    }
                }
                //neighbors.addAll(neighbor.getNeighboringClusters().stream().filter(e -> !visitedNeighbors.contains(e)).collect(Collectors.toList()));
                size += neighbor.getSize();
                startX = Math.min(startX, neighbor.getStartX());
                startY = Math.min(startY, neighbor.getStartY());
                endX = Math.max(endX, neighbor.getEndX());
                endY = Math.max(endY, neighbor.getEndY());
                pointsInCluster.addAll(neighbor.getPoints());
            }
        }

        for (int[] point : pointsInCluster) {
/*            if ( point[0] == 270 && point[1] == 164) {
                System.out.println("Hmm");
            }*/
            visited[point[0]][point[1]] = true;
        }

        /*for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                //System.out.println("Set value to true");
                visited[i][j] = true;
            }
        }*/

/*        if (size > objectHistogram.getMostCommonColors().size()) {
            //if ((x == 270 && y == 164) || (x == 269 && y == 163)) {
            System.out.println("Possible object found!");
            System.out.println("Started in cluster: " + cluster + " that has info " + cluster.getStartX() + " " + cluster.getStartY() + " " + cluster.getEndX() + " " + cluster.getEndY());
            System.out.println("Original neighboring cluster: " + cluster.getNeighboringClusters());
            System.out.println("Starting point of func: " + x + " " + y);
            System.out.println("startx: " + startX);
            System.out.println("starty: " + startY);
            System.out.println("endx: " + endX);
            System.out.println("endy: " + endY);
            System.out.println("width: " + (endX - startX));
            System.out.println("height: " + (endY - startY));
            System.out.println("size: " + size);
            System.out.println("Visited neighbors: " + visitedNeighbors.size());
            System.out.println("Neighbor facts");
*//*                for (Cluster neighbor : visitedNeighbors) {
                    System.out.println(neighbor + " " + neighbor.getSize() + " n: " + neighbor.getNeighboringClusters().toString());
                    System.out.println(neighbor.getStartX() + " " + neighbor.getStartY() + " " + neighbor.getEndX() + " " + neighbor.getEndY());
                }*//*
            System.out.println("points in cluster: " + pointsInCluster.size());
            //xSystem.out.println("colors in cluster: " + colorsInCluster.toString());
            System.out.println("---------------------------------------");

            createClusterHistogramAndCompare(objectHistogram);
        }*/


        //}
        clustersAccountedFor.addAll(visitedNeighbors);
        return new Cluster(-1, size, startX, startY, endX, endY).addPoints(pointsInCluster).addColorsInCluster(colorsInCluster);
    }

    public void createClusterHistogramAndCompare(Histogram objectHistogram, Cluster mergedCluster) {
        Histogram mergedClusterHistogram = new Histogram();

        for (int[] points : mergedCluster.getPoints()) {
            int h = clusters[points[0]][points[1]].getHValue();
            if (mergedClusterHistogram.getRawTable().containsKey(h)) {
                mergedClusterHistogram.getRawTable().put(h, mergedClusterHistogram.getRawTable().get(h) + 1);
            } else {
                mergedClusterHistogram.getRawTable().put(h, 1);
            }
        }
        mergedClusterHistogram.calculateTotalPixels(mergedCluster.getStartX(), mergedCluster.getStartY(), mergedCluster.getEndX(), mergedCluster.getEndY());
        mergedClusterHistogram.initRatioTable();
/*
        System.out.println("---------------Merged cluster histogram-------------");
        System.out.println(mergedClusterHistogram.getRatioTable().toString());
*/
        //System.out.println(mergedClusterHistogram.getRawTable().toString());

        if (objectHistogram.getMostCommonColors().containsAll(mergedClusterHistogram.getMostCommonColors())) {
            boolean clusterIsValid = true;
            //double largestChi = -1.0;
/*            for (Map.Entry<Integer, Double> e : mergedClusterHistogram.getRatioTable().entrySet()) {
                double chi = Math.pow((e.getValue() - objectHistogram.getRatioTable().get(e.getKey())), 2) / objectHistogram.getRatioTable().get(e.getKey());
                System.out.println("Object: " + e.getKey());
                System.out.println("Value E: " + objectHistogram.getRatioTable().get(e.getKey()) + " " + e.getValue());
                System.out.println("Chi: " + Math.pow((e.getValue() - objectHistogram.getRatioTable().get(e.getKey())), 2) / objectHistogram.getRatioTable().get(e.getKey()));
                if (chi > 0.45) {
                    clusterIsValid = false;
                    break;
                }*/
/*            int i = 0;
            int comparisonsMade = 0;
            while ( comparisonsMade < 5 && comparisonsMade < mergedClusterHistogram.getRawTable().size()) {
                int color = objectHistogram.getMostCommonColorsInOrder().get(i);
                //double chi = Math.pow((mergedClusterHistogram.getRatioTable().get(color) - objectHistogram.getRatioTable().get(color)), 2) / objectHistogram.getRatioTable().get(color);
                if (mergedClusterHistogram.getRawTable().get(color) != null) {
                    comparisonsMade++;
                    System.out.println(color + " " + mergedClusterHistogram.getRawTable().get(color));
                    if (mergedClusterHistogram.getRawTable().get(color) < 100) {
                        clusterIsValid = false;
                        break;
                    }
                }
                i++;
            }*/

            for (int i = 0; i < mergedClusterHistogram.getMostCommonColorsInOrder().size(); i++) {
                int colorCluster = mergedClusterHistogram.getMostCommonColorsInOrder().get(i);
                int colorObject = objectHistogram.getMostCommonColorsInOrder().get(i);
                if ( Math.abs(colorCluster - colorObject) > 16 || mergedClusterHistogram.getRawTable().get(colorCluster) < 90) {
                    clusterIsValid = false;
                    break;
                }
            }

            if (clusterIsValid) {
                System.out.println("---Comparison Of Real Object vs Cluster---");
                System.out.println("Merged: " + mergedClusterHistogram.getRawTable().toString());
                System.out.println("Object: " + objectHistogram.getRawTable().toString());
                //System.out.println("Ratio: " + objectHistogram.getRatioTable());
                /*System.out.println(objectHistogram.getMostCommonColorsInOrder().toString());
                System.out.println(mergedClusterHistogram.getMostCommonColorsInOrder().toString());*/
                System.out.println("-----------Colors-----------");
                System.out.println(objectHistogram.getMostCommonColorsInOrder());
                System.out.println(mergedClusterHistogram.getMostCommonColorsInOrder());
                System.out.println(mergedCluster.toString());
                objectHistogram.calculateMean();
                mergedClusterHistogram.calculateMean();
                System.out.println("Mean of real " + objectHistogram.getMean());
                System.out.println("Mean of merged " + mergedClusterHistogram.getMean());
                objectInImageClusters.add(mergedCluster);
            }
        }

        //System.out.println("Largest chi: " + largestChi);
    }

    public void createObjectHistogram(String imagePath) throws IOException {
        // Read in the specified image
        BufferedImage object = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //originalImage = ImageIO.read(new File(imagePath));
        //initObjectHistogram(originalImage);
        readObjectRGB(width, height, imagePath, object);
        //System.out.println(histogram.toString());
/*        ArrayList<Integer> test = new ArrayList<>();
        for (Integer value : objectHistograms.get(imagePath).getRawTable().values()) {
            test.add(value);
            // ...
        }
        Collections.sort(test);*/

        //System.out.println(test);
        //System.out.println(objectHistograms.toString());
        // Use label to display the image
    }

    public void labelObjectInImage() {
        System.out.println("Label");
        System.out.println("Number of matches: " + objectInImageClusters.size());
        Cluster object = objectInImageClusters.get(0);
        System.out.println("Common: " + objectHistograms.get(objectNames.get(0)).getMostCommonColorsInOrder());
        System.out.println("Total Clusters: " + object.getColorsInCluster().size());
        System.out.println();

        BufferedImage SubImg = inputImage.getSubimage(object.getStartX(), object.getStartY(), object.getEndX() - object.getStartX(), object.getEndY() - object.getStartY());
        JLabel imageOnFrame = new JLabel(new ImageIcon(SubImg));

        GridBagLayout gLayout = new GridBagLayout();
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(gLayout);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;

        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(imageOnFrame, c);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();

        for (int i = 1; i < args.length; i++) {
            objectIdentifier.createObjectHistogram(args[i]);
        }

        objectIdentifier.createImageClusters(args[0]);
        objectIdentifier.findObjectInImage();
        objectIdentifier.labelObjectInImage();
        //System.out.println(ColorConverter.RGBtoHSV(255, 128, 64, 0, 0, 0));


    }
}
