
package net.blacktortoise.android.ai.util;

import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.util.SparseArray;

public class WorkCaches {

    private SparseArray<Mat> mWorkMats;

    private SparseArray<Bitmap> mWorkBitmaps;

    private int mNextWorkCachesSeq;

    public WorkCaches() {
        mWorkMats = new SparseArray<Mat>();
        mWorkBitmaps = new SparseArray<Bitmap>();
    }

    public Mat getWorkMat(int idx) {
        Mat mat = mWorkMats.get(idx);
        if (mat != null) {
            return mat;
        }

        mat = new Mat();
        mWorkMats.put(idx, mat);
        return mat;
    }

    public Mat getWorkMat(int idx, Mat src) {
        return getWorkMat(idx, src.width(), src.height(), src.type());
    }

    public Mat getWorkMat(int idx, int width, int height, int type) {
        Mat mat = mWorkMats.get(idx);
        if (mat != null && mat.width() == width && mat.height() == height && mat.type() != type) {
            return mat;
        }
        if (mat != null) {
            mat.release();
        }

        mat = new Mat(height, width, type);
        mWorkMats.put(idx, mat);
        return mat;
    }

    public Bitmap getWorkBitmap(int idx, int width, int height) {
        Bitmap bitmap = mWorkBitmaps.get(idx);
        if (bitmap != null && width == bitmap.getWidth() && height == bitmap.getHeight()) {
            return bitmap;
        }
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mWorkBitmaps.put(idx, bitmap);
        return bitmap;
    }

    public void release() {
        for (int i = 0; i < mWorkMats.size(); i++) {
            mWorkMats.valueAt(i).release();
        }
        mWorkMats.clear();
        for (int i = 0; i < mWorkBitmaps.size(); i++) {
            mWorkBitmaps.valueAt(i).recycle();
        }
        mWorkBitmaps.clear();
    }

    public int getNextWorkCachesSeq() {
        return ++mNextWorkCachesSeq;
    }

}
