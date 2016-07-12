package ua.in.badparking.ui.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import roboguice.inject.InjectView;
import ua.in.badparking.Constants;
import ua.in.badparking.R;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.User;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.UserState;
import ua.in.badparking.services.api.ClaimsService;
import ua.in.badparking.services.api.TokenService;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.PhotoAdapter;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko on 7/3/16.
 */
public class ClaimOverviewFragment extends BaseFragment {


    @InjectView(R.id.recyclerView)
    protected RecyclerView recyclerView;

    @Inject
    private ClaimsService mClaimService;
    @Inject
    private TokenService mTokenService;
    private final OkHttpClient client = new OkHttpClient();
    private AlertDialog waitDialog;
    private AlertDialog readyDialog;

    private PhotoAdapter photoAdapter;

    public static Fragment newInstance() {
        return new ClaimOverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_overview, container, false);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mTokenService.verifyToken(ClaimState.INST.getToken());
                String url = Constants.BASE_URL + "/profiles/login/dummy";
                get(url, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
//                    EventBus.getDefault().post(new ClaimPostedEvent(e.getMessage()));
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        ClaimState.INST.setToken(response.headers().get("X-JWT"));
                    }
                });
//        }
                final Claim claim = ClaimState.INST.getClaim();
                final User user = UserState.INST.getUser();
                //TODO: 1. Add user data to request. 2. TBD - upload image
                showSendClaimDialog();
                mClaimService.postMyClaims(claim);
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        photoAdapter = new PhotoAdapter(getActivity());
        recyclerView.setAdapter(photoAdapter);
        recyclerView.setHasFixedSize(true);
    }

    Call get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

//TODO: Verify token
//    @Subscribe
//    public void onTokenVerified(TokenVerifiedEvent event) {
////        if(event.getVerificationResult().equals(false)) {
//            String url = Constants.BASE_URL + "/profiles/login/dummy";
//            get(url, new Callback() {
//                @Override
//                public void onFailure(Request request, IOException e) {
////                    EventBus.getDefault().post(new ClaimPostedEvent(e.getMessage()));
//                }
//
//                @Override
//                public void onResponse(Response response) throws IOException {
//                    ClaimState.INST.setToken(response.headers().get("X-JWT"));
//                }
//            });
////        }
//        final Claim claim = ClaimState.INST.getClaim();
//        final User user = UserState.INST.getUser();
//        //TODO: 1. Add user data to request. 2. TBD - upload image
//        showSendClaimDialog();
//        mClaimService.postMyClaims(claim);
//    }

    private void showSendClaimDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Відправлення...");
        waitDialog = builder.create();
        waitDialog.show();
    }

    @Subscribe
    public void onShowResult(final ClaimPostedEvent event) {
        waitDialog.hide();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(event.getMessage());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(event.getPosted()) {
                    ((MainActivity) getActivity()).moveToFirst();
                }
            }
        });
        readyDialog = builder.create();
        readyDialog.show();
    }
}
