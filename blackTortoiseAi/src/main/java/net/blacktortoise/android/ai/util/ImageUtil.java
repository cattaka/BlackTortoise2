
package net.blacktortoise.android.ai.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class ImageUtil {
    public static void rotate90(Mat src, Mat tmp, Mat dst) {
        Core.flip(src, tmp, 0);
        Core.transpose(tmp, dst);
    }
}
