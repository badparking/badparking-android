package ua.in.badparking.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.util.regex.Pattern;

import ua.in.badparking.R;
import ua.in.badparking.data.Trespass;
import ua.in.badparking.data.TrespassController;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class SenderInfoDialog extends Dialog {

    private EditText emailView;
    private EditText nameView;
    private EditText phoneView;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_sender_info);
        emailView = (EditText)findViewById(R.id.email);
        nameView = (EditText)findViewById(R.id.firstName);
        phoneView = (EditText)findViewById(R.id.phone);
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _saveData();
                dismiss();
            }
        });

        extractPossibleInfo();

        fillFromModel();
    }

    private void fillFromModel() {
        final Trespass trespass = TrespassController.INST.getTrespass();

        if (trespass.getName() != null) {
            nameView.setText(trespass.getName());
        }
        if (trespass.getPhone() != null) {
            phoneView.setText(trespass.getPhone());
        }
        if (trespass.getEmail() != null) {
            emailView.setText(trespass.getEmail());
        }
    }

    private void _saveData() {
        String name = nameView.getText().toString();
        String phone = phoneView.getText().toString();
        String email = emailView.getText().toString();

        TrespassController.INST.getTrespass().setName(name);
        TrespassController.INST.getTrespass().setPhone(phone);
        TrespassController.INST.getTrespass().setEmail(email);
        TrespassController.INST.saveToPrefs();
    }

    private void extractPossibleInfo() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                emailView.setText(possibleEmail);
            }
        }
    }
}
