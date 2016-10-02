package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
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