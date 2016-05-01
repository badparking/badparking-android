package ua.in.badparking.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ua.in.badparking.R;

/**
 * Created by Dima Kovalenko on 8/12/15.
 */
public class ReportTypeDialog extends Dialog {

    private String[] reportTypes;
    private ReportTypeChosenListener typeChosenListener;

    public ReportTypeDialog(Context context, ReportTypeChosenListener typeChosenListener) {
        super(context);
        this.typeChosenListener = typeChosenListener;
        _init(context);
    }

    public ReportTypeDialog(Context context, int theme, ReportTypeChosenListener typeChosenListener) {
        super(context, theme);
        this.typeChosenListener = typeChosenListener;
        _init(context);
    }

    protected ReportTypeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        _init(context);
    }

    private void _init(final Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_report_type);

        reportTypes = context.getResources().getStringArray(R.array.report_types);

        ListView listView = (ListView)findViewById(R.id.reportTypeList);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return reportTypes.length;
            }

            @Override
            public Object getItem(int i) {
                return reportTypes[i];
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View itemView = inflater.inflate(R.layout.listitem_report_type, parent, false);
                TextView textView = (TextView)itemView.findViewById(R.id.list_item);
                textView.setText((String)getItem(i));
                return itemView;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                typeChosenListener.onReportChosen(position);
                dismiss();
            }
        });
    }

    public interface ReportTypeChosenListener {
        public void onReportChosen(int typeId);
    }
}
