
package net.blacktortoise.android.ai.core;

import org.opencv.core.Size;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyPreferences {
    private String KEY_ROTATE_CAMERA = "RotateCamera";

    private String KEY_REVERSE_CAMERA = "ReverseCamera";

    private String KEY_PREVIEW_SIZE = "PreviewSize";

    private String KEY_TAG_DETECTOR_ALGORISM = "TagDetectorAlgorism";

    private String KEY_GOOD_THRESHOLD = "GoodThreshold";

    private SharedPreferences mPreferences;

    private SharedPreferences.Editor mEditor;

    public MyPreferences(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void edit() {
        mEditor = mPreferences.edit();
    }

    public void commit() {
        mEditor.commit();
        mEditor = null;
    }

    public Size getPreviewSizeAsSize() {
        Size result = null;
        String str = getPreviewSize();
        if (str.indexOf('x') >= 0) {
            String[] ts = str.split("x");
            if (ts.length >= 2) {
                try {
                    double w = Double.parseDouble(ts[0]);
                    double h = Double.parseDouble(ts[1]);
                    result = new Size(w, h);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        if (result == null) {
            result = new Size(800, 600);
        }
        return result;
    }

    public boolean isRotateCamera() {
        return mPreferences.getBoolean(KEY_ROTATE_CAMERA, true);
    }

    public void putRotateCamera(boolean rotateCamera) {
        mEditor.putBoolean(KEY_ROTATE_CAMERA, rotateCamera);
    }

    public boolean isReverseCamera() {
        return mPreferences.getBoolean(KEY_REVERSE_CAMERA, false);
    }

    public void putReverseCamera(boolean reverseCamera) {
        mEditor.putBoolean(KEY_REVERSE_CAMERA, reverseCamera);
    }

    public String getPreviewSize() {
        return mPreferences.getString(KEY_PREVIEW_SIZE, "800x600");
    }

    public void putPreviewSize(String previewSize) {
        mEditor.putString(KEY_PREVIEW_SIZE, previewSize);
    }

    public TagDetectorAlgorism getTagDetectorAlgorism() {
        String str = mPreferences.getString(KEY_TAG_DETECTOR_ALGORISM, null);
        if (str == null) {
            return TagDetectorAlgorism.DEFAULT;
        }
        try {
            return TagDetectorAlgorism.valueOf(str);
        } catch (IllegalArgumentException e) {
            return TagDetectorAlgorism.DEFAULT;
        }

    }

    public void putTagDetectorAlgorism(TagDetectorAlgorism tagDetectorAlgorism) {
        mEditor.putString(KEY_TAG_DETECTOR_ALGORISM, String.valueOf(tagDetectorAlgorism));
    }

    public float getGoodThreshold() {
        return mPreferences.getFloat(KEY_GOOD_THRESHOLD, 0.6f);
    }

    public void putGoodThreshold(float goodThreshold) {
        mEditor.putFloat(KEY_GOOD_THRESHOLD, goodThreshold);
    }
}
