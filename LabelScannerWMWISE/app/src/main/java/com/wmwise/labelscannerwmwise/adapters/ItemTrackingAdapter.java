package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;

public class ItemTrackingAdapter extends ArrayAdapter {

    private TextView tv_tracking;
    private Context c;
    private LayoutInflater layoutInflater;
    private ArrayList<String> listaTracking;

    public ItemTrackingAdapter(Context c, ArrayList<String> trackings) {
        super(c, 0, trackings);
        this.c = c;
        this.layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listaTracking = trackings;
    }

    @Override
    public int getCount() {
        return listaTracking.size();
    }

    @Override
    public Object getItem(int position) {
        return listaTracking.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (convertView == null) {
            v = layoutInflater.inflate(R.layout.adapter_item_tracking, null, true);
        }
        tv_tracking = (TextView) v.findViewById(R.id.tv_tracking);
        tv_tracking.setText(listaTracking.get(position));
        return v;
    }
}