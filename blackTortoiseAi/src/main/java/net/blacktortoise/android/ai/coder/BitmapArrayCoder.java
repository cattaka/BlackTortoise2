
package net.blacktortoise.android.ai.coder;

import java.io.ByteArrayInputStream;
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

public class BitmapArrayCoder implements IAccessor<Bitmap[]> {
    static BitmapArrayCoder instance;

    public static BitmapArrayCoder createAccessor(Class<Bitmap[]> clazz) {
        if (instance == null) {
            instance = new BitmapArrayCoder();
        }
        return instance;
    }

    @Override
    public Bitmap[] readFromStream(DataInputStream in) throws IOException {
        return null;
    }

    @Override
    public void writeToStream(DataOutputStream out, Bitmap[] value) throws IOException {

    }

    @Override
    public Bitmap[] readFromParcel(Parcel p) {
        return null;
    }

    @Override
    public void writeToParcel(Parcel p, Bitmap[] value) {

    }

    @Override
    public Bitmap[] readFromCursor(Cursor c, int idx) {
        return decode(c.getBlob(idx));
    }

    @Override
    public void putToContentValues(ContentValues values, String columnName, Bitmap[] value) {
        values.put(columnName, encode(value));
    }

    @Override
    public String stringValue(Bitmap[] value) {
        return null;
    }

    public static byte[] encode(Bitmap[] src) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        if (src == null) {
            return null;
        } else {
            try {
                out.writeInt(src.length);
                for (Bitmap bitmap : src) {
                    bitmap.compress(CompressFormat.PNG, 100, out);
                }
                out.flush();
                return bout.toByteArray();
            } catch (IOException e) {
                // Impossible
                throw new RuntimeException();
            }
        }
    }

    public static Bitmap[] decode(byte[] src) {
        if (src == null) {
            return null;
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(src));
            int n = in.readInt();
            Bitmap[] results = new Bitmap[n];
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    if (bitmap != null) {
                        results[i] = bitmap;
                    }
                }
            }
            return results;
        } catch (IOException e) {
            // Impossible
            throw new RuntimeException();
        }
    }
}
