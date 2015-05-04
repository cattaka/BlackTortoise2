
package net.blacktortoise.android.camera;

import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;

public interface ICameraManager {
    public void onResume();

    public void onPause();

    public boolean isEnablePreview();

    public void setEnablePreview(boolean enablePreview);

    public void setup(ICameraManagerAdapter cameraManagerAdapter,
            IBaseFragmentAdapter baseFragmentAdapter);
}
