package ua.in.badparking;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class SenderInfoDialog extends Dialog {
    public SenderInfoDialog(Context context) {
        super(context);
        _init();
    }

    private void _init() {

    }

    public SenderInfoDialog(Context context, int theme) {
        super(context, theme);
    }

    protected SenderInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
