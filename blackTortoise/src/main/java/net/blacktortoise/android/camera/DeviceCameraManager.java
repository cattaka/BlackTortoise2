
package net.blacktortoise.android.camera;

import java.io.IOException;

import net.blacktortoise.android.Constants;
import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

public class DeviceCameraManager implements ICameraManager {
    private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceExist = true;
            updateCameraState();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceExist = false;
            updateCameraState();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceExist = true;
            updateCameraState();
        }
    };

    public class PreviewCallbackEx implements PreviewCallback {
        private Size size;

        private Bitmap cacheBitmap;

        private int[] cacheRgb;

        private boolean reverse;

        private boolean rotate;

        public PreviewCallbackEx(Camera camera, boolean reverse, boolean rotate) {
            this.reverse = reverse;
            this.rotate = rotate;
            size = camera.getParameters().getPreviewSize();
            cacheRgb = new int[size.width * size.height];
            if (rotate) {
                cacheBitmap = Bitmap
                        .createBitmap(size.height, size.height, Bitmap.Config.ARGB_8888);
            } else {
                cacheBitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            decodeYUV(cacheRgb, data, size.width, size.height, reverse, rotate); // 変換
            cacheBitmap.setPixels(cacheRgb, 0, cacheBitmap.getWidth(), 0, 0,
                    cacheBitmap.getWidth(), cacheBitmap.getHeight());
            mAdapter.onPictureTaken(cacheBitmap, DeviceCameraManager.this);
        }
    };

    private Camera mCamera;

    private ICameraManagerAdapter mAdapter;

    private boolean mEnablePreview;

    private boolean mSurfaceExist;

    private boolean mResumed;

    private SurfaceHolder mSurfaceHolder;

    public DeviceCameraManager() {
        super();
    }

    @Override
    public void setup(ICameraManagerAdapter cameraManagerAdapter,
            IBaseFragmentAdapter baseFragmentAdapter) {
        mAdapter = cameraManagerAdapter;
    }

    @Override
    public void onResume() {
        mSurfaceHolder = mAdapter.getSurfaceView().getHolder();
        mSurfaceHolder.addCallback(mSurfaceListener);
        mResumed = true;
        updateCameraState();
    }

    @Override
    public void onPause() {
        mResumed = false;
        updateCameraState();
        mSurfaceHolder.removeCallback(mSurfaceListener);
    }

    @Override
    public boolean isEnablePreview() {
        return mEnablePreview;
    }

    @Override
    public void setEnablePreview(boolean enablePreview) {
        mEnablePreview = enablePreview;
        updateCameraState();
    }

    public void updateCameraState() {
        if (mEnablePreview && mSurfaceExist && mResumed) {
            if (mCamera == null) {
                mCamera = Camera.open();
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                } catch (IOException e) {
                    // Ignore
                    Log.e(Constants.TAG, e.getMessage(), e);
                }
                PreviewCallbackEx previewCallback = new PreviewCallbackEx(mCamera, true, true);
                mCamera.getParameters().setPreviewFpsRange(5, 10);
                mCamera.getParameters().setPreviewSize(320, 240);
                mCamera.setPreviewCallback(previewCallback);
                mCamera.startPreview();
            }
        } else {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    }

    public static void decodeYUV(int[] out, byte[] fg, int width, int height, boolean reverse,
            boolean rotate) throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length + " < minimum " + sz
                    * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4)
                        + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                if (reverse) {
                    if (rotate) {
                        out[height * (width - i - 1) + (height - j - 1)] = 0xff000000 + (B << 16)
                                + (G << 8) + R;
                    } else {
                        out[width * (height - j - 1) + i] = 0xff000000 + (B << 16) + (G << 8) + R;
                    }
                } else {
                    if (rotate) {
                        out[height * i + (height - j - 1)] = 0xff000000 + (B << 16) + (G << 8) + R;
                    } else {
                        out[pixPtr] = 0xff000000 + (B << 16) + (G << 8) + R;
                    }
                }
                pixPtr++;
            }
        }

    }
}
