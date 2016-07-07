package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ua.in.badparking.R;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.ui.MainActivity;
import ua.in.badparking.ui.adapters.CrimeTypeAdapter;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko and Volodymyr Dranyk on 7/3/16.
 */
public class ClaimTypeFragment extends BaseFragment{
    CrimeTypeAdapter crimeTypeAdapter;
    ListView listView;
    EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_claim_type, container, false);
        listView = (ListView)rootView.findViewById(R.id.reportTypeList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        crimeTypeAdapter = new CrimeTypeAdapter(getActivity(), getModel());
        listView.setAdapter(crimeTypeAdapter);

        editText = (EditText)  rootView.findViewById(R.id.additionalInfoEditText);
        Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO send all info
                ((MainActivity)getActivity()).moveToNext();
            }
        });

        return rootView;
    }

    public static Fragment newInstance() {
        return new ClaimTypeFragment();
    }

    //temp data
    private List<CrimeType> getModel(){
        String[] reportTypes = getResources().getStringArray(R.array.report_types);
        List<CrimeType> ctList = new ArrayList<>();
        for(int i= 0; i < reportTypes.length; i++){
            CrimeType ct = new CrimeType();
            ct.setId(i);
            ct.setName(reportTypes[i]);
            ctList.add(ct);
        }
        return ctList;
    }

    public List<CrimeType> getSelectedCrimeTypes() {
        List<CrimeType> selectedCrimeTypeList = new ArrayList<>();

        for (CrimeType ct : crimeTypeAdapter.getCrimeTypeList()) {
            if (ct.isSelected()){
                selectedCrimeTypeList.add(ct);
            }
        }

        return selectedCrimeTypeList;
    }
}