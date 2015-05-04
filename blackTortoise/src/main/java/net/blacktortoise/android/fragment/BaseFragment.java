
package net.blacktortoise.android.fragment;

import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.data.BtPacket;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * This class abstract activity's functions so that avoid casting activity to
 * sub class.
 * 
 * @author cattaka
 */
public class BaseFragment extends Fragment implements IDeviceAdapterListener<BtPacket> {
    public interface IBaseFragmentAdapter {
        public Object getSystemService(String name);

        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

        public void unregisterReceiver(BroadcastReceiver receiver);

        public void replacePrimaryFragment(Fragment fragment, boolean withBackStack);

        public BlackTortoiseServiceWrapper getServiceWrapper();

        public boolean registerDeviceAdapterListener(IDeviceAdapterListener<BtPacket> listener);

        public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener<BtPacket> listener);

        public void runOnUiThread(Runnable action);

        public void setKeepScreen(boolean flag);
    }

    public IBaseFragmentAdapter getBaseFragmentAdapter() {
        return (IBaseFragmentAdapter)getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        getBaseFragmentAdapter().registerDeviceAdapterListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getBaseFragmentAdapter().unregisterDeviceAdapterListener(this);
    }

    public Context getContext() {
        return getActivity();
    }

    /** Please override if you need. */
    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        // none
    }

    /** Please override if you need. */
    @Override
    public void onReceivePacket(BtPacket packet) {
        // none
    }

    /** Do only delegation */
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return getActivity().registerReceiver(receiver, filter);
    }

    /** Do only delegation */
    public void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().unregisterReceiver(receiver);
    }

    /** Do only delegation */
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        getBaseFragmentAdapter().replacePrimaryFragment(fragment, withBackStack);
    }

    /** Do only delegation */
    public boolean registerDeviceAdapterListener(IDeviceAdapterListener<BtPacket> listener) {
        return getBaseFragmentAdapter().registerDeviceAdapterListener(listener);
    }

    /** Do only delegation */
    public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener<BtPacket> listener) {
        return getBaseFragmentAdapter().unregisterDeviceAdapterListener(listener);
    }

    /** Do only delegation */
    public void runOnUiThread(Runnable action) {
        getBaseFragmentAdapter().runOnUiThread(action);
    }

    public BlackTortoiseServiceWrapper getServiceWrapper() {
        return getBaseFragmentAdapter().getServiceWrapper();
    }

    public void setKeepScreen(boolean flag) {
        getBaseFragmentAdapter().setKeepScreen(flag);
    }

}
