
package net.blacktortoise.android.ai.util;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MyCapture {
    static Handler sHander = new Handler();

    private static int sM1Seq = -1;

    private static int sM2Seq = -1;

    private static int sM3Seq = -1;

    private static int sM4Seq = -1;

    private Camera mVideoCapture;

    private WorkCaches mWorkCaches;

    private boolean mRotateCamera = true;

    private boolean mReverseCamera = false;

    private Size mRequestedPreviewSize;

    private Camera.Size mPreviewSize;

    private IMyCaptureListener mMyCaptureListener;

    private Mat mPicture = new Mat();

    public MyCapture(WorkCaches workCaches) {
        super();
        mWorkCaches = workCaches;
        setup(workCaches);
    }

    public void setMyCaptureListener(IMyCaptureListener mMyCaptureListener) {
        this.mMyCaptureListener = mMyCaptureListener;
    }

    public void open(SurfaceHolder holder, boolean rotateCamera, boolean reverceCamera, Size requestedPreviewSize) throws IOException {
        mVideoCapture = Camera.open();
        mRotateCamera = rotateCamera;
        mReverseCamera = reverceCamera;
        mRequestedPreviewSize = requestedPreviewSize;
        Camera.Parameters params = mVideoCapture.getParameters();
        Camera.Size size = null;
        {
            List<Camera.Size> ss = params.getSupportedPreviewSizes();
            for (Camera.Size s : ss) {
                if ((s.width) == ((int)requestedPreviewSize.width)
                        && (s.height) == ((int)requestedPreviewSize.height)) {
                    size = s;
                    break;
                }
            }
            if (size == null) {
                for (int i = ss.size() - 1; i >= 0; i--) {
                    Camera.Size s = ss.get(i);
                    if ((s.width) <= ((int)requestedPreviewSize.width)
                            && (s.height) <= ((int)requestedPreviewSize.height)) {
                        size = s;
                        break;
                    }
                }
            }
        }
        if (size == null) {
            size = params.getPreviewSize();
        }
        params.setPreviewSize(size.width, size.height);
        mPreviewSize = size;
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        params.setPreviewFormat(ImageFormat.NV21);
        mVideoCapture.setParameters(params);

        mVideoCapture.setPreviewCallback(mPreviewCallback);
        mVideoCapture.setPreviewDisplay(holder);
        mVideoCapture.startPreview();
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

    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        final Mat mFrame = new Mat();
        final Mat mTemp = new Mat();
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mFrame.rows() != mPreviewSize.height + (mPreviewSize.height/2) || mFrame.cols() != mPreviewSize.width) {
                mFrame.create(mPreviewSize.height + (mPreviewSize.height/2), mPreviewSize.width, CvType.CV_8UC1);
            }
            mFrame.put(0, 0, data);
            Imgproc.cvtColor(mFrame, mTemp, Imgproc.COLOR_YUV2RGBA_NV21, 4);
            if (mReverseCamera) {
                Core.flip(mTemp, mTemp, -1);
            } else {
                Core.flip(mTemp, mTemp, 0);
            }
            Core.transpose(mTemp, mPicture);

            if (mMyCaptureListener != null) {
                mMyCaptureListener.onTakePicture(mPicture);
            }
        }
    };

    public boolean getPicture(Mat dest) {
        if (mPicture.size().width == 0 || mPicture.height() == 0) {
            return false;
        } else {
            mPicture.copyTo(dest);
            return true;
        }
    }

//    public boolean takePicture(Mat dst) {
//        Mat m1 = mWorkCaches.getWorkMat(sM1Seq);
//        if (mVideoCapture.grab()) {
//            mVideoCapture.retrieve(m1);
//            Mat m2 = mWorkCaches.getWorkMat(sM2Seq, m1.width(), m1.height(), m1.type());
//            Mat m3 = mWorkCaches.getWorkMat(sM3Seq, m1.width(), m1.height(), m1.type());
//            // Mat m4 = workCaches.getWorkMat(sM3Seq, m1.width(), m1.height(),
//            // m1.type());
//            { // Convert and rotate from raw data
//              // BGR→RGB
//                Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
//                // Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
//
//                if (mRotateCamera) {
//                    if (mReverseCamera) {
//                        // rotate2deg
//                        // Core.flip(m2, m3, 0);
//                        Core.flip(m2, m3, 0);
//                        Core.transpose(m3, m2);
//                        Core.flip(m2, dst, 0);
//                    } else {
//                        // rotate2deg
//                        Core.flip(m2, m3, 0);
//                        Core.transpose(m3, dst);
//                    }
//                } else {
//                    if (mReverseCamera) {
//                        // rotate2deg
//                        // Core.flip(m2, m3, 0);
//                        Core.flip(m2, dst, 0);
//                    } else {
//                        // rotate2deg
//                        m2.copyTo(dst);
//                    }
//                }
//            }
//            return true;
//        } else {
//            return false;
//        }
//
//    }

    public int getWidth() {
        return (int)((mRotateCamera) ? mPreviewSize.height : mPreviewSize.width);
    }

    public int getHeight() {
        return (int)((mRotateCamera) ? mPreviewSize.width : mPreviewSize.height);
    }

    public void release() throws IOException {
        mVideoCapture.stopPreview();
        mVideoCapture.setPreviewCallback(null);
        mVideoCapture.release();
        mVideoCapture = null;
    }

    public interface IMyCaptureListener {
        void onTakePicture(Mat mat);
    }
}
