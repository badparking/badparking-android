package ua.in.badparking.ui.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import ua.in.badparking.BuildConfig;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.utils.LogHelper;

import static ua.in.badparking.utils.LogHelper.CLAIM_STATE_TAG;

/**
 * Created by Dima Kovalenko on 7/4/16.
 */
public class BaseFragment extends Fragment {

    private InputMethodManager inputManager;
    protected boolean isTablet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputManager = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean large = ((getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        boolean xlarge = ((getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        isTablet = large || xlarge;
    }

    public void removePhoneKeypad() {
        IBinder binder = getView().getWindowToken();
        inputManager.hideSoftInputFromWindow(binder,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected void claimStateLogging(){
        String log = LogHelper.getClaimStateLog(this.getClass(), ClaimService.INST.getClaim());
        Log.d(CLAIM_STATE_TAG, log);

        if (!BuildConfig.DEBUG) {
            Toast toast = Toast.makeText(getContext(), log, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
