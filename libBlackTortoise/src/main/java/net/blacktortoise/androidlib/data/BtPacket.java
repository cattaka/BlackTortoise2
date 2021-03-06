
package net.blacktortoise.androidlib.data;

import net.cattaka.libgeppa.data.IPacket;
import android.os.Parcel;
import android.os.Parcelable;

public class BtPacket implements IPacket, Parcelable {
    private static final long serialVersionUID = 1L;

    private OpCode opCode;

    private int dataLen;

    private byte[] data;

    private BtPacket() {
    }

    public BtPacket(OpCode opCode, int dataLen, byte[] data) {
        super();
        this.opCode = opCode;
        this.dataLen = dataLen;
        this.data = data;
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public void setOpCode(OpCode opCode) {
        this.opCode = opCode;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(opCode.getValue());
        dest.writeInt(this.dataLen);
        dest.writeByteArray(data, 0, dataLen);
    }

    public static final Parcelable.Creator<BtPacket> CREATOR = new Parcelable.Creator<BtPacket>() {
        @Override
        public BtPacket[] newArray(int size) {
            return new BtPacket[size];
        }

        @Override
        public BtPacket createFromParcel(Parcel source) {
            BtPacket p = new BtPacket();
            p.opCode = OpCode.fromValue(source.readByte());
            p.dataLen = source.readInt();
            p.data = new byte[p.dataLen];
            source.readByteArray(p.data);
            return p;
        }
    };
}
