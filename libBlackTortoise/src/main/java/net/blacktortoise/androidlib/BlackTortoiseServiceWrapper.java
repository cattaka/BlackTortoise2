
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.OpCode;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.os.RemoteException;

public class BlackTortoiseServiceWrapper {
    private byte[] mBuffer = new byte[0x100];

    private IActiveGeppaService mService;

    public BlackTortoiseServiceWrapper(IActiveGeppaService service) {
        super();
        mService = service;
    }

    public IActiveGeppaService getService() {
        return mService;
    }

    public boolean sendMove(float forward, float turn) {
        float leftMotor = Math.min(1, 1 + turn * 2) * forward;
        float rightMotor = Math.min(1, 1 - turn * 2) * forward;

        float leftMotor1 = (leftMotor > 0) ? leftMotor : 0;
        float leftMotor2 = (leftMotor < 0) ? -leftMotor : 0;
        float rightMotor1 = (rightMotor > 0) ? rightMotor : 0;
        float rightMotor2 = (rightMotor < 0) ? -rightMotor : 0;

        return sendMove(leftMotor1, leftMotor2, rightMotor1, rightMotor2);
    }

    public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1, float rightMotor2) {
        mBuffer[0] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * leftMotor1)));
        mBuffer[1] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * leftMotor2)));
        mBuffer[2] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * rightMotor1)));
        mBuffer[3] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * rightMotor2)));
        BtPacket packet = new BtPacket(OpCode.MOVE, 4, mBuffer);
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean sendHead(float yaw, float pitch) {
        mBuffer[0] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * yaw)));
        mBuffer[1] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * pitch)));
        BtPacket packet = new BtPacket(OpCode.HEAD, 2, mBuffer);
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean sendEcho(byte[] data) {
        BtPacket packet = new BtPacket(OpCode.ECHO, data.length, data);
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean sendRequestCameraImage(int cameraIdx) {
        mBuffer[0] = (byte)cameraIdx;
        BtPacket packet = new BtPacket(OpCode.REQUEST_CAMERA_IMAGE, 1, mBuffer);
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean sendPacket(BtPacket packet) {
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public DeviceInfo getCurrentDeviceInfo() {
        try {
            return mService.getCurrentDeviceInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

}
