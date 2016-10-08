package ua.in.badparking.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.badoualy.stepperindicator.StepperIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.badparking.BuildConfig;
import ua.in.badparking.R;
import ua.in.badparking.events.ShowHeaderEvent;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.GeolocationService;
import ua.in.badparking.ui.fragments.BaseFragment;
import ua.in.badparking.ui.fragments.CaptureFragment;
import ua.in.badparking.ui.fragments.ClaimOverviewFragment;
import ua.in.badparking.ui.fragments.ClaimTypeFragment;
import ua.in.badparking.ui.fragments.LocationFragment;


public class MainActivity extends AppCompatActivity {

    public final static int PAGE_CAPTURE = 0;
    public final static int PAGE_CLAIM_TYPES = 1;
    public final static int PAGE_MAP = 2;
    public final static int PAGE_CLAIM_OVERVIEW = 3;

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = MainActivity.class.getName();

    @BindView(R.id.toolbar_top)
    protected Toolbar toolbarTop;

    @BindView(R.id.contentView)
    protected FrameLayout contentView;

    @BindView(R.id.stepper_indicator)
    protected StepperIndicator stepperIndicator;

    private Dialog senderProgressDialog;
    private int mPosition;
    private AlertDialog locationDialog;

//    private BroadcastReceiver connectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupToolbar();
        if (DEBUG) {
            printDevHashKey();
        }
        showPage(PAGE_CAPTURE);

//        connectionReceiver = new BroadcastReceiver() { TODO uncomment
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (isConnected(context) & isLocationEnabled()) {
//                    start();
//                }
//            }
//        };
//
//        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(connectionReceiver, intentFilter);

        GeolocationService.INST.start(getApplicationContext());
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
//        unregisterReceiver(connectionReceiver);
        GeolocationService.INST.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        // checking internet connection
        if (!isConnected(this) && !DEBUG) {
            buildConnectionDialog(this).show();
            return;
        } else {
            ClaimService.INST.updateTypes();
        }

        checkLocationServices();

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
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (locationDialog != null && locationDialog.isShowing()) {
            locationDialog.dismiss();
        }
        GeolocationService.INST.unsubscribeFromLocationUpdates();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
            builder.setPositiveButton(getResources().getString(R.string.open_location_settings),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            paramDialogInterface.dismiss();
                        }
                    });
            builder.setCancelable(false);
            locationDialog = builder.show();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setTitle("");
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mPosition--;
        if (mPosition >= 0) {
            stepperIndicator.onPageSelected(mPosition);
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
        int shift = event.isShow() ? 150 : -150;
//        toolbarTop.animate().yBy(shift).setDuration(500).start();
//        contentView.animate().yBy(shift).setDuration(500).start();
        toolbarTop.setVisibility(event.isShow() ? View.VISIBLE : View.GONE);
    }

    public void showPage(int position) {
        mPosition = position;
        BaseFragment fragment;
        if (position == 0) {
            fragment = CaptureFragment.newInstance();
        } else if (position == 1) {
            fragment = ClaimTypeFragment.newInstance();
        } else if (position == 2) {
            fragment = LocationFragment.newInstance();
        } else {
            fragment = ClaimOverviewFragment.newInstance();
        }
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.contentView, fragment);
        if (position != 0) {
            transaction.addToBackStack("page_" + position);
        }
        transaction.commit();
        stepperIndicator.onPageSelected(position);
    }
}