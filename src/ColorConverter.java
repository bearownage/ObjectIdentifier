import java.util.Arrays;
import java.util.List;

public class ColorConverter {

    // r,g,b values are from 0 to 1
// h = [0,360], s = [0,1], v = [0,1]
//		if s == 0, then h = -1 (undefined)
    static List<Float> RGBtoHSV(float r, float g, float b, float h, float s, float v) {
        float min, max, delta;
        min = Math.min(r, Math.min(g, b));
        max = Math.max(r, Math.max(g, b));
        v = max;

        delta = max - min;

        if (max != 0) {
            s = delta / max;
        } else {
            //r = g = b = 0
            s = 0;
            h = -1;
            return Arrays.asList(h, s, v);
        }

        if (r == max) {
            h = (g - b) / delta;
        } else if (g == max) {
            h = 2 + (b - r) / delta;
        } else {
            h = 4 + (r - g) / delta;
        }

        h *= 60;
        if (h < 0) {
            h += 360;
        }

        return Arrays.asList(h, s, v);
    }

    static List<Float> HSVtoRGB(float r, float g, float b, float h, float s, float v) {
        int i;
        float f, p, q, t;
        if (s == 0) {
            // achromatic (grey)
            r = g = b = v;
            return Arrays.asList(r, g, b);
        }
        h /= 60;            // sector 0 to 5
        i = (int) Math.floor(h);
        f = h - i;            // factorial part of h
        p = v * (1 - s);
        q = v * (1 - s * f);
        t = v * (1 - s * (1 - f));
        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:        // case 5:
                r = v;
                g = p;
                b = q;
                break;
        }

        return Arrays.asList(r, g, b);
    }

}
