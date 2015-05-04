
package net.blacktortoise.android.ai.view;

import java.util.List;

import net.blacktortoise.android.ai.R;
import net.blacktortoise.android.ai.model.TagItemModel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TagItemModelAdapter extends ArrayAdapter<TagItemModel> {
    private static class ViewHolder {
        ImageView thumbnailImage;

        TextView nameText;

        TextView labelText;
    }

    public TagItemModelAdapter(Context context, List<TagItemModel> objects) {
        super(context, R.layout.item_tag_item_model, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag_item_model,
                    null);
            vh = new ViewHolder();
            vh.thumbnailImage = (ImageView)convertView.findViewById(R.id.thumbnailImage);
            vh.nameText = (TextView)convertView.findViewById(R.id.nameText);
            vh.labelText = (TextView)convertView.findViewById(R.id.labelText);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }
        TagItemModel model = getItem(position);
        vh.thumbnailImage.setImageBitmap(model.getThumbnail());
        vh.nameText.setText(model.getName());
        vh.labelText.setText(model.getLabel());

        return convertView;
    }

}
