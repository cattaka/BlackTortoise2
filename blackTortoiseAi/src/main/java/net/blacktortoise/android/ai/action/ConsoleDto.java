
package net.blacktortoise.android.ai.action;

import org.opencv.core.Mat;

public class ConsoleDto {
    private float mLastForward;

    private float mLastTurn;

    private float mLastYaw;

    private float mLastPitch;

    private Mat mResultMat;

    public float getLastForward() {
        return mLastForward;
    }

    public void setLastForward(float lastForward) {
        mLastForward = lastForward;
    }

    public float getLastTurn() {
        return mLastTurn;
    }

    public void setLastTurn(float lastTurn) {
        mLastTurn = lastTurn;
    }

    public float getLastYaw() {
        return mLastYaw;
    }

    public void setLastYaw(float lastYaw) {
        mLastYaw = lastYaw;
    }

    public float getLastPitch() {
        return mLastPitch;
    }

    public void setLastPitch(float lastPitch) {
        mLastPitch = lastPitch;
    }

    public Mat getResultMat() {
        return mResultMat;
    }

    public void setResultMat(Mat resultMat) {
        mResultMat = resultMat;
    }

}
