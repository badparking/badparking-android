package ua.in.badparking.ui.dialogs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class AlertUserDialog extends DialogFragment implements DialogInterface.OnClickListener{
    private String displayMessage;
    private String settingsActivityAction;

    public AlertUserDialog() {
    }

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


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(getActivity());
        }

        builder.setMessage(displayMessage);
        builder.setPositiveButton("Налаштування", this);

        Dialog theDialog = builder.create();
        theDialog.setCanceledOnTouchOutside(false);
        theDialog.setCancelable(false);

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
        dialogInterface.dismiss();
    }


}
