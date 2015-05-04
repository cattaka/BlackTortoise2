
package net.blacktortoise.android.ai.model;

import java.util.List;

import net.blacktortoise.android.ai.coder.BitmapArrayCoder;
import net.blacktortoise.android.ai.coder.BitmapCoder;
import net.cattaka.util.cathandsgendroid.annotation.DataModel;
import net.cattaka.util.cathandsgendroid.annotation.DataModelAttrs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

@DataModel(find = {
        "id", ":id"
})
public class TagItemModel {
    @DataModelAttrs(primaryKey = true)
    private Long id;

    private String name;

    private String label;

    private Integer width;

    private Integer height;

    @DataModelAttrs(accessor = BitmapCoder.class)
    private Bitmap thumbnail;

    @DataModelAttrs(accessor = BitmapArrayCoder.class)
    private Bitmap[] bitmaps;

    public void updateThumbnail() {
        if (bitmaps != null && bitmaps.length > 0) {
            Bitmap bt = bitmaps[0];
            thumbnail = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888);
            float scale = Math.min((float)thumbnail.getWidth() / (float)bt.getWidth(),
                    (float)thumbnail.getHeight() / (float)bt.getHeight());
            Matrix m = new Matrix();
            m.setScale(scale, scale);
            Canvas canvas = new Canvas(thumbnail);
            canvas.drawBitmap(bt, m, null);
        } else {
            thumbnail = null;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Bitmap[] getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(Bitmap[] bitmaps) {
        this.bitmaps = bitmaps;
    }

}
