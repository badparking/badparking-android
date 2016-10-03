package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.services.ClaimService;
import ua.in.badparking.ui.activities.MainActivity;
import ua.in.badparking.ui.adapters.CrimeTypeAdapter;

/**
 * Design https://www.dropbox.com/sh/vbffs09uqzaj2mt/AAABkTvQbP7q10o5YP83Mzdia?dl=0
 * Created by Dima Kovalenko and Volodymyr Dranyk on 7/3/16.
 */
public class ClaimTypeFragment extends BaseFragment {

    @BindView(R.id.reportTypeList)
    ListView listView;

    @BindView(R.id.next_button)
    Button nextButton;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_claim_type, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        if (ClaimService.INST.getCrimeTypes() != null) {
            CrimeTypeAdapter crimeTypeAdapter = new CrimeTypeAdapter(getActivity(), ClaimService.INST.getCrimeTypes(), nextButton);
            listView.setAdapter(crimeTypeAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    if (checkedItems != null) {
                        Set<Integer> checkedCrimeTypesId = new HashSet<>();
                        for (int i=0; i<checkedItems.size(); i++) {
                            CrimeType ct = ClaimService.INST.getCrimeTypes().get(checkedItems.keyAt(i));
                            checkedCrimeTypesId.add(ct.getId());
                        }

                        ClaimService.INST.getClaim().getCrimetypes().clear();
                        ClaimService.INST.getClaim().getCrimetypes().addAll(checkedCrimeTypesId);
                    }
                }
            });
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).showPage(MainActivity.PAGE_MAP);
            }
        });
//        nextButton.setVisibility(View.GONE);
    }

    public static BaseFragment newInstance() {
        return new ClaimTypeFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}