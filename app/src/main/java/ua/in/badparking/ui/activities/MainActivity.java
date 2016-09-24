package ua.in.badparking.ui.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.badoualy.stepperindicator.StepperIndicator;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.inject.Inject;
import com.squareup.okhttp.OkHttpClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.BuildConfig;
import ua.in.badparking.CustomViewPager;
import ua.in.badparking.R;
import ua.in.badparking.events.ShowHeaderEvent;
import ua.in.badparking.services.api.ClaimsService;
import ua.in.badparking.ui.dialogs.EnableGPSDialog;
import ua.in.badparking.ui.fragments.CaptureFragment;
import ua.in.badparking.ui.fragments.ClaimOverviewFragment;
import ua.in.badparking.ui.fragments.ClaimTypeFragment;
import ua.in.badparking.ui.fragments.LocationFragment;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActionBarActivity {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    @BindView(R.id.pager)
    protected CustomViewPager viewPager;

    @BindView(R.id.toolbar_top)
    protected Toolbar toolbarTop;

    private SectionsPagerAdapter pagerAdapter;

    private Dialog senderProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setupToolbar();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        viewPager.setAdapter(pagerAdapter);
        //block swipe
        viewPager.setPagingEnabled(false);

        StepperIndicator indicator = (StepperIndicator)findViewById(R.id.stepper_indicator);
        assert indicator != null;
        indicator.setViewPager(viewPager, true);
        if (DEBUG) {
            printDevHashKey();
        }
    }

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
                    start();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnected(this)) {
            buildConnectionDialog(this).show();
        }
        start();
    }

    private void start() {
        mClaimsService.updateTypes();
    }

    public boolean isLocationEnabled() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void printDevHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        checkLocationServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void checkLocationServices() {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
            dialog.show();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setTitle("");
    }

    private void showEnableGpsDialogIfNeeded() {
        EnableGPSDialog introDialog = new EnableGPSDialog(this, android.R.style.Theme_Black_NoTitleBar,
                new EnableGPSDialog.ActionListener() {
                    @Override
                    public void onAction() {

                    }
                });
        introDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_your_data) {
//            Dialog senderInfoDialog = new SenderInfoDialog(this);
//            senderInfoDialog.show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    public void handleResult(int code, String message) {
        if (senderProgressDialog == null || !senderProgressDialog.isShowing()) {
            showSenderDialogWithMessage();
        }
        final TextView sendingMessageView = (TextView)senderProgressDialog.findViewById(R.id.sendingMessage);
        final Button sendingMessageButton = (Button)senderProgressDialog.findViewById(R.id.sendingButton);
        final View progressBar = senderProgressDialog.findViewById(R.id.progressBar);
        switch (code) {
            case 200: // OK
                sendingMessageButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                sendingMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        senderProgressDialog.dismiss();
                    }
                });
                sendingMessageView.setText("Вiдiслано, дякую!");
                break;
            case 8001: // uploading photo
                progressBar.setVisibility(View.GONE);
                sendingMessageView.setText(message);
                sendingMessageButton.setVisibility(View.GONE);
                break;
            default:
                sendingMessageButton.setVisibility(View.VISIBLE);
                sendingMessageButton.setText("Спробувати ще");
                progressBar.setVisibility(View.GONE);
                sendingMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        senderProgressDialog.dismiss();
                    }
                });
                String text = "Помилка " + code + ".\n Спробуйте пiзнiше.";
                if (BuildConfig.DEBUG) {
                    text += "\n" + message;
                }
                sendingMessageView.setText(text);

        }
    }

    private void showSenderDialogWithMessage() {
        senderProgressDialog = new Dialog(this);
        senderProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        senderProgressDialog.setContentView(R.layout.dialog_sending_progress);
        senderProgressDialog.show();
    }

    @Subscribe
    public void onShowHeaderEvent(final ShowHeaderEvent event) {
//        toolbarTop.animate().yBy(-150).setDuration(600).start();
//        viewPager.animate().yBy(-150).setDuration(600).start();
        toolbarTop.setVisibility(event.isShow() ? View.VISIBLE : View.GONE);
    }

    public void moveToNext() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        if (viewPager.getCurrentItem() == pagerAdapter.getCount() - 1) {
            viewPager.setPagingEnabled(true);
        }
    }

    public void moveToFirst() {
        viewPager.setCurrentItem(0);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return CaptureFragment.newInstance();
            } else if (position == 1) {
                return ClaimTypeFragment.newInstance();
            } else if (position == 2) {
                return LocationFragment.newInstance();
            } else return ClaimOverviewFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int currentItem = viewPager.getCurrentItem();
        if (keyCode == KeyEvent.KEYCODE_BACK && currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}