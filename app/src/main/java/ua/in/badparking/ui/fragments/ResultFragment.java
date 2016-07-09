package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.services.api.ClaimsService;

public class ResultFragment extends BaseFragment {

    @InjectView(R.id.web_auth) private WebView mAuthWeb;
    @InjectView(R.id.text_auth_response) private TextView mAuthResponse;
    @Inject private ClaimsService mClaimService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_auth, container, false);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Subscribe
    public void onShowResult(ClaimPostedEvent event) {
        mAuthResponse.setText(event.getMessage());
    }


    public static Fragment newInstance() {
        return new ResultFragment();
    }
}
