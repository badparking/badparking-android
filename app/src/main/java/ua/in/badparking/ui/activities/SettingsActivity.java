package ua.in.badparking.ui.activities;

import android.os.Bundle;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.R;
import ua.in.badparking.services.api.TokenService;
import ua.in.badparking.services.api.UserService;

@ContentView(R.layout.activity_settings)
public class SettingsActivity extends RoboActivity {

    private static final String TAG = SettingsActivity.class.getName();

    @BindView(R.id.login_button)
    LoginButton loginButton;
    @Inject
    private TokenService tokenService;
    private CallbackManager callbackManager;

    public static SettingsActivity newInstance() {
        return new SettingsActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email, public_profile");
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        tokenService.authorizeWithFacebook(loginResult.getAccessToken().getToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }
                });
    }
}