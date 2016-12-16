package ua.in.badparking.ui.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.events.ImageUploadedEvent;
import ua.in.badparking.events.TokenRefreshFailedEvent;
import ua.in.badparking.events.TokenRefreshedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.model.MediaFile;
import ua.in.badparking.model.User;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.UserService;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;
import ua.in.badparking.utils.Constants;

/**
 * Created by Dima Kovalenko on 7/3/16.
 */
public class ClaimOverviewFragment extends BaseFragment {

    @BindView(R.id.recyclerView)
    protected RecyclerView recyclerView;

    @BindView(R.id.send_button)
    protected Button mSendButton;

    @BindView(R.id.carPlateNumberEditText)
    protected EditText carPlateNumberEditText;

    @BindView(R.id.crimeTypesTextView)
    protected TextView crimeTypeTextView;

    @BindView(R.id.addressTextView)
    protected TextView addressTextView;

    @BindView(R.id.login_button)
    LoginButton loginButton;

    private Unbinder unbinder;

    private AlertDialog waitDialog;
    private AlertDialog readyDialog;

    private PhotoAdapter photoAdapter;
    private CallbackManager callbackManager;

    private Claim claim;

    public static BaseFragment newInstance() {
        return new ClaimOverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_overview, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        EventBus.getDefault().register(this);

        carPlateNumberEditText.setEnabled(true);
        carPlateNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = editable.toString().replaceAll(" ", "");
                if (!editable.toString().equals(result)) {
                    carPlateNumberEditText.setText(result);
                    carPlateNumberEditText.setSelection(result.length());
                }

                if(carPlateNumberEditText.getText().toString().length() >= Constants.MIN_CARPLATE_LENGTH){
                    mSendButton.setEnabled(true);
                    ClaimService.INST.getClaim().setLicensePlates(String.valueOf(carPlateNumberEditText.getText()));
                } else {
                    mSendButton.setEnabled(false);
                }
            }
        });

        carPlateNumberEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    if(carPlateNumberEditText.getText().toString().length() < Constants.MIN_CARPLATE_LENGTH){
                        carPlateNumberEditText.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

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
                if (UserService.INST.getUser() != null) {
                    UserService.INST.validateTokenAndSend(UserService.INST.getUser().getToken());
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        photoAdapter = new PhotoAdapter(getActivity(), true);

        recyclerView.setAdapter(photoAdapter);
        recyclerView.setHasFixedSize(true);

        callbackManager = CallbackManager.Factory.create();
        crimeTypeTextView.setText(getCrimeTypesNames(ClaimService.INST.getSelectedCrimeTypes()));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.GONE);
            }
        });

        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        mSendButton.setVisibility(View.VISIBLE);
                        UserService.INST.authorizeWithFacebook(loginResult.getAccessToken().getToken());
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
    public void onResume() {
        super.onResume();

        if (isLoggedIn()) {
            loginButton.setVisibility(View.GONE);
            mSendButton.setVisibility(View.VISIBLE);
        } else {
            loginButton.setVisibility(View.VISIBLE);
            mSendButton.setVisibility(View.GONE);
        }
        carPlateNumberEditText.setText(ClaimService.INST.getClaim().getLicensePlates());
        addressTextView.setText(ClaimService.INST.getOverviewAddress());

        claimStateLogging();
    }

    private void send() {
        mSendButton.setEnabled(false);
        if (!ClaimService.INST.getClaim().isComplete()) {
            // TODO show "not complete" message
            return;
        } else if (UserService.INST.getUser() == null) {
            loginButton.setVisibility(View.VISIBLE);
            mSendButton.setVisibility(View.GONE);
            return;
        }

        claim = ClaimService.INST.getClaim();
        User user = UserService.INST.getUser();
        if (!user.isComplete()) {
            showCompleteUserDataDialog();
        } else {
            showSendClaimDialog();
            ClaimService.INST.postMyClaims(claim);
        }
    }

    private void showCompleteUserDataDialog() {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText emailText = new EditText(getActivity());
        emailText.setHint("Email");
        emailText.setText(UserService.INST.getUser().getEmail());
        layout.addView(emailText);

        final EditText phoneText = new EditText(getActivity());
        phoneText.setHint("Phone Number");
        String phone = UserService.INST.getUser().getPhone();
        phoneText.setText(phone);
        layout.addView(phoneText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.claim_overview_complete_user_message));
        builder.setView(layout);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String phone = String.valueOf(phoneText.getText());
                String email = String.valueOf(emailText.getText());
                UserService.INST.putUserComplete(email, phone);
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
        builder.setMessage(getActivity().getString(R.string.claim_sending));
        builder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //TODO BREAK SEND
                mSendButton.setEnabled(true);
            }
        });
        waitDialog = builder.create();
        waitDialog.show();
    }

    @Subscribe
    public void onImagePosted(final ImageUploadedEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!event.getFilesUploaded()) {
            builder.setMessage(getActivity().getString(R.string.error_uploading_image));
        } else {
            builder.setMessage(getActivity().getString(R.string.thanks));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode==KeyEvent.KEYCODE_BACK && event.isCanceled()) {
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });

            builder.setOnDismissListener(new DialogInterface.OnDismissListener(){

                @Override
                public void onDismiss(DialogInterface dialog) {
                    restartFlow();
                }
            });

        }
        readyDialog = builder.create();
        if(waitDialog.isShowing()){
            waitDialog.dismiss();
        }
        readyDialog.show();
    }

    private void restartFlow() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish(); // call this to finish the current activity
    }

    @Subscribe
    public void onClaimPosted(final ClaimPostedEvent event) {
        ClaimService.INST.setPk(event.getPk());

        if (event.getPosted()) {
            List<MediaFile> files = ClaimService.INST.getPictures();
            for (int i = 0; i < files.size(); i++) {
                MediaFile file = files.get(i);
                ClaimService.INST.postImage(event.getPk(), file);
            }
        }
    }

    @Subscribe
    public void onTokenRefreshFailed(final TokenRefreshFailedEvent event) {
        //loginButton.setVisibility(View.VISIBLE);
        //mSendButton.setVisibility(View.GONE);

        Toast.makeText(getContext(), "Cecciя закiнчилась.\nОновлення сессії...", Toast.LENGTH_SHORT).show();
        //Toast.makeText(getContext(), "Залогiнтесь ще раз, будь-ласка", Toast.LENGTH_LONG).show();

        //logout
        LoginManager.getInstance().logOut();

        //refreshed
        send();
    }

    @Subscribe
    public void onTokenRefreshed(final TokenRefreshedEvent event) {
        send();
    }

    @Subscribe
    public void onUserUpdated(final UserUpdatedEvent event) {
        UserService.INST.saveUser(event.getUser());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public String getCrimeTypesNames(List<CrimeType> crimeTypeList) {
        StringBuilder sb = new StringBuilder();
        Iterator<CrimeType> crimeTypeIterator = crimeTypeList.iterator();

        while (crimeTypeIterator.hasNext()) {
            CrimeType ct = crimeTypeIterator.next();

            sb.append("- ").append(ct.getName());
            if (crimeTypeIterator.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}