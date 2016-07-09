package ua.in.badparking.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import ua.in.badparking.Constants;
import ua.in.badparking.R;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.User;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.UserState;
import ua.in.badparking.services.api.ClaimsService;
import ua.in.badparking.ui.activities.MainActivity;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko on 7/3/16.
 */
public class ClaimOverviewFragment extends BaseFragment {

    @Inject
    private ClaimsService mClaimService;
    private final OkHttpClient client = new OkHttpClient();

    public static Fragment newInstance() {
        return new ClaimOverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_overview, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Constants.BASE_URL + "/profiles/login/dummy";

                get(url, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        EventBus.getDefault().post(new ClaimPostedEvent(e.getMessage()));
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        ClaimState.INST.setToken(response.headers().get("X-JWT"));
                        final Claim claim = ClaimState.INST.getClaim();
                        final User user = UserState.INST.getUser();
                        //TODO: 1. Add user data to request. 2. TBD - upload image
                        mClaimService.postMyClaims(claim);
                    }
                });

                ((MainActivity)getActivity()).moveToNext();
            }
        });
    }

    Call get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
}
