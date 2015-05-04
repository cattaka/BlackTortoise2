
package net.blacktortoise.android.camera;

import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.OpCode;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RemoteCameraManager implements ICameraManager, IDeviceAdapterListener<BtPacket> {
    private ICameraManagerAdapter mCameraManagerAdapter;

    private IBaseFragmentAdapter mBaseFragmentAdapter;

    private boolean mEnablePreview;

    private boolean mRequested;

    public RemoteCameraManager() {
    }

    @Override
    public void setup(ICameraManagerAdapter cameraManagerAdapter,
            IBaseFragmentAdapter baseFragmentAdapter) {
        mCameraManagerAdapter = cameraManagerAdapter;
        mBaseFragmentAdapter = baseFragmentAdapter;
    }

    @Override
    public void onResume() {
        mBaseFragmentAdapter.registerDeviceAdapterListener(this);
        setEnablePreview(mEnablePreview);
    }

    @Override
    public void onPause() {
        mBaseFragmentAdapter.unregisterDeviceAdapterListener(this);
    }

    @Override
    public boolean isEnablePreview() {
        return mEnablePreview;
    }

    @Override
    public void setEnablePreview(boolean enablePreview) {
        mEnablePreview = enablePreview;
        if (mEnablePreview && !mRequested) {
            BlackTortoiseServiceWrapper wrapper = mBaseFragmentAdapter.getServiceWrapper();
            if (wrapper != null) {
                mRequested = wrapper.sendRequestCameraImage(0);
            }
        }
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        // none
    }

    @Override
    public void onReceivePacket(BtPacket packet) {
        if (packet.getOpCode() == OpCode.CAMERA_IMAGE) {
            int cameraIdx = packet.getData()[0];
            Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 1,
                    packet.getDataLen() - 1);
            if (bitmap != null) {
                mCameraManagerAdapter.onPictureTaken(bitmap, this);
                BlackTortoiseServiceWrapper wrapper = mBaseFragmentAdapter.getServiceWrapper();
                if (wrapper != null) {
                    mRequested = wrapper.sendRequestCameraImage(cameraIdx);
                }
            }
        }
    }
}
