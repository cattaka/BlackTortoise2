
package net.blacktortoise.android.ai.thread;

import android.graphics.Bitmap;

public class IndicatorToken {
    private Bitmap captureImage;

    private float forward;

    private float turn;

    private float yaw;

    private float pitch;

    public IndicatorToken() {
    }

    public IndicatorToken(Bitmap captureImage, float forward, float turn, float yaw, float pitch) {
        super();
        this.captureImage = captureImage;
        this.forward = forward;
        this.turn = turn;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Bitmap getCaptureImage() {
        return captureImage;
    }

    public void setCaptureImage(Bitmap captureImage) {
        this.captureImage = captureImage;
    }

    public float getForward() {
        return forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public float getTurn() {
        return turn;
    }

    public void setTurn(float turn) {
        this.turn = turn;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
