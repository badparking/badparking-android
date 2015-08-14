package ua.in.badparking;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class SenderInfoDialog extends Dialog {

    public SenderInfoDialog(Context context) {
        super(context);
        _init();
    }

    public SenderInfoDialog(Context context, int theme) {
        super(context, theme);
        _init();
    }

    protected SenderInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        _init();
    }

    private void _init() {
        setTitle(getContext().getString(R.string.your_data));
        setContentView(R.layout.dialog_sender_info);
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
