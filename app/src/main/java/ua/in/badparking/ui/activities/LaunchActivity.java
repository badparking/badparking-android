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
import com.squareup.okhttp.OkHttpClient;

import org.greenrobot.eventbus.EventBus;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.R;
import ua.in.badparking.services.api.ClaimsService;

@ContentView(R.layout.activity_launch)
public class LaunchActivity extends RoboActivity {


}
