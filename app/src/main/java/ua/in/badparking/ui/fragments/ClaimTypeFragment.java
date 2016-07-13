package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.CrimeTypeAdapter;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko and Volodymyr Dranyk on 7/3/16.
 */
public class ClaimTypeFragment extends BaseFragment {

    private CrimeTypeAdapter crimeTypeAdapter;
    @InjectView(R.id.reportTypeList)
    private ListView listView;
    @InjectView(R.id.next_button)
    private Button nextButton;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_claim_type, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        crimeTypeAdapter = new CrimeTypeAdapter(getActivity(), ClaimState.INST.getCrimeTypes(), nextButton);
        listView.setAdapter(crimeTypeAdapter);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClaimState.INST.getClaim().setCrimetypes(ClaimState.INST.getSelectedCrimeTypeIds());
                ((MainActivity) getActivity()).moveToNext();
            }
        });
        nextButton.setVisibility(View.GONE);
    }

    public static Fragment newInstance() {
        return new ClaimTypeFragment();
    }
}