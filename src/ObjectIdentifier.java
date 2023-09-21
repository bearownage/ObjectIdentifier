import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

public class ObjectIdentifier {

    BufferedImage originalImage;
    int width = 640; // default image width and height
    int height = 480;

    Hashtable<Integer, Integer> histogram;

    ObjectIdentifier() {
        histogram = new Hashtable<>();
    }

    /**
     * Read Image RGB
     * Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
/*    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
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

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];
                    //Setings the a, then masks r using the least significant 8 bits rtc.
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //System.out.println(pix);
                    img.setRGB(x, y, pix);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    ind++;
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    void initObjectHistogram(BufferedImage originalImage) {
        for (int x = 0; x < width; x++) {
            for ( int y = 0; y < height; y++ ) {
                // Ignore lime green
                if ( originalImage.getRGB(x, y) != -16580864) {
                    int pix = originalImage.getRGB(x, y);
                    if (histogram.containsKey(pix)) {
                        histogram.put(pix, histogram.get(pix) + 1);
                    } else {
                        histogram.put(pix, 1);
                    }
                } else {

                }
            }
        }
    }

    public void showIms(String imagePath) throws IOException {
        // Read in the specified image
        originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        originalImage = ImageIO.read(new File(imagePath));
        initObjectHistogram(originalImage);
        System.out.println(histogram.toString());

        // Use label to display the image
        JLabel imageOnFrame = new JLabel(new ImageIcon(originalImage));

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
        objectIdentifier.showIms(args[0]);
    }
}
