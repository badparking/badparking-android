package ua.in.badparking.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Dima Kovalenko on 7/4/16.
 */
public class BaseFragment extends Fragment {

    InputMethodManager inputManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputManager = (InputMethodManager)getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void removePhoneKeypad() {
        IBinder binder = getView().getWindowToken();
        inputManager.hideSoftInputFromWindow(binder,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
