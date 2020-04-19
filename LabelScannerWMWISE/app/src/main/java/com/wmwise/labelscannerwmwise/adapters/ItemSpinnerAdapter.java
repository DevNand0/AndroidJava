package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.Models.SpinnerData;
import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;

/**
 * Created by Developer-02 on 12/04/2018.
 */

public class ItemSpinnerAdapter extends BaseAdapter {
    private ArrayList<SpinnerData> itemList;
    private LayoutInflater inflater;

    public ItemSpinnerAdapter(Context c,ArrayList<SpinnerData> list){
        this.itemList=list;
        inflater =(LayoutInflater)c.getSystemService(c.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view==null){
            view = inflater.inflate(R.layout.spinner_item,null);
        }
        TextView txt_item= view.findViewById(R.id.txt_item);
        txt_item.setText(this.itemList.get(position).getTypeNames());
        txt_item.setCompoundDrawablesWithIntrinsicBounds(this.itemList.get(position).getIcons(),0,0,0);
        return view;
    }


    @Override
    public int getCount() {
        return this.itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view==null){
            view = inflater.inflate(R.layout.spinner_item,null);
        }
        TextView txt_item= view.findViewById(R.id.txt_item);
        txt_item.setText(this.itemList.get(position).getTypeNames());
        txt_item.setCompoundDrawablesWithIntrinsicBounds(this.itemList.get(position).getIcons(),0,0,0);
        return view;
    }
}
