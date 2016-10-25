package ua.in.badparking.ui.activities;

import android.Manifest;
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
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.badparking.BuildConfig;
import ua.in.badparking.R;
import ua.in.badparking.events.ShowHeaderEvent;
import ua.in.badparking.receivers.GpsStatusReceiver;
import ua.in.badparking.receivers.UserLocationListener;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.TrackingService;
import ua.in.badparking.ui.dialogs.Alerts;
import ua.in.badparking.ui.fragments.BaseFragment;
import ua.in.badparking.ui.fragments.CaptureFragment;
import ua.in.badparking.ui.fragments.ClaimOverviewFragment;
import ua.in.badparking.ui.fragments.ClaimTypeFragment;
import ua.in.badparking.ui.fragments.LocationFragment;
import ua.in.badparking.utils.LogHelper;


public class MainActivity extends AppCompatActivity {

    public final static int PAGE_CAPTURE = 0;
    public final static int PAGE_CLAIM_TYPES = 1;
    public final static int PAGE_MAP = 2;
    public final static int PAGE_CLAIM_OVERVIEW = 3;

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 123;
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = MainActivity.class.getName();
    private boolean firstChecked = false;

    @BindView(R.id.toolbar_top)
    protected Toolbar toolbarTop;

    @BindView(R.id.contentView)
    protected FrameLayout contentView;

    @BindView(R.id.stepper_indicator)
    protected StepperIndicator stepperIndicator;

    private LocationListener gpsLocationListener;
    private GpsStatusReceiver gpsStatusReceiver;

    private int mPosition;
    private Alerts alerts;

    private String[] permissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION};

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        ClaimService.INST.updateTypes();

        if (checkPermissions()) {
            LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);

            if (!firstChecked && confirmNetworkProviderAvailable(lm)) {
                gpsStatusReceiver = new GpsStatusReceiver();
                gpsStatusReceiver.setActivity(this);
                gpsStatusReceiver.start(this);

                gpsLocationListener = new UserLocationListener(this);

                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //permission logic
                }

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);

                firstChecked = true;
            }

            controlTrackingService(TrackingService.ACTION_START_MONITORING);
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
        stopLocationListener();
        controlTrackingService(TrackingService.ACTION_STOP_MONITORING);
        EventBus.getDefault().unregister(this);
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
        Dialog senderProgressDialog = new Dialog(this);
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
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);

        if (gpsLocationListener != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //permission logic
            }

            lm.removeUpdates(gpsLocationListener);
            gpsLocationListener = null;
        }

        if (gpsStatusReceiver != null) {
            gpsStatusReceiver.stop(this);
            gpsStatusReceiver = null;
        }
    }

    boolean confirmNetworkProviderAvailable(LocationManager lm) {
        return confirmAirplaneModeOff() && confirmWiFiAvailable() && confirmNetworkProviderEnabled(lm);
    }

    boolean confirmNetworkProviderEnabled(LocationManager lm) {

        boolean isAvailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isAvailable) {
            alerts.showGpsAlert();
        }

        return isAvailable;
    }

    boolean confirmAirplaneModeOff() {
        boolean isOff = Settings.System.getInt(getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 0;

        if (!isOff) {
            alerts.showAirModeAlert();
        }

        return isOff;
    }

    boolean confirmWiFiAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = (wifi != null && wifi.isConnectedOrConnecting()) ||
                (mobile != null && mobile.isConnectedOrConnecting());

        if (!isConnected) {
            alerts.showWifiAlert();
        }

        return isConnected;
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }

        return true;
    }

    private void controlTrackingService(String command) {
        Intent onTrackingServiceIntent = new Intent(this, TrackingService.class);
        onTrackingServiceIntent.setAction(command);
        startService(onTrackingServiceIntent);
    }

    @Override
    protected void onDestroy() {
        turnGPSOff();
        super.onDestroy();
    }

    private void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
            Log.d(LogHelper.LOCATION_MONITORING_TAG, "GPS TURN OFF!");
        }
    }
}