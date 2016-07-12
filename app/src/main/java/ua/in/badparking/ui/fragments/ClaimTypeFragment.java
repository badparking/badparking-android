package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;
import ua.in.badparking.R;
import ua.in.badparking.model.CrimeType;
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
        View rootView = inflater.inflate(R.layout.fragment_claim_type, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        List<CrimeType> crimeTypes = ClaimState.INST.getCrimeTypes();
        crimeTypeAdapter = new CrimeTypeAdapter(getActivity(), crimeTypes, nextButton);
        listView.setAdapter(crimeTypeAdapter);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> crimetypesIds = new ArrayList<>();
                for (CrimeType crimeType : getSelectedCrimeTypes()) {
                    crimetypesIds.add(crimeType.getId());
                }
                ClaimState.INST.getClaim().setCrimetypes(crimetypesIds);
                ((MainActivity) getActivity()).moveToNext();
            }
        });
        nextButton.setEnabled(false);
    }

    public static Fragment newInstance() {
        return new ClaimTypeFragment();
    }

    public List<CrimeType> getSelectedCrimeTypes() {
        List<CrimeType> selectedCrimeTypeList = new ArrayList<>();

        for (CrimeType ct : crimeTypeAdapter.getCrimeTypeList()) {
            if (ct.isSelected()) {
                selectedCrimeTypeList.add(ct);
            }
        }

        return selectedCrimeTypeList;
    }
}