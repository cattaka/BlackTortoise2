
package net.blacktortoise.android.ai.core;

import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.cattaka.libgeppa.IActiveGeppaService;

public class BlackTortoiseServiceWrapperEx extends BlackTortoiseServiceWrapper {
    private float mLastForward;

    private float mLastTurn;

    private float mLastYaw;

    private float mLastPitch;

    public BlackTortoiseServiceWrapperEx(IActiveGeppaService service) {
        super(service);
    }

    @Override
    public boolean sendMove(float forward, float turn) {
        mLastForward = forward;
        mLastTurn = turn;
        return super.sendMove(forward, turn);
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        yaw = Math.min(1f, Math.max(-1f, yaw));
        pitch = Math.min(1f, Math.max(-1f, pitch));

        mLastYaw = yaw;
        mLastPitch = pitch;
        // yaw = Math.min(0.3f, Math.max(-0.3f, yaw));
        // pitch = Math.min(0.3f, Math.max(-0.3f, pitch));
        return super.sendHead((yaw + 1) / 2, (pitch + 1) / 2);
    }

    public float getLastForward() {
        return mLastForward;
    }

    public float getLastTurn() {
        return mLastTurn;
    }

    public float getLastYaw() {
        return mLastYaw;
    }

    public float getLastPitch() {
        return mLastPitch;
    }

}
