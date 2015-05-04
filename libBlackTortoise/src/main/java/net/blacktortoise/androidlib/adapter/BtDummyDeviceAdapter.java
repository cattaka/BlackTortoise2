
package net.blacktortoise.androidlib.adapter;

import java.io.ByteArrayOutputStream;

import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.OpCode;
import net.cattaka.libgeppa.adapter.IDeviceAdapter;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;

public class BtDummyDeviceAdapter implements IDeviceAdapter<BtPacket> {
    private IDeviceAdapterListener<BtPacket> mListener;

    private DeviceState mLastDeviceState = DeviceState.INITIAL;

    private Handler mHandler;

    private Runnable mDummyImageGenerator = new Runnable() {
        private Bitmap mBitmap;

        private Paint mPaint;

        {
            mBitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
            mPaint = new Paint();
            mPaint.setColor(0xFF000000);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(50);
        }

        @Override
        public void run() {
            Canvas canvas = new Canvas(mBitmap);
            canvas.drawColor(0xFF7F7F7F);
            // canvas.drawText(String.valueOf(mCount++), 30, 30, mPaint);
            int s = Math.max(mBitmap.getWidth(), mBitmap.getHeight()) / 10;
            int offset = (int)(SystemClock.elapsedRealtime() / 30) % s;
            for (int x = -offset; x < mBitmap.getWidth(); x += s * 2) {
                for (int y = -offset; y < mBitmap.getHeight(); y += s * 2) {
                    canvas.drawRect(x, y, x + s, y + s, mPaint);
                    canvas.drawRect(x + s, y + s, x + s * 2, y + s * 2, mPaint);
                }
            }

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(0);// cameraIndex
            mBitmap.compress(CompressFormat.JPEG, 50, bout);
            byte[] data = bout.toByteArray();
            BtPacket packet = new BtPacket(OpCode.CAMERA_IMAGE, data.length, data);
            mListener.onReceivePacket(packet);
        }
    };

    public BtDummyDeviceAdapter(IDeviceAdapterListener<BtPacket> listener, Handler handler) {
        mListener = listener;
        mHandler = handler;
    }

    @Override
    public void startAdapter() throws InterruptedException {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DeviceInfo deviceInfo = getDeviceInfo();
                mListener.onDeviceStateChanged(DeviceState.CONNECTING, DeviceEventCode.UNKNOWN,
                        deviceInfo);
                mListener.onDeviceStateChanged(DeviceState.CONNECTED, DeviceEventCode.UNKNOWN,
                        deviceInfo);
                mLastDeviceState = DeviceState.CONNECTED;
            }
        });
    }

    @Override
    public void stopAdapter() throws InterruptedException {
        mHandler.removeCallbacks(mDummyImageGenerator);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DeviceInfo deviceInfo = getDeviceInfo();
                mListener.onDeviceStateChanged(DeviceState.CLOSED, DeviceEventCode.DISCONNECTED,
                        deviceInfo);
                mLastDeviceState = DeviceState.CLOSED;
            }
        });
    }

    @Override
    public DeviceState getDeviceState() {
        return mLastDeviceState;
    }

    @Override
    public boolean sendPacket(BtPacket packet) {
        if (packet.getOpCode() == OpCode.ECHO) {
            byte[] data = new byte[packet.getDataLen()];
            System.arraycopy(packet.getData(), 0, data, 0, data.length);
            final BtPacket respPacket = new BtPacket(OpCode.ECHO, data.length, data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onReceivePacket(respPacket);
                }
            });
        } else if (packet.getOpCode() == OpCode.REQUEST_CAMERA_IMAGE) {
            mHandler.postDelayed(mDummyImageGenerator, 100);
        }
        return true;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return DeviceInfo.createDummy(true);
    }
}
