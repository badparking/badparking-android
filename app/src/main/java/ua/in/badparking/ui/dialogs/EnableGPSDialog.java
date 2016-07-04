package ua.in.badparking.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import ua.in.badparking.R;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class EnableGPSDialog extends Dialog {

    private ActionListener actionListener;

    public EnableGPSDialog(Context context, ActionListener actionListener) {
        super(context);
        this.actionListener = actionListener;
        _init(context);
    }

    public EnableGPSDialog(Context context, int theme, ActionListener actionListener) {
        super(context, theme);
        this.actionListener = actionListener;
        _init(context);
    }

    protected EnableGPSDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        _init(context);
    }

    private void _init(final Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_enable_gps);

    }

    public interface ActionListener {
        public void onAction();
    }
}
