
package net.blacktortoise.android.fragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.blacktortoise.android.R;
import net.blacktortoise.android.camera.DeviceCameraManager;
import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.camera.ICameraManagerAdapter;
import net.blacktortoise.android.camera.RemoteCameraManager;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.BtPacketFactory;
import net.blacktortoise.androidlib.data.OpCode;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.SocketState;
import net.cattaka.libgeppa.thread.ClientThread;
import net.cattaka.libgeppa.thread.ServerThread;
import net.cattaka.libgeppa.thread.ServerThread.IServerThreadListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ServerModeFragment extends BaseFragment implements OnClickListener {
    private ServerThread<BtPacket> mServerThread;

    private IServerThreadListener<BtPacket> mServerThreadListener = new IServerThreadListener<BtPacket>() {
        @Override
        public void onClientConnected(ClientThread<BtPacket> target) {
            refleshRomoteControllerList();
        }

        @Override
        public void onClientDisconnected(ClientThread<BtPacket> target) {
            refleshRomoteControllerList();
        }

        @Override
        public void onSocketStateChanged(SocketState socketState) {
            // none
        }

        /**
         * When receive packet from remote client, pass it to USB device.
         */
        @Override
        public void onReceivePacket(ClientThread<BtPacket> from, BtPacket packet) {
            getServiceWrapper().sendPacket(packet);
            if (packet.getOpCode() == OpCode.REQUEST_CAMERA_IMAGE) {
                synchronized (mRequestedCameraImageClients) {
                    mRequestedCameraImageClients.add(from);
                }
            }
        }
    };

    private ICameraManagerAdapter mCameraManagerAdapter = new ICameraManagerAdapter() {
        @Override
        public SurfaceView getSurfaceView() {
            return mCameraSurfaceView;
        }

        @Override
        public void onPictureTaken(Bitmap bitmap, ICameraManager cameraManager) {
            mCameraImageView.setImageBitmap(bitmap);
            synchronized (mRequestedCameraImageClients) {
                if (mRequestedCameraImageClients.size() > 0) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    bout.write(0); // cameraIdx = 0
                    bitmap.compress(CompressFormat.JPEG, 50, bout);
                    byte[] data = bout.toByteArray();
                    BtPacket packet = new BtPacket(OpCode.CAMERA_IMAGE, data.length, data);
                    for (ClientThread<BtPacket> ct : mRequestedCameraImageClients) {
                        ct.sendPacket(packet);
                    }
                    mRequestedCameraImageClients.clear();
                }
            }
        }
    };

    private ListView mConnectedControllerList;

    private SurfaceView mCameraSurfaceView;

    private ImageView mCameraImageView;

    private ICameraManager mCameraManager;

    private Set<ClientThread<BtPacket>> mRequestedCameraImageClients;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestedCameraImageClients = new HashSet<ClientThread<BtPacket>>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_mode, null);

        // Pickup views
        mConnectedControllerList = (ListView)view.findViewById(R.id.connectedControllerList);
        mCameraSurfaceView = (SurfaceView)view.findViewById(R.id.cameraSurfaceView);
        mCameraImageView = (ImageView)view.findViewById(R.id.cameraImageView);

        // Bind event listeners
        mCameraImageView.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mServerThread != null) {
            mServerThread.stopThread();
        }
        mServerThread = new ServerThread<BtPacket>(5000, new BtPacketFactory(),
                mServerThreadListener);
        try {
            mServerThread.startThread();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
        prepareCameraManager(null);
        setKeepScreen(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mServerThread != null) {
            mServerThread.stopThread();
            mServerThread = null;
        }
        disposeCameraManager();
        setKeepScreen(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendButton) {
            BlackTortoiseServiceWrapper wrapper = getServiceWrapper();
            if (wrapper != null) {
                EditText sendText = (EditText)getView().findViewById(R.id.sendEdit);
                byte[] data = String.valueOf(sendText.getText()).getBytes();
                BtPacket packet = new BtPacket(OpCode.ECHO, data.length, data);
                wrapper.sendPacket(packet);
            }
        } else if (v.getId() == R.id.clearButton) {
            TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
            receivedText.setText("");
        } else if (v.getId() == R.id.cameraImageView) {
            mCameraManager.setEnablePreview(!mCameraManager.isEnablePreview());
        }

    }

    @Override
    public void onReceivePacket(BtPacket packet) {
        super.onReceivePacket(packet);
        mServerThread.sendPacket(packet);
    }

    public void refleshRomoteControllerList() {
        if (mServerThread != null) {
            List<String> labels = new ArrayList<String>();
            for (ClientThread<BtPacket> ct : mServerThread.getClientThreads()) {
                labels.add(ct.getLabel());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, labels);
            mConnectedControllerList.setAdapter(adapter);
        } else {
            // Impossible
        }

    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        super.onDeviceStateChanged(state, code, deviceInfo);
        switch (state) {
            case CONNECTED:
                prepareCameraManager(deviceInfo);
                break;
            default:
                disposeCameraManager();
                break;
        }
    }

    private void prepareCameraManager(DeviceInfo info) {
        if (info == null) {
            BlackTortoiseServiceWrapper wrapper = getServiceWrapper();
            if (wrapper != null) {
                info = wrapper.getCurrentDeviceInfo();
            }
        }
        if (info != null && mCameraManager == null) {
            if (info.isSupportCamera()) {
                mCameraManager = new RemoteCameraManager();
            } else {
                mCameraManager = new DeviceCameraManager();
            }
            mCameraManager.setup(mCameraManagerAdapter, getBaseFragmentAdapter());
            mCameraManager.onResume();
            mCameraManager.setEnablePreview(true);
        }
    }

    private void disposeCameraManager() {
        if (mCameraManager != null) {
            mCameraManager.onPause();
            mCameraManager = null;
        }
    }
}
