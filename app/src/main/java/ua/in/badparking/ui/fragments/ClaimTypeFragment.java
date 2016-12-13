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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private static final String TAG = ClaimTypeFragment.class.getName();

    @BindView(R.id.reportTypeList)
    ListView listView;

    @BindView(R.id.emptyTypeList)
    TextView emptyListView;

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

        if (ClaimService.INST.getAvailableCrimeTypes() != null) {

            CrimeTypeAdapter crimeTypeAdapter = new CrimeTypeAdapter(getActivity(),
                    ClaimService.INST.getAvailableCrimeTypes());

            if(crimeTypeAdapter.isEmpty()){
                listView.setVisibility(View.GONE);
                emptyListView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                emptyListView.setVisibility(View.GONE);
            }

            listView.setAdapter(crimeTypeAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    List<CrimeType> checkedCrimeTypesList = new ArrayList<>();
                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

                    if (checkedItems != null) {
                        Set<Integer> checkedCrimeTypesId = new HashSet<>();

                        for (int i = 0; i < checkedItems.size(); i++) {
                            if(checkedItems.valueAt(i)) {
                                CrimeType ct = ClaimService.INST.getAvailableCrimeTypes().get(checkedItems.keyAt(i));
                                checkedCrimeTypesId.add(ct.getId());
                                checkedCrimeTypesList.add(ct);
                            }
                        }

                        ClaimService.INST.getClaim().getCrimetypes().clear();
                        ClaimService.INST.getClaim().getCrimetypes().addAll(checkedCrimeTypesId);
                        nextButton.setVisibility(checkedCrimeTypesList.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }

        for(Integer crimeTypeId : ClaimService.INST.getClaim().getCrimetypes()){
            listView.setItemChecked(crimeTypeId - 1, true);
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setEnabled(false);
                ((MainActivity)getActivity()).showPage(MainActivity.PAGE_MAP);
            }
        });

        nextButton.setVisibility(ClaimService.INST.getClaim().getCrimetypes().size() > 0 ? View.VISIBLE : View.GONE);
    }

    public static BaseFragment newInstance() {
        return new ClaimTypeFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        claimStateLogging();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}