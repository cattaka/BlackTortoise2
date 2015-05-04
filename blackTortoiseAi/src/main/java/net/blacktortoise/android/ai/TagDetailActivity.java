
package net.blacktortoise.android.ai;

import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.view.ImageAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class TagDetailActivity extends Activity {
    public static final String EXTRA_TAG_ITEM_MODEL_ID = "tagItemModelId";

    private ListView mImageList;

    private DbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_detail);

        mImageList = (ListView)findViewById(R.id.imageList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        {
            if (mDbHelper != null) {
                mDbHelper.close();
            }
            mDbHelper = new DbHelper(this);
        }
        refleshList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    private void refleshList() {
        int id = getIntent().getIntExtra(EXTRA_TAG_ITEM_MODEL_ID, 0);
        TagItemModel model = null;
        if (id >= 0) {
            model = mDbHelper.findTagItemModelById(id);
        }
        if (model != null && model.getBitmaps() != null) {
            ImageAdapter adapter = new ImageAdapter(this, model.getBitmaps());
            mImageList.setAdapter(adapter);
        } else {
            mImageList.setAdapter(null);
        }

    }
}
