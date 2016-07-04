package ua.in.badparking.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ua.in.badparking.R;

/**
 * Created by Dima Kovalenko on 7/3/16.
 */
public class ClaimOverviewFragment extends Fragment {

    public static Fragment newInstance() {
        return new ClaimOverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_overview, container, false);
        return rootView;
    }

}
