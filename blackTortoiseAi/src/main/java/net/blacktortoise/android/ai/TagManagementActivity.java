
package net.blacktortoise.android.ai;

import java.util.List;

import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.dialog.DialogUtil;
import net.blacktortoise.android.ai.dialog.IDialog;
import net.blacktortoise.android.ai.dialog.IDialog.IDialogListener;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.view.TagItemModelAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class TagManagementActivity extends Activity implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener {
    private ListView mTagItemModelList;

    private DbHelper mDbHelper;

    private IDialog<TagItemModel> mEditTagItemModelDialog;

    private IDialogListener<TagItemModel> mEditTagItemModelListner = new IDialogListener<TagItemModel>() {

        @Override
        public void onPositive(TagItemModel data) {
            data.updateThumbnail();
            mDbHelper.registerTagItemModel(data);
            refleshList();
        }

        @Override
        public void onNeutral(TagItemModel data) {
            if (data != null && data.getId() != null) {
                mDbHelper.deleteTagItemModel(data.getId());
            }
            refleshList();
        }

        @Override
        public void onNegative(TagItemModel data) {
            // none
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_management);

        mEditTagItemModelDialog = DialogUtil.createEditTagItemModel(this, mEditTagItemModelListner);

        findViewById(R.id.addNetTagButton).setOnClickListener(this);

        mTagItemModelList = (ListView)findViewById(R.id.tagItemModelList);
        mTagItemModelList.setOnItemClickListener(this);
        mTagItemModelList.setOnItemLongClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addNetTagButton) {
            Intent intent = new Intent(this, TakeTagActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.tagItemModelList) {
            TagItemModel srcModel = (TagItemModel)parent.getAdapter().getItem(position);
            Intent intent = new Intent(this, TagDetailActivity.class);
            intent.putExtra(TagDetailActivity.EXTRA_TAG_ITEM_MODEL_ID, srcModel.getId().intValue());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.tagItemModelList) {
            TagItemModel srcModel = (TagItemModel)parent.getAdapter().getItem(position);
            TagItemModel model = mDbHelper.findTagItemModelById(srcModel.getId());
            mEditTagItemModelDialog.show(model);
            return true;
        } else {
            return false;
        }
    }

    private void refleshList() {
        List<TagItemModel> models = mDbHelper.findTagItemModel(false);
        TagItemModelAdapter adapter = new TagItemModelAdapter(this, models);
        mTagItemModelList.setAdapter(adapter);
    }
}
