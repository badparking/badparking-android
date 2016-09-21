package ua.in.badparking.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.Constants;
import ua.in.badparking.R;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.api.ClaimsService;

@ContentView(R.layout.activity_launch)
public class LaunchActivity extends RoboActivity {

    @Inject
    private ClaimsService mClaimsService;
    private BroadcastReceiver connectionReceiver;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        connectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isConnected(context) & isLocationEnabled()) {
                    init();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
    }

    @Subscribe
    public void onTypesLoaded(TypesLoadedEvent event) {
        ClaimState.INST.setCrimeTypes(event.getCrimeTypes());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    Call get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;
    }

    public AlertDialog.Builder buildConnectionDialog(Context context) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(getResources().getString(R.string.network_not_enabled));

        dialog.setPositiveButton(getResources().getString(R.string.open_location_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(myIntent);
                        paramDialogInterface.dismiss();
                    }
                });
        dialog.setCancelable(false);
        return dialog;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnected(this)) buildConnectionDialog(this).show();
        if (!isLocationEnabled()) buildLocationDialog(this).show();
    }

    private void init() {
        mClaimsService.getTypes();
        String url = Constants.BASE_URL + "/profiles/login/dummy";
        get(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                    EventBus.getDefault().post(new ClaimPostedEvent(e.getMessage()));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                ClaimState.INST.setToken(response.headers().get("X-JWT"));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
    }

    public AlertDialog.Builder buildLocationDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
        dialog.setPositiveButton(getResources().getString(R.string.open_location_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        paramDialogInterface.dismiss();
                    }
                });
        dialog.setCancelable(false);
        return dialog;
    }

    public boolean isLocationEnabled() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
