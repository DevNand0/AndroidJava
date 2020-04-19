package com.wmwise.labelscannerwmwise.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;

import android.widget.TextView;

import com.wmwise.labelscannerwmwise.Models.Item;
import com.wmwise.labelscannerwmwise.Models.Warehouse;
import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;

/**
 * Created by Pedro Avellaneda on 10/04/2018.
 */

public class ItemWarehouseDetailsAdapter extends BaseAdapter {

    private GridLayout gl_data_content;
    private Context c;
    private LayoutInflater layoutInflater;
    private ArrayList<Warehouse> warehouses;
    private Warehouse warehouse;

    private TextView tv_consignee, tv_shipper, tv_wr_code, tv_picked;

    public ItemWarehouseDetailsAdapter(Context c,ArrayList<Warehouse> w){
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
        return warehouses.indexOf(position);
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view  = convertView;
        if(convertView==null){
                view = layoutInflater.inflate(R.layout.adapter_item_warehouse,null,true);
        }
        boolean complete_pick = false;
        boolean complete_loaded = false;
        tv_consignee = (TextView)view.findViewById(R.id.tv_consignee);
        tv_shipper = (TextView)view.findViewById(R.id.tv_shipper);
        tv_wr_code = (TextView)view.findViewById(R.id.tv_wr_code);
        tv_picked =(TextView) view.findViewById(R.id.tv_picked);
        gl_data_content = (GridLayout)view.findViewById(R.id.gl_data_content);

        warehouse = (warehouses.get(position)!=null)?warehouses.get(position):new Warehouse();
        String wr=warehouse.getCode();
        int size = warehouse.items.size();
        int rows = size+1;

        gl_data_content.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gl_data_content.removeAllViews();
        gl_data_content.setColumnCount(5);
        gl_data_content.setRowCount(rows);

        TextView pcs = new TextView(this.c);
        pcs.setText("Pcs");
        pcs.setTextColor(Color.parseColor("#FF000000"));
        pcs.setTypeface(null, Typeface.BOLD);
        pcs.setLayoutParams(layoutParams(0, 0));

        TextView type = new TextView(this.c);
        type.setText("Type");
        type.setTextColor(Color.parseColor("#FF000000"));
        type.setTypeface(null, Typeface.BOLD);
        type.setLayoutParams(layoutParams(0, 1));

        TextView dims = new TextView(this.c);
        dims.setText("Dims");
        dims.setTextColor(Color.parseColor("#FF000000"));
        dims.setTypeface(null, Typeface.BOLD);
        dims.setLayoutParams(layoutParams(0, 2));

        TextView picked = new TextView(this.c);
        picked.setText("Picked");
        picked.setTextColor(Color.parseColor("#FF000000"));
        picked.setTypeface(null, Typeface.BOLD);
        picked.setLayoutParams(layoutParams(0, 3));

        TextView loaded = new TextView(this.c);
        loaded.setText("Loaded");
        loaded.setTextColor(Color.parseColor("#FF000000"));
        loaded.setTypeface(null, Typeface.BOLD);
        loaded.setLayoutParams(layoutParams(0, 4));

        gl_data_content.addView(pcs, 0);
        gl_data_content.addView(type, 0);
        gl_data_content.addView(dims, 0);
        gl_data_content.addView(picked, 0);
        gl_data_content.addView(loaded, 0);

        if(size>0){

            TextView[] tv_tmp_pcs = new TextView[size] ;
            TextView[] tv_tmp_type = new TextView[size];
            TextView[] tv_tmp_dims = new TextView[size];
            TextView[] tv_checked_picked = new TextView[size];
            TextView[] tv_checked_loaded = new TextView[size];
            for(int i=0;i<size;i++){
                Item item = warehouse.items.get(i);

                int row = (i+1);
                int column = 0;

                tv_tmp_pcs[i] = new TextView(this.c);
                String piezas = String.valueOf(item.getPcs_loaded())+"/"+String.valueOf(item.getPcs());
                //tv_tmp_pcs[i].setText((i+1)+")  "+piezas);
                tv_tmp_pcs[i].setText(piezas);
                tv_tmp_pcs[i].setTextColor(Color.parseColor("#FF000000"));
                tv_tmp_pcs[i].setTypeface(null, Typeface.BOLD);
                tv_tmp_pcs[i].setWidth(120);
                tv_tmp_pcs[i].setLayoutParams(layoutParams(row, column));

                column++;
                tv_tmp_type[i] = new TextView(this.c);
                tv_tmp_type[i].setText(String.valueOf(item.getCargo_type_code()));
                tv_tmp_type[i].setTextColor(Color.parseColor("#FF000000"));
                tv_tmp_type[i].setTypeface(null, Typeface.BOLD);
                tv_tmp_type[i].setWidth(100);
                tv_tmp_type[i].setLayoutParams(layoutParams(row, column));

                column++;
                tv_tmp_dims[i] = new TextView(this.c);
                tv_tmp_dims[i].setText(String.valueOf(item.getDims()));
                tv_tmp_dims[i].setTextColor(Color.parseColor("#FF000000"));
                tv_tmp_dims[i].setTypeface(null, Typeface.BOLD);
                tv_tmp_dims[i].setWidth(170);
                tv_tmp_dims[i].setLayoutParams(layoutParams(row, column));


                tv_checked_picked[i] = new TextView(this.c);
                tv_checked_picked[i].setWidth(90);
                tv_checked_loaded[i] = new TextView(this.c);
                tv_checked_loaded[i].setWidth(120);
                int iconP = R.drawable.ic_check_box_outline_blank_black_24dp;
                int iconL = R.drawable.ic_check_box_outline_blank_black_24dp;
                if(item.getPcs_picked()==item.getPcs()){
                    iconP = R.drawable.ic_check_box_black_24dp;
                    complete_pick =true;
                }

                if(item.getPcs_loaded()==item.getPcs()){
                    iconL = R.drawable.ic_check_box_black_24dp;
                    complete_loaded =true;
                }

                column++;
                tv_checked_picked[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, iconP, 0);
                tv_checked_picked[i].setText("");
                tv_checked_picked[i].setLayoutParams(layoutParams(row, column));

                column++;
                tv_checked_loaded[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, iconL, 0);
                tv_checked_loaded[i].setText("");
                tv_checked_loaded[i].setLayoutParams(layoutParams(row, column));

                gl_data_content.addView(tv_tmp_pcs[i], row);
                gl_data_content.addView(tv_tmp_type[i], row);
                gl_data_content.addView(tv_tmp_dims[i], row);
                gl_data_content.addView(tv_checked_picked[i], row);
                gl_data_content.addView(tv_checked_loaded[i], row);

            }


        }


        tv_consignee.setText(warehouse.getConsignee_name());
        tv_shipper.setText(warehouse.getShipper_name());
        tv_wr_code.setText(warehouse.getCode());
        return view;
    }



    private GridLayout.LayoutParams layoutParams(int row, int column){
        GridLayout.LayoutParams param =new GridLayout.LayoutParams();
        param.height = LayoutParams.WRAP_CONTENT;
        param.width = LayoutParams.WRAP_CONTENT;
        param.rightMargin = 5;
        param.topMargin = 5;
        param.setGravity(Gravity.CENTER);
        param.columnSpec = GridLayout.spec(column);
        param.rowSpec = GridLayout.spec(row);
        return param;
    }


}
