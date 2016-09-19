package name.javalex.apijson.app.urlshortener.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import name.javalex.apijson.app.urlshortener.R;
import name.javalex.apijson.app.urlshortener.entities.LongShortDate;

public class ListAdapter extends ArrayAdapter<LongShortDate> {

    private int layoutResource;

    public ListAdapter(Context context, int layoutResource, List<LongShortDate> longShortDateList) {
        super(context, layoutResource, longShortDateList);
        this.layoutResource = layoutResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(layoutResource, null);
        }

        LongShortDate longShortDate = getItem(position);

        if (longShortDate != null) {
            TextView leftTextView = (TextView) view.findViewById(R.id.longUrlTextView);
            TextView rightTextView = (TextView) view.findViewById(R.id.shortUrlTextView);
            TextView centreTextView = (TextView) view.findViewById(R.id.dateTimeTextView);

            if (leftTextView != null) {
                leftTextView.setText(longShortDate.getLongLink());
            }

            if (rightTextView != null) {
                rightTextView.setText(longShortDate.getShortLink());
            }

            if (centreTextView != null) {
                centreTextView.setText(longShortDate.getDateAndTime());
            }
        }

        return view;
    }
}