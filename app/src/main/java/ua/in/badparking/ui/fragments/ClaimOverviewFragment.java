package ua.in.badparking.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.api.requests.ClaimRequest;
import ua.in.badparking.model.Claim;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.services.Sender;
import ua.in.badparking.services.api.ClaimsService;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko on 7/3/16.
 */
public class ClaimOverviewFragment extends BaseFragment {

    @Inject
    private ClaimsService mClaimService;

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
                final Claim claim = ClaimService.INST.getClaim();
                List<Claim> claims = new ArrayList<>();
                claims.add(claim);
                ClaimRequest claimRequest = new ClaimRequest(claims);
                mClaimService.postMyClaims(claimRequest);
//                ClaimRequest claimRequest = mClaimService.getClaimRequest();
            }
        });
    }
}
