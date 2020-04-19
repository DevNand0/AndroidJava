package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.Models.MessageTransaction;
import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;

public class ItemMessageTransactionAdapter extends ArrayAdapter {

    private Context c;
    private LayoutInflater layoutInflater;
    private ArrayList<MessageTransaction> messages;

    private TextView tv_msg_title,tv_msg_info;
    private ImageView iv_ico;

    public ItemMessageTransactionAdapter(Context c, ArrayList<MessageTransaction> messages){
        super(c,0,messages);
        this.c=c;
        this.layoutInflater =(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.messages=messages;
    }


    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(convertView==null){
            v = layoutInflater.inflate(R.layout.adapter_item_message_transaction,null,true);
        }
        MessageTransaction mt = messages.get(position);
        tv_msg_title = (TextView)v.findViewById(R.id.tv_msg_title);
        tv_msg_info = (TextView)v.findViewById(R.id.tv_msg_info);
        iv_ico = (ImageView)v.findViewById(R.id.iv_ico);
        tv_msg_title.setText(mt.getTitle());
        tv_msg_info.setText(mt.getMessage());
        int ico = (mt.isSuccess())?R.drawable.ic_success_24dp:R.drawable.ic_warning_black_24dp;
        iv_ico.setImageResource(ico);
        return v;
    }
}
