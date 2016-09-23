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

    private LayoutInflater layoutInflater;
    //List<LongShortDate> longShortDateList;

    private int layoutResource;

    public ListAdapter(Context context, int layoutResource, List<LongShortDate> longShortDateList) {
        super(context, layoutResource, longShortDateList);
        this.layoutResource = layoutResource;
        //this.longShortDateList = longShortDateList1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(layoutResource, null);
        }

        LongShortDate longShortDate = getItem(position);

        if (longShortDate != null) {
            TextView originalUrlTextView = (TextView) view.findViewById(R.id.longUrlTextView);
            TextView shortUrlTextView = (TextView) view.findViewById(R.id.shortUrlTextView);
            TextView timestampTextView = (TextView) view.findViewById(R.id.dateTimeTextView);

            if (originalUrlTextView != null) {
                originalUrlTextView.setText(longShortDate.getLongLink());
            }

            if (shortUrlTextView != null) {
                shortUrlTextView.setText(longShortDate.getShortLink());
            }

            if (timestampTextView != null) {
                timestampTextView.setText(longShortDate.getDateAndTime());
            }
        }

        return view;
    }


/*
    public void updateReceiptsList(List<LongShortDate> newlist) {
        longShortDateList.clear();
        longShortDateList.addAll(newlist);
        this.notifyDataSetChanged();
    }

    //Sum of items
    @Override
    public int getCount() {
        return longShortDateList.size();
    }

    //Item's position
    @Override
    public LongShortDate getItem(int position) {
        return longShortDateList.get(position);
    }
*/



}