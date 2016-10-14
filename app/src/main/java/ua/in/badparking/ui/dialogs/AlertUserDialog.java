package ua.in.badparking.ui.dialogs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

public class AlertUserDialog extends DialogFragment implements DialogInterface.OnClickListener{
    private String displayMessage;
    private String settingsActivityAction;
    private Activity activity;

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getSettingsActivityAction() {
        return settingsActivityAction;
    }

    public void setSettingsActivityAction(String settingsActivityAction) {
        this.settingsActivityAction = settingsActivityAction;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(activity);
        }

        builder.setMessage(displayMessage);
        builder.setPositiveButton("Налаштування", this);

        final Dialog theDialog = builder.create();
        theDialog.setCanceledOnTouchOutside(false);
        theDialog.setCancelable(true);

        return theDialog;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch(i) {
            case Dialog.BUTTON_POSITIVE:
                // Perform desired action response to user clicking "OK"
                if(settingsActivityAction != null) {
                    startActivity(new Intent(settingsActivityAction));
                }

                dialogInterface.dismiss();
                break;
        }
    }
}
