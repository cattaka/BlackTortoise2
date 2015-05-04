
package net.blacktortoise.android.ai.dialog;

public interface IDialog<T> {
    public interface IDialogListener<T> {
        public void onPositive(T data);

        public void onNegative(T data);

        public void onNeutral(T data);
    }

    public void show(T data);

}
