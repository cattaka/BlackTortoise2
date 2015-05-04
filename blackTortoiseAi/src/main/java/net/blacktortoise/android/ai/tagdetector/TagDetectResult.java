
package net.blacktortoise.android.ai.tagdetector;

import org.opencv.core.Point;

import android.util.SparseBooleanArray;

public class TagDetectResult {
    private int mTagKey;

    private Point[] mPoints;

    private SparseBooleanArray mDetectedLevels;

    public TagDetectResult() {
        super();
    }

    public TagDetectResult(int tagKey, Point[] points) {
        super();
        mTagKey = tagKey;
        mPoints = points;
    }

    public int getTagKey() {
        return mTagKey;
    }

    public void setTagKey(int tagKey) {
        mTagKey = tagKey;
    }

    public Point[] getPoints() {
        return mPoints;
    }

    public void setPoints(Point[] points) {
        mPoints = points;
    }

    public SparseBooleanArray getDetectedLevels() {
        return mDetectedLevels;
    }

    public void setDetectedLevels(SparseBooleanArray detectedLevels) {
        mDetectedLevels = detectedLevels;
    }

}
