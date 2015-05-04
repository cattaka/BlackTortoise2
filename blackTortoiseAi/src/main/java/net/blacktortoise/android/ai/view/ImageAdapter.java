
package net.blacktortoise.android.ai.view;

import java.util.List;

import net.blacktortoise.android.ai.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends ArrayAdapter<Bitmap> {
    private static class ViewHolder {
        ImageView thumbnailImage;

        TextView sizeText;
    }

    public ImageAdapter(Context context, Bitmap[] objects) {
        super(context, R.layout.item_image, objects);
    }

    public ImageAdapter(Context context, List<Bitmap> objects) {
        super(context, R.layout.item_image, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_image, null);
            vh = new ViewHolder();
            vh.thumbnailImage = (ImageView)convertView.findViewById(R.id.thumbnailImage);
            vh.sizeText = (TextView)convertView.findViewById(R.id.sizeText);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }
        Bitmap bm = getItem(position);
        if (bm != null) {
            vh.thumbnailImage.setImageBitmap(bm);
            vh.sizeText.setText(bm.getWidth() + "x" + bm.getHeight());
        } else {
            vh.thumbnailImage.setImageBitmap(null);
            vh.sizeText.setText("null");
        }

        return convertView;
    }
}
