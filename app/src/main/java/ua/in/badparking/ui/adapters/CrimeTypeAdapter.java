package ua.in.badparking.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import ua.in.badparking.R;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.services.ClaimState;

/**
 * Created by Volodymyr Dranyk on 7/6/2016.
 */
public class CrimeTypeAdapter extends ArrayAdapter<CrimeType> {
    //private final List<CrimeType> crimeTypeList;
    private Button nextButton;

    public CrimeTypeAdapter(Context context, List<CrimeType> objects, Button nextButton) {
        super(context, R.layout.listitem_report_type, objects);
        ClaimState.INST.setCrimeTypes(objects);
        this.nextButton = nextButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_report_type,
                    parent, false);

            viewHolder = new ViewHolder();
            viewHolder.todoName = (TextView)convertView
                    .findViewById(R.id.list_item);
            viewHolder.checkBox = (CheckBox)convertView
                    .findViewById(R.id.checkBox);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer)buttonView.getTag();
                    ClaimState.INST.getCrimeTypes().get(getPosition).setSelected(buttonView.isChecked());

                    if(ClaimState.INST.getSelectedCrimeTypes().isEmpty()){
                        nextButton.setVisibility(View.GONE);
                    } else nextButton.setVisibility(View.VISIBLE);
                }
            });

            convertView.setTag(viewHolder);
            convertView.setTag(R.id.list_item, viewHolder.todoName);
            convertView.setTag(R.id.checkBox, viewHolder.checkBox);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.checkBox.setTag(position);
        viewHolder.todoName.setText(ClaimState.INST.getCrimeTypes().get(position).getName());
        viewHolder.checkBox.setChecked(ClaimState.INST.getCrimeTypes().get(position).isSelected());

        return convertView;
    }

    static class ViewHolder {
        public TextView todoName;
        public CheckBox checkBox;
    }
}