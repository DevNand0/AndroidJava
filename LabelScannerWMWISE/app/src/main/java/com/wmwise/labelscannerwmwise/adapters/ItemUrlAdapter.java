package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.Models.UrlBaseConfiguration;
import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;

public class ItemUrlAdapter extends BaseAdapter {


    private Context c;
    private LayoutInflater layoutInflater;
    private TextView tv_app_name, tv_url;
    private ArrayList<UrlBaseConfiguration> URLlist;

    public ItemUrlAdapter(Context c, ArrayList<UrlBaseConfiguration> URLlist){
        this.c=c;
        this.layoutInflater =(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.URLlist=URLlist;
    }

    @Override
    public int getCount() {
        return this.URLlist.size();
    }

    @Override
    public Object getItem(int position) {
        return this.URLlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view  = convertView;
        if(convertView==null){
            view = layoutInflater.inflate(R.layout.adapter_url,null,true);
        }

        tv_app_name =(TextView)view.findViewById(R.id.tv_app_name);
        tv_url =(TextView)view.findViewById(R.id.tv_url);

        UrlBaseConfiguration url = URLlist.get(position);

        tv_app_name.setText(url.getName());
        tv_url.setText(url.getApp_url());

        return view;
    }
}
