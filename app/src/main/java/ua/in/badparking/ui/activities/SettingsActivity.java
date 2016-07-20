package ua.in.badparking.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.inject.Inject;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.services.api.UserService;
import ua.in.badparking.ui.fragments.BaseFragment;

public class SettingsActivity extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getName();

    @InjectView(R.id.login_button)
    LoginButton loginButton;
    @Inject
    private UserService userService;
    private CallbackManager callbackManager;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        loginButton.setReadPermissions("email, public_profile");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        loginButton.setVisibility(View.GONE);
                        userService.authorizeWithFacebook(loginResult.getAccessToken().getToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }
                });

        return rootView;
    }


}