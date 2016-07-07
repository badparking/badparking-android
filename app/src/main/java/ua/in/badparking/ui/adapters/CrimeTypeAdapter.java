package ua.in.badparking.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import ua.in.badparking.R;
import ua.in.badparking.model.CrimeType;

/**
 * Created by Volodymyr Dranyk on 7/6/2016.
 */
public class CrimeTypeAdapter extends ArrayAdapter<CrimeType> {
    private final List<CrimeType> crimeTypeList;

    public CrimeTypeAdapter(Context context, List<CrimeType> objects) {
        super(context, R.id.list_item, objects);
        this.crimeTypeList = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_report_type,
                    parent, false);

            viewHolder = new ViewHolder();

            viewHolder.todoName = (TextView) convertView
                    .findViewById(R.id.list_item);
            viewHolder.checkBox = (CheckBox) convertView
                    .findViewById(R.id.checkBox);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();
                    crimeTypeList.get(getPosition).setSelected(buttonView.isChecked());
                }
            });

            convertView.setTag(viewHolder);
            convertView.setTag(R.id.list_item, viewHolder.todoName);
            convertView.setTag(R.id.checkBox, viewHolder.checkBox);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.checkBox.setTag(position);

        viewHolder.todoName.setText(crimeTypeList.get(position).getName());
        viewHolder.checkBox.setChecked(crimeTypeList.get(position).isSelected());

        return convertView;
    }

    static class ViewHolder {
        public TextView todoName;
        public CheckBox checkBox;
    }

    public List<CrimeType> getCrimeTypeList(){
        return crimeTypeList;
    }
}

