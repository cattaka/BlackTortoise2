
package net.blacktortoise.android.ai.util;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class MyCapture {
    private static int sM1Seq = -1;

    private static int sM2Seq = -1;

    private static int sM3Seq = -1;

    private static int sM4Seq = -1;

    private VideoCapture mVideoCapture;

    private WorkCaches mWorkCaches;

    private boolean mRotateCamera = true;

    private boolean mReverseCamera = false;

    private Size mRequestedPreviewSize = new Size(800, 600);

    private Size mPreviewSize;

    public MyCapture(WorkCaches workCaches, VideoCapture videoCapture) {
        super();
        mWorkCaches = workCaches;
        mVideoCapture = videoCapture;
        setup(workCaches);
    }

    public void open(boolean rotateCamera, boolean reverceCamera, Size requestedPreviewSize) {
        mRotateCamera = rotateCamera;
        mReverseCamera = reverceCamera;
        mRequestedPreviewSize = requestedPreviewSize;
        mVideoCapture.open(0);
        Size size = null;
        {
            List<Size> ss = mVideoCapture.getSupportedPreviewSizes();
            for (Size s : ss) {
                if (((int)s.width) == ((int)requestedPreviewSize.width)
                        && ((int)s.height) == ((int)requestedPreviewSize.height)) {
                    size = s;
                    break;
                }
            }
            if (size == null) {
                for (int i = ss.size() - 1; i >= 0; i--) {
                    Size s = ss.get(i);
                    if (((int)s.width) <= ((int)requestedPreviewSize.width)
                            && ((int)s.height) <= ((int)requestedPreviewSize.height)) {
                        size = s;
                        break;
                    }
                }
            }
        }
        if (size != null) {
            mVideoCapture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, size.width);
            mVideoCapture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, size.height);
            mPreviewSize = size;
        } else {
            // use default
            mPreviewSize = new Size(mVideoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH),
                    mVideoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT));
        }
        mVideoCapture.set(Highgui.CV_CAP_PROP_ANDROID_FOCUS_MODE,
                Highgui.CV_CAP_ANDROID_FOCUS_MODE_MACRO);
    }

    private void setup(WorkCaches workCaches) {
        if (sM1Seq < 0) {
            sM1Seq = workCaches.getNextWorkCachesSeq();
        }
        if (sM2Seq < 0) {
            sM2Seq = workCaches.getNextWorkCachesSeq();
        }
        if (sM3Seq < 0) {
            sM3Seq = workCaches.getNextWorkCachesSeq();
        }
        if (sM4Seq < 0) {
            sM4Seq = workCaches.getNextWorkCachesSeq();
        }
    }

    public boolean takePicture(Mat dst) {
        Mat m1 = mWorkCaches.getWorkMat(sM1Seq);
        if (mVideoCapture.grab()) {
            mVideoCapture.retrieve(m1);
            Mat m2 = mWorkCaches.getWorkMat(sM2Seq, m1.width(), m1.height(), m1.type());
            Mat m3 = mWorkCaches.getWorkMat(sM3Seq, m1.width(), m1.height(), m1.type());
            // Mat m4 = workCaches.getWorkMat(sM3Seq, m1.width(), m1.height(),
            // m1.type());
            { // Convert and rotate from raw data
              // BGR→RGB
                Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
                // Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);

                if (mRotateCamera) {
                    if (mReverseCamera) {
                        // rotate2deg
                        // Core.flip(m2, m3, 0);
                        Core.flip(m2, m3, 0);
                        Core.transpose(m3, m2);
                        Core.flip(m2, dst, 0);
                    } else {
                        // rotate2deg
                        Core.flip(m2, m3, 0);
                        Core.transpose(m3, dst);
                    }
                } else {
                    if (mReverseCamera) {
                        // rotate2deg
                        // Core.flip(m2, m3, 0);
                        Core.flip(m2, dst, 0);
                    } else {
                        // rotate2deg
                        m2.copyTo(dst);
                    }
                }
            }
            return true;
        } else {
            return false;
        }

    }

    public int getWidth() {
        return (int)((mRotateCamera) ? mPreviewSize.height : mPreviewSize.width);
    }

    public int getHeight() {
        return (int)((mRotateCamera) ? mPreviewSize.width : mPreviewSize.height);
    }

    public void release() {
        mVideoCapture.release();
    }

}
