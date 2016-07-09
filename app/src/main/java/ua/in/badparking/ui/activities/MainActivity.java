package ua.in.badparking.ui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.badoualy.stepperindicator.StepperIndicator;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.BuildConfig;
import ua.in.badparking.R;
import ua.in.badparking.ui.dialogs.EnableGPSDialog;
import ua.in.badparking.ui.fragments.ResultFragment;
import ua.in.badparking.ui.fragments.CaptureFragment;
import ua.in.badparking.ui.fragments.ClaimOverviewFragment;
import ua.in.badparking.ui.fragments.ClaimTypeFragment;
import ua.in.badparking.ui.fragments.LocationFragment;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActionBarActivity {

    private SectionsPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private Dialog senderProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupToolbar();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        StepperIndicator indicator = (StepperIndicator)findViewById(R.id.stepper_indicator);
        assert indicator != null;
        indicator.setViewPager(viewPager, true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        checkLocationServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationServices();
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
                            //get gps
                        }
                    });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    showEnableGpsDialogIfNeeded();
                }
            });
            dialog.show();
        }
    }

    private void setupToolbar() {
        Toolbar toolbarTop = (Toolbar)findViewById(R.id.toolbar_top);
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

    public void moveToNext() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return CaptureFragment.newInstance();
            } else if (position == 1) {
                return LocationFragment.newInstance();
            } else if (position == 2) {
                return ClaimTypeFragment.newInstance();
            } else if (position == 3){
                return ClaimOverviewFragment.newInstance();
            } else {
                return ResultFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 5;
        }


    }

}
