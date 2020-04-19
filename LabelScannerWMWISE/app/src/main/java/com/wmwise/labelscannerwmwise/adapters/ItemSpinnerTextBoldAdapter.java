package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.R;

public class ItemSpinnerTextBoldAdapter extends BaseAdapter {

    private String items [];
    private Context c;
    private LayoutInflater inflater;


    public ItemSpinnerTextBoldAdapter( Context context,String[] item) {

        this.c=context;
        this.items=item;
        this.inflater = (LayoutInflater)c.getSystemService(c.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.length ;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(convertView==null){
            v = this.inflater.inflate(R.layout.spinner_item_text,null);
        }
        TextView tv_item= v.findViewById(R.id.tv_item);
        tv_item.setText(this.items[position]);

        return v;
    }


}
