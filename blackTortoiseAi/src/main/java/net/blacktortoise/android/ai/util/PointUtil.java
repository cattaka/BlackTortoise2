
package net.blacktortoise.android.ai.util;

import org.opencv.core.Point;

public class PointUtil {
    public static void getCenter(Point dst, Point[] ps) {
        dst.x = 0;
        dst.y = 0;
        for (Point p : ps) {
            dst.x += p.x;
            dst.y += p.y;
        }
        dst.x /= ps.length;
        dst.y /= ps.length;
    }

    /**
     * Scale to [-1,+1]
     * 
     * @param dst
     * @param width
     * @param height
     */
    public static void getCenterScaled(Point dst, Point[] ps, int width, int height) {
        dst.x = 0;
        dst.y = 0;
        for (Point p : ps) {
            dst.x += p.x;
            dst.y += p.y;
        }
        dst.x /= ps.length;
        dst.y /= ps.length;
        dst.x = ((dst.x / width) - 0.5) * 2;
        dst.y = ((dst.y / height) - 0.5) * 2;
    }

    public static double getArea(Point[] ps) {
        double x01 = ps[0].x - ps[1].x;
        double y01 = ps[0].y - ps[1].y;
        double x03 = ps[0].x - ps[3].x;
        double y03 = ps[0].y - ps[3].y;
        double x21 = ps[2].x - ps[1].x;
        double y21 = ps[2].y - ps[1].y;
        double x23 = ps[2].x - ps[3].x;
        double y23 = ps[2].y - ps[3].y;

        double s = -(x03 * y01 - x01 * y03 + x21 * y23 - x23 * y21) / 2;

        return s;
    }

    public static double getAreaScaled(Point[] ps, double width, double height) {
        double x01 = ps[0].x - ps[1].x;
        double y01 = ps[0].y - ps[1].y;
        double x03 = ps[0].x - ps[3].x;
        double y03 = ps[0].y - ps[3].y;
        double x21 = ps[2].x - ps[1].x;
        double y21 = ps[2].y - ps[1].y;
        double x23 = ps[2].x - ps[3].x;
        double y23 = ps[2].y - ps[3].y;

        double s = -(x03 * y01 - x01 * y03 + x21 * y23 - x23 * y21) / 2;

        return s / (width * height);
    }
}
