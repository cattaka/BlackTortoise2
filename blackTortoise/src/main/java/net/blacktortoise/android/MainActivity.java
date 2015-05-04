
package net.blacktortoise.android;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;
import net.blacktortoise.android.fragment.ConnectFragment;
import net.blacktortoise.androidlib.BlackTortoiseFunctions;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.data.BtPacket;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.IActiveGeppaServiceListener;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.view.Menu;
import android.view.WindowManager;

public class MainActivity extends Activity implements IBaseFragmentAdapter {
    private static final int EVENT_ON_DEVICE_STATE_CHANGED = 1;

    private static final int EVENT_ON_RECEIVE_PACKET = 2;

    private MainActivity me = this;

    private BlackTortoiseServiceWrapper mServiceWrapper;

    private List<IDeviceAdapterListener<BtPacket>> mDeviceAdapterListeners;

    private int mServiceListenerSeq = -1;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            IActiveGeppaService service = IActiveGeppaService.Stub.asInterface(binder);
            mServiceWrapper = new BlackTortoiseServiceWrapper(service);
            if (mServiceListenerSeq < 0) {
                try {
                    mServiceListenerSeq = mServiceWrapper.getService().registerServiceListener(
                            mServiceListener);
                } catch (RemoteException e) {
                    // Nothing to do
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private IActiveGeppaServiceListener mServiceListener = new IActiveGeppaServiceListener.Stub() {
        @Override
        public void onReceivePacket(PacketWrapper packet) throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_RECEIVE_PACKET, new Object[] {
                    me, packet.getPacket()
            }).sendToTarget();
        }

        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code,
                final DeviceInfo deviceInfo) throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_DEVICE_STATE_CHANGED, new Object[] {
                    me, state, code, deviceInfo
            }).sendToTarget();
        }
    };

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Object[] objs = (Object[])msg.obj;
            MainActivity target = (MainActivity)objs[0];
            if (msg.what == EVENT_ON_RECEIVE_PACKET) {
                for (IDeviceAdapterListener<BtPacket> listener : target.mDeviceAdapterListeners) {
                    listener.onReceivePacket((BtPacket)objs[1]);
                }
            } else if (msg.what == EVENT_ON_DEVICE_STATE_CHANGED) {
                for (IDeviceAdapterListener<BtPacket> listener : target.mDeviceAdapterListeners) {
                    listener.onDeviceStateChanged((DeviceState)objs[1], (DeviceEventCode)objs[2],
                            (DeviceInfo)objs[3]);
                }
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        mDeviceAdapterListeners = new ArrayList<IDeviceAdapterListener<BtPacket>>();

        if (getFragmentManager().findFragmentById(R.id.primaryFragment) == null) {
            ConnectFragment fragment = new ConnectFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            ft.add(R.id.primaryFragment, fragment);

            // トランザクションをコミットする
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = BlackTortoiseFunctions.createServiceIntent();
        startService(service);
        bindService(service, mServiceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceListenerSeq >= 0) {
            try {
                mServiceWrapper.getService().unregisterServiceListener(mServiceListenerSeq);
                mServiceListenerSeq = -1;
            } catch (RemoteException e) {
                // Nothing to do
                throw new RuntimeException(e);
            }
        }
        unbindService(mServiceConnection);
    }

    @Override
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (withBackStack) {
            ft.addToBackStack(null);
        }
        ft.replace(R.id.primaryFragment, fragment);
        ft.commit();
    }

    @Override
    public BlackTortoiseServiceWrapper getServiceWrapper() {
        return mServiceWrapper;
    }

    @Override
    public boolean registerDeviceAdapterListener(IDeviceAdapterListener<BtPacket> listener) {
        if (!mDeviceAdapterListeners.contains(listener)) {
            mDeviceAdapterListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener<BtPacket> listener) {
        return mDeviceAdapterListeners.remove(listener);
    }

    @Override
    public void setKeepScreen(boolean flag) {
        if (flag) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
