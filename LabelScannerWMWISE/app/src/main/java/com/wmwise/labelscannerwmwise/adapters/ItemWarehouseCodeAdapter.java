package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.Models.Warehouse;
import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;

public class ItemWarehouseCodeAdapter extends ArrayAdapter {

    private TextView tv_wr_code;
    private Context c;
    private LayoutInflater layoutInflater;
    private ArrayList<Warehouse> warehouses;
    private Warehouse warehouse;

    public ItemWarehouseCodeAdapter(Context c,ArrayList<Warehouse> w){
        super(c,0,w);
        this.c=c;
        this.layoutInflater =(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.warehouses=w;
    }

    @Override
    public int getCount() {
        return warehouses.size();
    }

    @Override
    public Object getItem(int position) {
        return warehouses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(convertView==null){
            v = layoutInflater.inflate(R.layout.adapter_item_warehouse_code,null,true);
        }
        tv_wr_code = (TextView)v.findViewById(R.id.tv_wr_code);
        warehouse=warehouses.get(position);
        tv_wr_code.setText(warehouse.getCode());
        return v;
    }
}
