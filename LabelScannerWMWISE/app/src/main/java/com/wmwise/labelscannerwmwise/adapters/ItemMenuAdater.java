package com.wmwise.labelscannerwmwise.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wmwise.labelscannerwmwise.R;

/**
 * Created by Pedro Avellaneda on 19/03/2018.
 */

public class ItemMenuAdater extends RecyclerView.Adapter <ItemMenuAdater.ElemHolder>{
    private Context ctx;
    private int images[];
    private String titles[];
    private String descriptions[];
    private int backgrounds[];

    public ItemMenuAdater(Context c,int list_images[],int list_backgrounds[],String list_titles[],String list_descriptions[]){
        ctx=c;
        images=list_images;
        backgrounds=list_backgrounds;
        titles=list_titles;
        descriptions=list_descriptions;
    }

    @Override
    public ElemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item,null);
        ElemHolder holder = new ElemHolder(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(ElemHolder holder, int pos) {
        holder.iv_option.setImageResource(images[pos]);
        holder.iv_option.setBackgroundResource(backgrounds[pos]);
        holder.tv_title.setText(titles[pos]);
        holder.tv_description.setText(descriptions[pos]);

    }

    @Override
    public int getItemCount() {
        return titles.length;
    }


    public static class ElemHolder extends RecyclerView.ViewHolder{
        ImageView iv_option;
        TextView tv_title, tv_description;


        public ElemHolder(View itemView) {
            super(itemView);
            iv_option = (ImageView)itemView.findViewById(R.id.iv_option);
            tv_title = (TextView)itemView.findViewById(R.id.tv_title);
            tv_description = (TextView)itemView.findViewById(R.id.tv_description);
        }


    }

}
