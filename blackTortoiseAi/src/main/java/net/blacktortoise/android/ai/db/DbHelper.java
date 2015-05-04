
package net.blacktortoise.android.ai.db;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.model.handler.TagItemModelHandler;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ai.db";

    public DbHelper(Context context) {
        this(context, DB_NAME);
    }

    public DbHelper(Context context, String name) {
        super(context, name, null, 1, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TagItemModelHandler.SQL_CREATE_TABLE);
        ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // none
    }

    public List<TagItemModel> findTagItemModel(boolean withBitmaps) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            if (withBitmaps) {
                return TagItemModelHandler.findOrderByIdAsc(db, -1);
            } else {
                String[] columnsArray = new String[] {
                        "id", "name", "label", "width", "height", "thumbnail"
                };
                String selection = "";
                String[] selectionArgs = new String[] {};
                String limitStr = null;// (limit > 0) ? String.valueOf(limit) :
                                       // null;
                String orderBy = "id asc";
                Cursor cursor = db.query(TagItemModelHandler.TABLE_NAME, columnsArray, selection,
                        selectionArgs, null, null, orderBy, limitStr);
                List<TagItemModel> result = new ArrayList<TagItemModel>();
                while (cursor.moveToNext()) {
                    result.add(TagItemModelHandler.readCursorByName(cursor));
                }
                cursor.close();
                return result;
            }
        } finally {
            db.close();
        }
    }

    public TagItemModel findTagItemModelById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            return TagItemModelHandler.findById(db, id);
        } finally {
            db.close();
        }
    }

    public boolean registerTagItemModel(TagItemModel model) {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            if (model.getId() != null) {
                result = TagItemModelHandler.update(db, model) > 0;
            } else {
                result = TagItemModelHandler.insert(db, model) > 0;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }

    public boolean deleteTagItemModel(long id) {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            result = TagItemModelHandler.delete(db, id) > 0;
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }
}
