package ua.in.badparking.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
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
import ua.in.badparking.receivers.GpsStatusReceiver;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.GeolocationService;
import ua.in.badparking.services.UserLocationListener;
import ua.in.badparking.ui.dialogs.Alerts;
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

    private static boolean firstChecked = false;

    @BindView(R.id.toolbar_top)
    protected Toolbar toolbarTop;

    @BindView(R.id.contentView)
    protected FrameLayout contentView;

    @BindView(R.id.stepper_indicator)
    protected StepperIndicator stepperIndicator;

    private LocationListener _gpsLocationListener;
    private GpsStatusReceiver _gpsStatusReceiver;

    private Dialog senderProgressDialog;
    private int mPosition;
    private Alerts alerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        alerts = new Alerts(this);

        setupToolbar();
        if (DEBUG) {
            printDevHashKey();
        }
        showPage(PAGE_CAPTURE);

        GeolocationService.INST.start(getApplicationContext());


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(connectionReceiver);
        stopLocationListener();
        GeolocationService.INST.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!firstChecked && confirmNetworkProviderAvailable(lm) ) {
            _gpsStatusReceiver = new GpsStatusReceiver();
            _gpsStatusReceiver.setActivity(this);
            _gpsStatusReceiver.start(this);

            _gpsLocationListener = new UserLocationListener();


            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, _gpsLocationListener);
            } catch (SecurityException se) {

            }

            ClaimService.INST.updateTypes();

            firstChecked = true;
        }
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
        GeolocationService.INST.unsubscribeFromLocationUpdates();
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

    private void stopLocationListener() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (_gpsLocationListener != null) {
            try{
                lm.removeUpdates(_gpsLocationListener);
            }catch (SecurityException se){

            }
            _gpsLocationListener = null;
        }

        if (_gpsStatusReceiver != null) {
            _gpsStatusReceiver.stop(this);
            _gpsStatusReceiver = null;
        }
    }

    boolean confirmNetworkProviderAvailable(LocationManager lm) {
        return confirmAirplaneModeOff() && confirmWiFiAvailable() && confirmNetworkProviderEnabled(lm);
    }

    boolean confirmNetworkProviderEnabled(LocationManager lm) {

        boolean isAvailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!isAvailable) {
            alerts.showGpsAlert();
        }

        return isAvailable;
    }

    boolean confirmAirplaneModeOff() {
        boolean isOff = Settings.System.getInt(getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 0;

        if(!isOff) {
            alerts.showAirModeAlert();
        }

        return isOff;
    }

    boolean confirmWiFiAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = (wifi != null && wifi.isConnectedOrConnecting()) ||
                (mobile != null && mobile.isConnectedOrConnecting());

        if (!isConnected) {
            alerts.showWifiAlert();
        }

        return isConnected;
    }
}