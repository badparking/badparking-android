package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ua.in.badparking.R;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.ui.activities.MainActivity;

public class PlateFragment extends BaseFragment implements View.OnClickListener{

    @BindView(R.id.plateEditText)
    protected EditText plateEditText;
    @BindView(R.id.next_button)
    protected Button nextButton;
    private Unbinder unbinder;

    public static PlateFragment newInstance() {
        return new PlateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plane, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nextButton.setVisibility(View.GONE);
        nextButton.setOnClickListener(this);

        plateEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if(plateEditText.getText().length() >= 3){
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    nextButton.setVisibility(View.GONE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button:
                ClaimState.INST.getClaim().setLicensePlates(plateEditText.getText().toString());
                ((MainActivity)getActivity()).moveToNext();
                break;
        }
    }
}
