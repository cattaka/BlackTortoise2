
package net.blacktortoise.android.ai.dialog;

import net.blacktortoise.android.ai.R;
import net.blacktortoise.android.ai.dialog.IDialog.IDialogListener;
import net.blacktortoise.android.ai.model.TagItemModel;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogUtil {

    private static abstract class InnerDialog<T> implements DialogInterface.OnClickListener,
            IDialog<T> {
        protected T model;

        protected Dialog dialog;

        protected IDialogListener<TagItemModel> listener;

        public InnerDialog(View view, IDialogListener<TagItemModel> listener) {
            super();
            this.listener = listener;
        }

        @Override
        public abstract void onClick(DialogInterface dialog, int which);

        @Override
        public abstract void show(T data);
    }

    public static IDialog<TagItemModel> createEditTagItemModel(Context context,
            IDialogListener<TagItemModel> listener) {
        class InnerDialogImpl extends InnerDialog<TagItemModel> {

            private EditText nameEdit;

            private EditText labelEdit;

            public InnerDialogImpl(View view, IDialogListener<TagItemModel> listener) {
                super(view, listener);
                this.nameEdit = (EditText)view.findViewById(R.id.nameEdit);
                this.labelEdit = (EditText)view.findViewById(R.id.labelEdit);
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    model.setName(String.valueOf(nameEdit.getText()));
                    model.setLabel(String.valueOf(labelEdit.getText()));
                    listener.onPositive(model);
                } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                    listener.onNeutral(model);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    listener.onNegative(model);
                }
            }

            @Override
            public void show(TagItemModel data) {
                this.model = data;
                nameEdit.setText(data.getName());
                labelEdit.setText(data.getLabel());
                dialog.show();
            }
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_tag_item_model, null);
        InnerDialogImpl myDialog = new InnerDialogImpl(view, listener);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, myDialog);
        builder.setNeutralButton("Delete", myDialog);
        builder.setNegativeButton(android.R.string.cancel, myDialog);
        myDialog.dialog = builder.create();

        return myDialog;
    }
}
