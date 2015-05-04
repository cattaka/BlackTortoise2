
package net.blacktortoise.android.ai.coder;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Parcel;

import net.cattaka.util.cathandsgendroid.accessor.IAccessor;

public class BitmapCoder implements IAccessor<Bitmap> {
    static BitmapCoder instance;

    public static BitmapCoder createAccessor(Class<Bitmap> clazz) {
        if (instance == null) {
            instance = new BitmapCoder();
        }
        return instance;
    }

    @Override
    public Bitmap readFromStream(DataInputStream in) throws IOException {
        return null;
    }

    @Override
    public void writeToStream(DataOutputStream out, Bitmap value) throws IOException {

    }

    @Override
    public Bitmap readFromParcel(Parcel p) {
        return null;
    }

    @Override
    public void writeToParcel(Parcel p, Bitmap value) {

    }

    @Override
    public Bitmap readFromCursor(Cursor c, int idx) {
        return decode(c.getBlob(idx));
    }

    @Override
    public void putToContentValues(ContentValues values, String columnName, Bitmap value) {
        values.put(columnName, encode(value));
    }

    @Override
    public String stringValue(Bitmap value) {
        return null;
    }

    public static byte[] encode(Bitmap src) {
        if (src == null) {
            return null;
        } else {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                src.compress(CompressFormat.PNG, 100, bout);
                bout.flush();
                return bout.toByteArray();
            } catch (IOException e) {
                // Impossible
                throw new RuntimeException();
            }
        }
    }

    public static Bitmap decode(byte[] src) {
        if (src == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(src, 0, src.length);
        return bitmap;
    }
}
