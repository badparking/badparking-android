package ua.in.badparking.ui.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.App;
import ua.in.badparking.R;
import ua.in.badparking.events.AuthorizedWithFacebookEvent;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.events.ImageUploadedEvent;
import ua.in.badparking.events.UserLoadedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.MediaFile;
import ua.in.badparking.model.User;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.UserState;
import ua.in.badparking.services.api.ClaimsService;
import ua.in.badparking.services.api.TokenService;
import ua.in.badparking.services.api.UserService;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko on 7/3/16.
 */
public class ClaimOverviewFragment extends BaseFragment {

    @BindView(R.id.recyclerView)
    protected RecyclerView recyclerView;
    @BindView(R.id.send_button)
    protected Button mSendButton;
    @BindView(R.id.carPlateNumberTextView)
    protected TextView carPlateNumberTextView;
    @BindView(R.id.crimeTypesTextView)
    protected TextView crimeTypeTextView;
    @BindView(R.id.addressTextView)
    protected TextView addressTextView;
    @BindView(R.id.login_button)
    LoginButton loginButton;
    private Unbinder unbinder;

    @Inject
    private ClaimsService mClaimService;

    @Inject
    private UserService mUserService;

    @Inject
    private TokenService mTokenService;

    private AlertDialog waitDialog;
    private AlertDialog readyDialog;

    private PhotoAdapter photoAdapter;
    private CallbackManager callbackManager;

    private User user;
    private Claim claim;

    public static Fragment newInstance() {
        return new ClaimOverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_overview, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSendButton.setEnabled(true);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        photoAdapter = new PhotoAdapter(getActivity());
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setHasFixedSize(true);

        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions("email, public_profile");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        loginButton.setVisibility(View.GONE);
                        mSendButton.setVisibility(View.VISIBLE);
                        mTokenService.authorizeWithFacebook(loginResult.getAccessToken().getToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }
                });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            carPlateNumberTextView.setText(ClaimState.INST.getClaim().getLicensePlates());
            crimeTypeTextView.setText(ClaimState.INST.getSelectedCrimeTypesNames());
            addressTextView.setText(ClaimState.INST.getFullAddress());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLoggedIn()) {
            loginButton.setVisibility(View.GONE);
            mSendButton.setVisibility(View.VISIBLE);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            mSendButton.setVisibility(View.GONE);
        }
    }

    private void send() {
        mUserService.getUser();
        if (!ClaimState.INST.getClaim().isComplete()) {
            // TODO show "not complete" message
            return;
        } else if (UserState.INST.getUser() == null) {
            loginButton.setVisibility(View.VISIBLE);
            mSendButton.setVisibility(View.GONE);
            return;
        }
        claim = ClaimState.INST.getClaim();
        user = UserState.INST.getUser();
        if (user.isComplete().equals("false")) {
            showCompleteUserDataDialog();
            return;
        } else {
            showSendClaimDialog();
            mClaimService.postMyClaims(claim);
        }
    }

    private void showCompleteUserDataDialog() {

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText emailText = new EditText(getActivity());
        emailText.setHint("Email");
        emailText.setText(UserState.INST.getUser().getEmail());
        layout.addView(emailText);

        final EditText phoneText = new EditText(getActivity());
        phoneText.setHint("Phone Number");
        String phone = UserState.INST.getUser().getPhone();
        if (TextUtils.isEmpty(phone)) {
            TelephonyManager tMgr = (TelephonyManager)getActivity().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            phone = tMgr.getLine1Number();
        }
        phoneText.setText(phone);
        layout.addView(phoneText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(App.getAppContext().getString(R.string.claim_overview_complete_user_message));
        builder.setView(layout);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String phone = String.valueOf(phoneText.getText());
                String email = String.valueOf(emailText.getText());
                mUserService.putUserComplete(email, phone);
            }
        });
        builder.show();
    }


    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void showSendClaimDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(App.getAppContext().getString(R.string.claim_sending));
        builder.setNegativeButton(App.getAppContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        waitDialog = builder.create();
        waitDialog.show();
    }

    @Subscribe
    public void onImagePosted(final ImageUploadedEvent event) {
        readyDialog.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (event.getImageCounter() != -1) {
            builder.setMessage(App.getAppContext().getString(R.string.photo_uploaded) + event.getImageCounter() + "/" + ClaimState.INST.getPictures().size());
        } else {
            builder.setMessage(App.getAppContext().getString(R.string.error_uploading_image));
        }

        if (ClaimState.INST.getPictures().size() == event.getImageCounter()) {
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((MainActivity)getActivity()).moveToFirst();
                }
            });
        } else {
            builder.setNegativeButton(App.getAppContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        readyDialog = builder.create();
        readyDialog.show();
    }

    @Subscribe
    public void onClaimPosted(final ClaimPostedEvent event) {
        ClaimState.INST.setPk(event.getPk());
        waitDialog.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(event.getMessage());
        readyDialog = builder.create();
        readyDialog.show();

        if (event.getPosted()) {
            List<MediaFile> files = ClaimState.INST.getPictures();
            for (int i = 0; i < files.size(); i++) {
                MediaFile file = files.get(i);
                mClaimService.postImage(event.getPk(), file, i + 1);
            }
        }

    }

    @Subscribe
    public void onUserInfoCompleted(final UserUpdatedEvent event) {
        user = event.getUser();
        UserState.INST.setUser(user);
        mClaimService.postMyClaims(claim);
    }

    @Subscribe
    public void onUserLoaded(final UserLoadedEvent event) {
        user = event.getUser();
        UserState.INST.setUser(user);
    }

    @Subscribe
    public void onAuthorizedWithFacebook(final AuthorizedWithFacebookEvent event) {
        user = event.getUser();
        UserState.INST.setUser(user);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}