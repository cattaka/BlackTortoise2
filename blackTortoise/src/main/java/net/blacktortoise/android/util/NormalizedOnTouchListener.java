
package net.blacktortoise.android.util;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This is wrapped listener class. This normalize the touch position.
 * 
 * @author cattaka
 */
abstract public class NormalizedOnTouchListener implements OnTouchListener {

    public NormalizedOnTouchListener() {
        super();
    }

    @Override
    public final boolean onTouch(View v, MotionEvent event) {
        float rx = (event.getX() / v.getWidth());
        float ry = (event.getY() / v.getHeight());

        rx = Math.max(0f, Math.min(1f, rx));
        ry = Math.max(0f, Math.min(1f, ry));

        return onTouch(v, event, rx, ry);
    }

    /**
     * This is wrapped listener function.
     * 
     * @param v same as the original
     * @param event same as the original
     * @param rx Normalized x position between 0 and 1.
     * @param ry Normalized x position between 0 and 1.
     * @return
     */
    abstract public boolean onTouch(View v, MotionEvent event, float rx, float ry);
}
