package ua.in.badparking.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import ua.in.badparking.R;
import ua.in.badparking.model.CrimeType;

/**
 * Created by Volodymyr Dranyk on 7/6/2016.
 */
public class CrimeTypeAdapter extends ArrayAdapter<CrimeType> {

    public CrimeTypeAdapter(Context context, List<CrimeType> crimeTypes, Button nextButton) {
        super(context, 0, crimeTypes);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_report_type, parent, false);
        }

        CrimeType crimeType = getItem(position);
        TextView crimeTypeName = (TextView) convertView.findViewById(R.id.list_item);
        crimeTypeName.setText(crimeType.getName());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getName().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}