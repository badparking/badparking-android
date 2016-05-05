package ua.in.badparking.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ua.in.badparking.BuildConfig;
import ua.in.badparking.R;
import ua.in.badparking.model.ReportController;
import ua.in.badparking.model.Sender;
import ua.in.badparking.model.UserManager;
import ua.in.badparking.ui.dialogs.IntroDialog;
import ua.in.badparking.ui.dialogs.ReportTypeDialog;
import ua.in.badparking.ui.fragments.ReportFragment;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Dialog senderProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        showReportFragment();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (UserManager.INST.getUserToken() == null) {
            showIntro();
        }
    }


    private void showReportFragment() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (actionBar != null) {
            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(mSectionsPagerAdapter.getPageTitle(i))
                                .setTabListener(this));
            }
        }
    }


    private void showIntro() {
        IntroDialog introDialog = new IntroDialog(this, android.R.style.Theme_Black_NoTitleBar, new IntroDialog.ActionListener() {
            @Override
            public void onAction() {
                if (!isOnline()) {
                    Toast.makeText(MainActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = Uri.parse("https://dl.dropboxusercontent.com/u/46259342/error.html");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        });
        introDialog.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_your_data) {
//            Dialog senderInfoDialog = new SenderInfoDialog(this);
//            senderInfoDialog.show();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    public void onSendClicked() {
//        if (!isOnline()) {
//            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
//            return;
//        }

        ReportTypeDialog reportTypeDialog = new ReportTypeDialog(this, android.R.style.Theme_Black_NoTitleBar, new ReportTypeDialog.ReportTypeChosenListener() {
            @Override
            public void onReportChosen(int typeId) {
                ReportController.INST.getReport().setCaseTypeId(String.valueOf(typeId));
                onReportTypeChosen();
            }
        });
        reportTypeDialog.show();

    }

    private void onReportTypeChosen() {
        showSenderDialogWithMessage();
        Sender.INST.send(new Sender.SendCallback() {
            @Override
            public void onCallback(final int code, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(code, message);
                    }
                });
            }
        });
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
            case Sender.CODE_UPLOADING_PHOTO: // uploading photo
                sendingMessageView.setText(message);
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
                        onSendClicked();
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
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ReportFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }


}
