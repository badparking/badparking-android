package ua.in.badparking.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

//TODO singleton
public class Alerts {
    private Context context;

    public Alerts(Context context) {
        this.context = context;
    }

    public void showWifiAlert(){
        AlertUserDialog dialog = new AlertUserDialog();
        dialog.setDisplayMessage("Please Turn On Wi-Fi");
        dialog.setSettingsActivityAction(Settings.ACTION_WIFI_SETTINGS);
        dialog.show(((Activity)context).getFragmentManager(), null);
    }

    public void showAirModeAlert(){
        AlertUserDialog dialog = new AlertUserDialog();
        dialog.setDisplayMessage("Please Disable Airplane Mode");
        dialog.setSettingsActivityAction(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        dialog.show(((Activity)context).getFragmentManager(), null);
    }

    public void showGpsAlert(){
        AlertUserDialog dialog = new AlertUserDialog();
        dialog.setDisplayMessage("Please Enable Location Services");
        dialog.setSettingsActivityAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        dialog.show(((Activity)context).getFragmentManager(), null);
    }
}
