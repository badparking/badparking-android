package ua.in.badparking.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

import ua.in.badparking.R;

public class Alerts {
    private Activity activity;

    public Alerts(Activity activity) {
        this.activity = activity;
    }

    public void showWifiAlert(){
        AlertUserDialog dialog = new AlertUserDialog();
        dialog.setDisplayMessage(activity.getResources().getString(R.string.network_not_enabled));
        dialog.setSettingsActivityAction(Settings.ACTION_SETTINGS);
        dialog.setActivity(activity);
        dialog.show(activity.getFragmentManager(), null);
    }

    public void showAirModeAlert(){
        AlertUserDialog dialog = new AlertUserDialog();
        dialog.setDisplayMessage(activity.getResources().getString(R.string.air_mode_alert));
        dialog.setSettingsActivityAction(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        dialog.setActivity(activity);
        dialog.show(activity.getFragmentManager(), null);
    }

    public void showGpsAlert(){
        AlertUserDialog dialog = new AlertUserDialog();
        dialog.setDisplayMessage(activity.getResources().getString(R.string.gps_network_not_enabled));
        dialog.setSettingsActivityAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        dialog.setActivity(activity);
        dialog.show(activity.getFragmentManager(), null);
    }
}
