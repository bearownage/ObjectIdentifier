import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class ObjectIdentifier {

    BufferedImage originalImage;
    int width = 640; // default image width and height
    int height = 480;

    Hashtable<String, Histogram> objectHistograms;
    Histogram imageHistogram;

    ObjectIdentifier() {
        objectHistograms = new Hashtable<>();
        imageHistogram = new Histogram();
    }

    /**
     * Read Image RGB
     * Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            //Create a new HashTable for this object
            objectHistograms.put(imgPath, new Histogram());

            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            BufferedImage d = ImageIO.read(file);
            int awidth = img.getWidth();
            int aheight = img.getHeight();
            System.out.println(awidth);
            System.out.println(aheight);

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
                    if (x == 0 && y == 0 ) {
                        greenBackgroundPix = pix;
                        /*System.out.println((pix >> 16) & 0xFF);
                        System.out.println((pix >> 8) & 0xFF);
                        System.out.println((pix) & 0xFF);*/
                    }

                    if (pix != greenBackgroundPix) {
                        topLeftCornerX = Math.min(topLeftCornerX, x);
                        topLeftCornerY = Math.min(topLeftCornerY, y);
                        bottomRightCornerX = Math.max(bottomRightCornerX, x);
                        bottomRightCornerY = Math.max(bottomRightCornerY, y);
                        if (objectHistograms.get(imgPath).getRawTable().containsKey(pix)) {
                            objectHistograms.get(imgPath).getRawTable().put(pix, objectHistograms.get(imgPath).getRawTable().get(pix) + 1);
                        } else {
                            objectHistograms.get(imgPath).getRawTable().put(pix, 1);
                        }
                    } else {

                    }
                    //System.out.println(pix);
                    img.setRGB(x, y, pix);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    ind++;
                }
            }

            objectHistograms.get(imgPath).calculateTotalPixels(topLeftCornerX, topLeftCornerY, bottomRightCornerX, bottomRightCornerY);
            objectHistograms.get(imgPath).initRatioTable();
            System.out.println(objectHistograms.get(imgPath).getRatioTable().toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createImageHistogram(String imagePath) {

    }

    public void createObjectHistogram(String imagePath) throws IOException {
        // Read in the specified image
        originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //originalImage = ImageIO.read(new File(imagePath));
        //initObjectHistogram(originalImage);
        readImageRGB(width, height, imagePath, originalImage);
        //System.out.println(histogram.toString());
        ArrayList<Integer> test = new ArrayList<>();
        for (Integer value : objectHistograms.get(imagePath).getRawTable().values()) {
            test.add(value);
            // ...
        }
        Collections.sort(test);
        //System.out.println(test);
        System.out.println(objectHistograms.toString());

        // Use label to display the image
/*        JLabel imageOnFrame = new JLabel(new ImageIcon(originalImage));

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
        frame.setVisible(true);*/
    }

    public static void main(String[] args) throws IOException {
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();

        for (int i = 1; i < args.length; i++) {
            objectIdentifier.createObjectHistogram(args[i]);
        }
    }
}
