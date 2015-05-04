
package net.blacktortoise.android.fragment;

import java.util.Locale;

import net.blacktortoise.android.R;
import net.blacktortoise.android.camera.DeviceCameraManager;
import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.camera.ICameraManagerAdapter;
import net.blacktortoise.android.camera.RemoteCameraManager;
import net.blacktortoise.android.util.NormalizedOnTouchListener;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.OpCode;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ControllerFragment extends BaseFragment implements OnClickListener {
    private OnTouchListener mOnTouchListener = new NormalizedOnTouchListener() {
        long lastSendHeadTime;

        long lastSendMoveTime;

        @Override
        public boolean onTouch(View v, MotionEvent event, float rx, float ry) {
            if (v.getId() == R.id.controller_head) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendHeadTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float yaw = (1 - rx);
                    float pitch = (1 - ry);
                    ;
                    { // Displayes values on TextView
                        String text = String.format(Locale.getDefault(), "(yaw,pitch)=(%.2f,%.2f)",
                                yaw, pitch);
                        mHeadValueText.setText(text);
                    }
                    { // Sends command
                        BlackTortoiseServiceWrapper wrapper = getServiceWrapper();
                        if (wrapper != null) {
                            wrapper.sendHead(yaw, pitch);
                        }
                    }
                    lastSendHeadTime = t;
                }
            } else if (v.getId() == R.id.controller_move) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendMoveTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float forward = -(ry * 2 - 1);
                    float turn = rx * 2 - 1;
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        forward = 0;
                        turn = 0;
                    }
                    { // Displayes values on TextView
                        String text = String.format(Locale.getDefault(),
                                "(forward,turn)=(%.2f,%.2f)", forward, turn);
                        mMoveValueText.setText(text);
                    }
                    { // Sends command
                        BlackTortoiseServiceWrapper wrapper = getServiceWrapper();
                        if (wrapper != null) {
                            wrapper.sendMove(forward, turn);
                        }
                    }
                    lastSendMoveTime = t;
                }
            }
            return true;
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
        }
    };

    private TextView mHeadValueText;

    private TextView mMoveValueText;

    private SurfaceView mCameraSurfaceView;

    private ImageView mCameraImageView;

    private ICameraManager mCameraManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, null);

        // Pickup views
        mHeadValueText = (TextView)view.findViewById(R.id.head_value_text);
        mMoveValueText = (TextView)view.findViewById(R.id.move_value_text);
        mCameraSurfaceView = (SurfaceView)view.findViewById(R.id.cameraSurfaceView);
        mCameraImageView = (ImageView)view.findViewById(R.id.cameraImageView);

        // Binds event listeners
        view.findViewById(R.id.controller_head).setOnTouchListener(mOnTouchListener);
        view.findViewById(R.id.controller_move).setOnTouchListener(mOnTouchListener);
        view.findViewById(R.id.sendButton).setOnClickListener(this);
        view.findViewById(R.id.clearButton).setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareCameraManager(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        disposeCameraManager();
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
        if (packet.getOpCode() == OpCode.ECHO) {
            String str = new String(packet.getData());
            TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
            receivedText.setText(receivedText.getText() + str);
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
