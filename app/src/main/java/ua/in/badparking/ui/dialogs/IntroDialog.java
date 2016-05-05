package ua.in.badparking.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import ua.in.badparking.R;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class IntroDialog extends Dialog {

    private ActionListener actionListener;

    public IntroDialog(Context context, ActionListener actionListener) {
        super(context);
        this.actionListener = actionListener;
        _init(context);
    }

    public IntroDialog(Context context, int theme, ActionListener actionListener) {
        super(context, theme);
        this.actionListener = actionListener;
        _init(context);
    }

    protected IntroDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        _init(context);
    }

    private void _init(final Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_intro);


        Button bankIdButton = (Button)findViewById(R.id.bankIdButton);
        bankIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionListener.onAction();
            }
        });

    }

    public interface ActionListener {
        public void onAction();
    }
}
