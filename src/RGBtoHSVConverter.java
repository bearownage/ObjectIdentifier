public class RGBtoHSVConverter {

    public static void main(String[] args) {
        int red = 255;
        int green = 128;
        int blue = 64;

        float[] hsv = RGBtoHSV(red, green, blue);

        System.out.println("Hue: " + hsv[0]);
        System.out.println("Saturation: " + hsv[1]);
        System.out.println("Value: " + hsv[2]);
    }

    public static float[] RGBtoHSV(int red, int green, int blue) {
        float[] hsv = new float[3];

        float min, max, delta;

        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;

        min = Math.min(Math.min(r, g), b);
        max = Math.max(Math.max(r, g), b);
        delta = max - min;

        // Calculate Hue
        if (delta == 0) {
            hsv[0] = 0; // Hue is undefined when delta is 0.
        } else if (max == r) {
            hsv[0] = (60 * ((g - b) / delta) + 360) % 360;
        } else if (max == g) {
            hsv[0] = (60 * ((b - r) / delta) + 120);
        } else {
            hsv[0] = (60 * ((r - g) / delta) + 240);
        }

        // Calculate Saturation
        if (max == 0) {
            hsv[1] = 0;
        } else {
            hsv[1] = (delta / max);
        }

        // Calculate Value
        hsv[2] = max;

        return hsv;
    }
}