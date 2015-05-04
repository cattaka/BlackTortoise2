
package net.blacktortoise.android.camera;

import android.graphics.Bitmap;
import android.view.SurfaceView;

public interface ICameraManagerAdapter {
    public SurfaceView getSurfaceView();

    public void onPictureTaken(Bitmap bitmap, ICameraManager cameraManager);
}
