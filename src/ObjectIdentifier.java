import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class ObjectIdentifier {

    BufferedImage inputImage;

    int width = 640; // default image width and height
    int height = 480;

    Cluster[][] clusters;

    Hashtable<String, Histogram> objectHistograms;
    List<String> imageNames;
    Histogram imageHistogram;

    ObjectIdentifier() {
        objectHistograms = new Hashtable<>();
        imageHistogram = new Histogram();
        imageNames = new ArrayList<>();
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
            imageNames.add(imgPath);

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
                        System.out.println((pix >> 16) & 0xFF);
                        System.out.println((pix >> 8) & 0xFF);
                        System.out.println((pix) & 0xFF);
                        System.out.println("h of green: " + Math.round(ColorConverter.RGBtoHSV((pix >> 16) & 0xFF, (pix >> 8) & 0xFF, (pix) & 0xFF, 0, 0, 0).get(0)));
                    }

                    if (pix != greenBackgroundPix) {
                        topLeftCornerX = Math.min(topLeftCornerX, x);
                        topLeftCornerY = Math.min(topLeftCornerY, y);
                        bottomRightCornerX = Math.max(bottomRightCornerX, x);
                        bottomRightCornerY = Math.max(bottomRightCornerY, y);

                        int rR = (pix >> 16) & 0xFF;
                        int gG = (pix >> 8) & 0xFF;
                        int bB = (pix) & 0xFF;
                        List<Float> hsv = ColorConverter.RGBtoHSV(rR, gG, bB, 0, 0, 0);
                        float h = Math.round(hsv.get(0));
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
            System.out.println("Size of histogram : " + objectHistograms.get(imgPath).getRatioTable().size());
            System.out.println("Illegal greens: " + illegalGreens);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createImageHistogram(String imagePath) throws FileNotFoundException {
        System.out.println("Creating the image histogram");
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
                    ;
                    List<Float> hsv = ColorConverter.RGBtoHSV(rR, gG, bB, 0, 0, 0);
                    float h = Math.round(hsv.get(0));
                    if (objectHistograms.get(imageNames.get(0)).getMostCommonColors().contains(h) && !once) {
                        System.out.println("Found a match in image!");
                        System.out.println(rR);
                        System.out.println(gG);
                        System.out.println(bB);
                        System.out.println("Coords");
                        System.out.println(x);
                        System.out.println(y);


                        once = true;
                    }
                    if (imageHistogram.getRawTable().containsKey(h)) {
                        imageHistogram.getRawTable().put(h, imageHistogram.getRawTable().get(h) + 1);
                    } else {
                        imageHistogram.getRawTable().put(h, 1);
                    }

                    //System.out.println(h);
                    ind++;
                }
            }

            imageHistogram.calculateTotalPixels(0, 0, width, height);
            imageHistogram.initRatioTable();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void compareHistograms() {

    }

    public void createObjectHistogram(String imagePath) throws IOException {
        // Read in the specified image
        BufferedImage object = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //originalImage = ImageIO.read(new File(imagePath));
        //initObjectHistogram(originalImage);
        readObjectRGB(width, height, imagePath, object);
        //System.out.println(histogram.toString());
        ArrayList<Integer> test = new ArrayList<>();
        for (Integer value : objectHistograms.get(imagePath).getRawTable().values()) {
            test.add(value);
            // ...
        }
        Collections.sort(test);
        //System.out.println(test);
        //System.out.println(objectHistograms.toString());

        // Use label to display the image
        JLabel imageOnFrame = new JLabel(new ImageIcon(object));

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

        objectIdentifier.createImageHistogram(args[0]);

        //System.out.println(ColorConverter.RGBtoHSV(255, 128, 64, 0, 0, 0));

    }
}
