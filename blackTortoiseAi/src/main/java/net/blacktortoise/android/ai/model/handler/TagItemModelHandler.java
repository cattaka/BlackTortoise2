package net.blacktortoise.android.ai.model.handler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import net.blacktortoise.android.ai.model.TagItemModel;

public class TagItemModelHandler {
    public static final String SQL_CREATE_TABLE = "CREATE TABLE tagItemModel(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,label TEXT,width INTEGER,height INTEGER,thumbnail BLOB,bitmaps BLOB)";
    public static final String TABLE_NAME = "tagItemModel";
    public static final String COLUMNS = "id,name,label,width,height,thumbnail,bitmaps";
    public static final String[] COLUMNS_ARRAY = new String[] {"id", "name", "label", "width", "height", "thumbnail", "bitmaps"};
    public static final int COL_INDEX_ID = 0;
    public static final int COL_INDEX_NAME = 1;
    public static final int COL_INDEX_LABEL = 2;
    public static final int COL_INDEX_WIDTH = 3;
    public static final int COL_INDEX_HEIGHT = 4;
    public static final int COL_INDEX_THUMBNAIL = 5;
    public static final int COL_INDEX_BITMAPS = 6;
    public static final String COL_NAME_ID = "id";
    public static final String COL_NAME_NAME = "name";
    public static final String COL_NAME_LABEL = "label";
    public static final String COL_NAME_WIDTH = "width";
    public static final String COL_NAME_HEIGHT = "height";
    public static final String COL_NAME_THUMBNAIL = "thumbnail";
    public static final String COL_NAME_BITMAPS = "bitmaps";
    public static long insert(SQLiteDatabase db, TagItemModel model) {
        ContentValues values = new ContentValues();
        values.put("name", model.getName());
        values.put("label", model.getLabel());
        values.put("width", model.getWidth());
        values.put("height", model.getHeight());
        values.put("thumbnail", ((model.getThumbnail() != null) ? net.blacktortoise.android.ai.coder.BitmapCoder.encode(model.getThumbnail()) : null));
        values.put("bitmaps", ((model.getBitmaps() != null) ? net.blacktortoise.android.ai.coder.BitmapArrayCoder.encode(model.getBitmaps()) : null));
        long key = db.insert(TABLE_NAME, null, values);
        model.setId(key);
        return key;
    }
    public static int update(SQLiteDatabase db, TagItemModel model) {
        ContentValues values = new ContentValues();
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(model.getId())};
        values.put("name", model.getName());
        values.put("label", model.getLabel());
        values.put("width", model.getWidth());
        values.put("height", model.getHeight());
        values.put("thumbnail", ((model.getThumbnail() != null) ? net.blacktortoise.android.ai.coder.BitmapCoder.encode(model.getThumbnail()) : null));
        values.put("bitmaps", ((model.getBitmaps() != null) ? net.blacktortoise.android.ai.coder.BitmapArrayCoder.encode(model.getBitmaps()) : null));
        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }
    public static int delete(SQLiteDatabase db, Long key) {
        String whereClause = "id=?";
        String[] whereArgs = new String[]{String.valueOf(key)};
        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
    public static TagItemModel findById(SQLiteDatabase db, Long id) {
        Cursor cursor = findCursorById(db, id);
        TagItemModel model = (cursor.moveToNext()) ? readCursorByIndex(cursor) : null;
        cursor.close();
        return model;
    }
    public static java.util.List<TagItemModel> findOrderByIdAsc(SQLiteDatabase db, int limit) {
        Cursor cursor = findCursorOrderByIdAsc(db, limit);
        java.util.List<TagItemModel> result = new java.util.ArrayList<TagItemModel>();
        while (cursor.moveToNext()) {
            result.add(readCursorByIndex(cursor));
        }
        cursor.close();
        return result;
    }
    public static Cursor findCursorById(SQLiteDatabase db, Long id) {
        String selection = "id=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, null);
    }
    public static Cursor findCursorOrderByIdAsc(SQLiteDatabase db, int limit) {
        String selection = "";
        String[] selectionArgs = new String[]{};
        String limitStr = (limit > 0) ? String.valueOf(limit) : null;
        String orderBy = "id asc";
        return db.query(TABLE_NAME, COLUMNS_ARRAY, selection, selectionArgs, null, null, orderBy, limitStr);
    }
    public static void readCursorByIndex(Cursor cursor, TagItemModel dest) {
        dest.setId(!cursor.isNull(0) ? cursor.getLong(0) : null);
        dest.setName(!cursor.isNull(1) ? cursor.getString(1) : null);
        dest.setLabel(!cursor.isNull(2) ? cursor.getString(2) : null);
        dest.setWidth(!cursor.isNull(3) ? cursor.getInt(3) : null);
        dest.setHeight(!cursor.isNull(4) ? cursor.getInt(4) : null);
        dest.setThumbnail(!cursor.isNull(5) ? net.blacktortoise.android.ai.coder.BitmapCoder.decode(cursor.getBlob(5)) : null);
        dest.setBitmaps(!cursor.isNull(6) ? net.blacktortoise.android.ai.coder.BitmapArrayCoder.decode(cursor.getBlob(6)) : null);
    }
    public static TagItemModel readCursorByIndex(Cursor cursor) {
        TagItemModel result = new TagItemModel();
        readCursorByIndex(cursor, result);
        return result;
    }
    public static void readCursorByName(Cursor cursor, TagItemModel dest) {
        int idx;
        idx = cursor.getColumnIndex("id");
        dest.setId(idx>=0 && !cursor.isNull(idx) ? cursor.getLong(idx) : null);
        idx = cursor.getColumnIndex("name");
        dest.setName(idx>=0 && !cursor.isNull(idx) ? cursor.getString(idx) : null);
        idx = cursor.getColumnIndex("label");
        dest.setLabel(idx>=0 && !cursor.isNull(idx) ? cursor.getString(idx) : null);
        idx = cursor.getColumnIndex("width");
        dest.setWidth(idx>=0 && !cursor.isNull(idx) ? cursor.getInt(idx) : null);
        idx = cursor.getColumnIndex("height");
        dest.setHeight(idx>=0 && !cursor.isNull(idx) ? cursor.getInt(idx) : null);
        idx = cursor.getColumnIndex("thumbnail");
        dest.setThumbnail(idx>=0 && !cursor.isNull(idx) ? net.blacktortoise.android.ai.coder.BitmapCoder.decode(cursor.getBlob(idx)) : null);
        idx = cursor.getColumnIndex("bitmaps");
        dest.setBitmaps(idx>=0 && !cursor.isNull(idx) ? net.blacktortoise.android.ai.coder.BitmapArrayCoder.decode(cursor.getBlob(idx)) : null);
    }
    public static TagItemModel readCursorByName(Cursor cursor) {
        TagItemModel result = new TagItemModel();
        readCursorByName(cursor, result);
        return result;
    }
    public static String toStringValue(Object arg) {
        return (arg != null) ? arg.toString() : null;
    }
}
