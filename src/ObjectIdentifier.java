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
    HashMap<String, List<Cluster>> objectInImageClusters;

    ObjectIdentifier() {
        objectHistograms = new Hashtable<>();
        imageHistogram = new Histogram();
        objectNames = new ArrayList<>();
        objectInImageClusters = new HashMap<>();
        clusters = new Cluster[width + 1][height + 1];
    }

    /**
     * Read Image RGB
     * Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readObjectRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            //Create a new HashTable for this object
            System.out.println("## Reading Object: " + imgPath);
            String imageName;
            if (imgPath.contains("\\")) {
                String[] path = imgPath.split("\\\\");
                imageName = path[path.length - 1];
            } else {
                imageName = imgPath;
            }
            objectNames.add(imageName);
            objectHistograms.put(imageName, new Histogram());

            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
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
                        if (objectHistograms.get(imageName).getRawTable().containsKey(h)) {
                            objectHistograms.get(imageName).getRawTable().put(h, objectHistograms.get(imageName).getRawTable().get(h) + 1);
                        } else {
                            objectHistograms.get(imageName).getRawTable().put(h, 1);
                        }
                    }
                    //System.out.println(pix);
                    img.setRGB(x, y, pix);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    ind++;
                }
            }

            objectHistograms.get(imageName).calculateTotalPixels(topLeftCornerX, topLeftCornerY, bottomRightCornerX, bottomRightCornerY);
            objectHistograms.get(imageName).initRatioTable();
            //objectHistograms.get(imageName).printOutMostCommonColorsAndTheirPercentages();
            //System.out.println(objectHistograms.get(imageName).getRawTable());
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
                    //Initiate cluster

                    if (clusters[x][y] == null) {
                        if (x > 0) {
                            if (y > 0) {
                                //System.out.println(x + " " + y);
                                if (clusters[x - 1][y].getHValue() == h) {
                                    clusters[x - 1][y].increaseSize().setStartX(x).setStartY(y).setEndY(y).setEndX(x);
                                    clusters[x][y] = clusters[x - 1][y];
                                }
                                if (clusters[x][y - 1].getHValue() == h) {
                                    if (clusters[x][y - 1] != clusters[x - 1][y]) {
                                        clusters[x][y - 1].increaseSize().setStartX(x).setStartY(y).setEndY(y).setEndX(x);
                                        clusters[x][y] = clusters[x][y - 1];
                                    }
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findObjectInImage(String objectName) {
        System.out.println("-------------Finding object--------------: " + objectName);
        boolean[][] visited = new boolean[width + 1][height + 1];
        objectInImageClusters.put(objectName, new ArrayList<>());
        Histogram objectHistogram = objectHistograms.get(objectName);
        Set<Cluster> clustersAccountedFor = new HashSet<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (objectHistogram.getMostCommonColorsWithRange().contains(clusters[x][y].getHValue()) && !visited[x][y]) {
                    Cluster mergedCluster = mergeClusters(x, y, objectHistogram, clusters[x][y], visited, clustersAccountedFor);
                    if (mergedCluster.getSize() > 2000 && mergedCluster.getSize() < 150000) {

                        createClusterHistogramAndCompare(objectHistogram, mergedCluster, objectName);
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

            if (objectHistogram.getMostCommonColorsWithRange().contains(neighbor.getHValue())) {
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
            visited[point[0]][point[1]] = true;
        }

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

    public void createClusterHistogramAndCompare(Histogram objectHistogram, Cluster mergedCluster, String objectName) {
        Histogram mergedClusterHistogram = new Histogram(objectHistogram.getMostCommonColorsInOrder());

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
        System.out.println("Merged Cluster: " + mergedCluster.toString());
        //System.out.println("---------------Merged cluster histogram-------------");
        //System.out.println(mergedClusterHistogram.getRatioTable().toString());
        //System.out.println(mergedClusterHistogram.getRawTable().toString());

        if (objectHistogram.getMostCommonColorsWithRange().containsAll(mergedClusterHistogram.getMostCommonColors())) {
            boolean clusterIsValid = true;

            System.out.println("----Comparisons-----");

            double prevChi = 0.0;
            for (int i = 0; i < objectHistogram.getMostCommonColorsGrouped().size(); i++) {
                int currColor = objectHistogram.getMostCommonColorsGrouped().get(i);
                //int nextColor = objectHistogram.getMostCommonColorsGrouped().get(i+1);
                //System.out.println("Object: " + key + " : " + objectHistogram.getMostCommonColorsBucketed().get(key));
                System.out.println(("Cluster in Image: " + currColor + " : " + objectHistogram.getMostCommonColorsBucketed().get(currColor)));

                double chi = Math.pow((mergedClusterHistogram.getMostCommonColorsBucketed().get(currColor) - objectHistogram.getMostCommonColorsBucketed().get(currColor)), 2) / objectHistogram.getMostCommonColorsBucketed().get(currColor);
                System.out.println("Color: " + currColor + " Chi!: " + chi);

                /*if ( mergedClusterHistogram.getMostCommonColorsBucketed().get(currColor) < mergedClusterHistogram.getMostCommonColorsBucketed().get(nextColor) ) {
                    System.out.println("Failed where: " + currColor + " was less than " + nextColor);
                    clusterIsValid = false;
                }*/

                if ( chi < 0.007 || chi > 0.21 ) {
                    System.out.println("chi failed: " + currColor);
                    clusterIsValid = false;
                }
            }

            //System.out.println("Object most common colors grouped: " + objectHistogram.getMostCommonColorsGrouped().toString());
            //System.out.println("Merged cluster colors grouped: " + mergedClusterHistogram.getMostCommonColorsGrouped().toString());
            /*for (int i = 0; i < objectHistogram.getMostCommonValuesGrouped().size(); i++) {
                int color = objectHistogram.getMostCommonColorsGrouped().get(i);
                int window = objectHistogram.getRangeOfColor();
                int color2;

                boolean foundAMatch = false;
                for (int j = -window; j <= window; j++) {
                    if (color + j > 360) {
                        color2 = j - 1;
                        //mostCommonColorsWithRange.add(j - 1);
                    } else if (color + j < 0) {
                        color2 = 360 + (color + j + 1);
                        //mostCommonColorsWithRange.add(360 + (color + j + 1));
                    } else {
                        color2 = color + j;
                        //mostCommonColorsWithRange.add(color + j);
                    }

                    if (mergedClusterHistogram.getMostCommonColorsGrouped().contains(color2)) {
                        foundAMatch = true;
                        break;
                    }
                }

                if (!foundAMatch) {
                    System.out.println("Could not find match for color: " + color);
                    clusterIsValid = false;
                    break;
                }
            }*/

/*
            for (int i = 0; i < mergedClusterHistogram.getMostCommonValuesGrouped().size(); i++) {
                if (mergedClusterHistogram.getMostCommonValuesGrouped().ge< 0.01) {
                    clusterIsValid = false;
                    break;
                }
            }
*/

            if (clusterIsValid) {
                //System.out.println("---Comparison Of Real Object vs Cluster---");
                //System.out.println("Merged: " + mergedClusterHistogram.getRawTable().toString());
                //System.out.println("Ratio: " + objectHistogram.getRatioTable());
                /*System.out.println(objectHistogram.getMostCommonColorsInOrder().toString());
                System.out.println(mergedClusterHistogram.getMostCommonColorsInOrder().toString());*/
                /*System.out.println("-----------Colors-----------");
                System.out.println(objectHistogram.getMostCommonColorsInOrder());
                System.out.println(mergedClusterHistogram.getMostCommonColorsInOrder());
                System.out.println(mergedCluster.toString());*/
                /*objectHistogram.calculateMean();
                mergedClusterHistogram.calculateMean();
                System.out.println("Mean of real " + objectHistogram.getMean());
                System.out.println("Mean of merged " + mergedClusterHistogram.getMean());*/
                objectInImageClusters.get(objectName).add(mergedCluster);
            }
        }

        //System.out.println("Largest chi: " + largestChi);
    }

    public void createObjectHistogram(String imagePath) throws IOException {
        BufferedImage object = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readObjectRGB(width, height, imagePath, object);
    }

    public void labelObjectInImage() {
        System.out.println("Label");
        int widthOfBox = 4;
        int grey = 8421504;

        for (Map.Entry<String, List<Cluster>> e : objectInImageClusters.entrySet()) {
            List<Cluster> objectCluster = e.getValue();
            System.out.println("Number of matches for: " + e.getKey() + ": " + objectCluster.size());

            for (int index = 0; index < objectCluster.size(); index++) {
                Cluster object = objectCluster.get(index);
                //System.out.println("Common: " + objectHistograms.get(e.getKey()).getMostCommonColorsInOrder());
                System.out.println("Total Clusters: " + object.getColorsInCluster().size());
                System.out.println("Cluster: " + object.toString());
                System.out.println();

                for (int i = object.getStartX(); i < object.getEndX(); i++) {
                    for (int j = object.getStartY(); j < object.getEndY(); j++) {
                        if (i - object.getStartX() <= widthOfBox) {
                            inputImage.setRGB(i, j, grey);
                        } else if (j - object.getStartY() <= widthOfBox) {
                            inputImage.setRGB(i, j, grey);
                        } else if (object.getEndY() - j <= widthOfBox) {
                            inputImage.setRGB(i, j, grey);
                        } else if (object.getEndX() - i <= widthOfBox) {
                            inputImage.setRGB(i, j, grey);
                        }
                    }
                }
                inputImage = addTextToImage(inputImage, e.getKey(), object.getStartX() + widthOfBox + 2, object.getEndY() - widthOfBox - 2);
            }
        }

        //BufferedImage SubImg = inputImage.getSubimage(object.getStartX(), object.getStartY(), object.getEndX() - object.getStartX(), object.getEndY() - object.getStartY());
        JLabel imageOnFrame = new JLabel(new ImageIcon(inputImage));
        //JLabel objectLabel = new JLabel(objectNames.get(0));
        //objectLabel.setBounds(object.getStartX(), object.getEndY(), object.getEndX() - object.getStartX(), 30);

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
        //frame.getContentPane().add(objectLabel, c2);
        //frame.setLayout(null);

        frame.pack();
        frame.setVisible(true);
    }

    private BufferedImage addTextToImage(BufferedImage old, String objectName, int x, int y) {
        BufferedImage img = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(old, 0, 0, width, height, null);
        g2d.setPaint(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Serif", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(objectName, x, y);
        g2d.dispose();
        return img;
    }

    public static void main(String[] args) throws IOException {
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();

        for (int i = 1; i < args.length; i++) {
            objectIdentifier.createObjectHistogram(args[i]);
        }

        objectIdentifier.createImageClusters(args[0]);
        for (String objectName : objectIdentifier.objectNames) {
            objectIdentifier.findObjectInImage(objectName);
        }
        objectIdentifier.labelObjectInImage();
    }
}
