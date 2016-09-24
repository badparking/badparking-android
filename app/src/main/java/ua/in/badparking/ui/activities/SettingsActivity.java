package ua.in.badparking.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.R;

@ContentView(R.layout.activity_settings)
public class SettingsActivity extends RoboActivity {

    private static final String TAG = SettingsActivity.class.getName();

    @BindView(R.id.login_button)
    LoginButton loginButton;

    @BindView(R.id.avatar)
    CircleImageView avatar;

    @BindView(R.id.name)
    TextView name;

    @Inject
    private TokenService tokenService;
    private CallbackManager callbackManager;
    private ProfileTracker mProfileTracker;

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

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                refreshUI();
            }
        };

        refreshUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfileTracker.stopTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected void refreshUI() {
        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile == null) {
            avatar.setVisibility(View.GONE);
            name.setText("");
        } else {
            avatar.setVisibility(View.VISIBLE);
            int avatarDiameter = getResources().getDimensionPixelSize(R.dimen.expert_avatar_circle_diameter);
            String avatarUrl = String.format("http://graph.facebook.com/%s/picture?width=%d&height=%d", currentProfile.getId(), avatarDiameter, avatarDiameter);
            Glide.with(this).load(avatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(avatar);
            name.setText(currentProfile.getFirstName() + " " + currentProfile.getLastName());

        }
    }
}