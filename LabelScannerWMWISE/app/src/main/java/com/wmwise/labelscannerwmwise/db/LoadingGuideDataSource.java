package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.LoadingGuide;

import java.util.ArrayList;

public class LoadingGuideDataSource {
    public static String allColumns[]= {
            DataBaseHelper.LOADING_GUIDE_ID,
            DataBaseHelper.TYPE,
            DataBaseHelper.LG_CODE,
            DataBaseHelper.COMPLETE,
            DataBaseHelper.TOTAL_PCS
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public LoadingGuideDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public boolean saveLoadingGuide(LoadingGuide lg){
        LoadingGuide tmp_lg = getLoadingGuide(lg.getCode());
        boolean saved;
        if(tmp_lg!=null){
            long updated = updateLG(lg);
            saved=(updated>0)?true:false;
        }else{
            long inserted = insertLG(lg);
            saved=(inserted>0)?true:false;
        }
        return saved;
    }

    private long updateLG(LoadingGuide lg){
        ContentValues v = LoadingGuideValues(lg);
        long resp = database.update(DataBaseHelper.TBL_LOADING_GUIDE,v,DataBaseHelper.LOADING_GUIDE_ID+"="+lg.getId(),null);
        return resp;
    }

    private long insertLG(LoadingGuide lg){
        ContentValues v = LoadingGuideValues(lg);
        long resp = database.insert(DataBaseHelper.TBL_LOADING_GUIDE,null,v);
        return resp;
    }

    public LoadingGuide getLoadingGuide(String code){
        LoadingGuide lg = null;
        Cursor c = null;
        try{
            c=database.query(DataBaseHelper.TBL_LOADING_GUIDE,allColumns,DataBaseHelper.LG_CODE+"='"+code+"'",null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                lg= new LoadingGuide();
                lg.setId(c.getInt(c.getColumnIndex(DataBaseHelper.LOADING_GUIDE_ID)));
                lg.setType(c.getString(c.getColumnIndex(DataBaseHelper.TYPE)));
                lg.setCode(c.getString(c.getColumnIndex(DataBaseHelper.LG_CODE)));
                lg.setComplete(c.getInt(c.getColumnIndex(DataBaseHelper.COMPLETE)));
                lg.setTotal_pcs(c.getInt(c.getColumnIndex(DataBaseHelper.TOTAL_PCS)));
            }
        }finally{
            c.close();
        }
        return lg;
    }

    private ContentValues LoadingGuideValues(LoadingGuide lg){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.LOADING_GUIDE_ID,lg.getId());
        values.put(DataBaseHelper.TYPE,lg.getType());
        values.put(DataBaseHelper.LG_CODE,lg.getCode());
        values.put(DataBaseHelper.COMPLETE,lg.getComplete());
        values.put(DataBaseHelper.TOTAL_PCS,lg.getTotal_pcs());
        return values;
    }


    public ArrayList<LoadingGuide> showLoadingGuides(int complete){
        ArrayList<LoadingGuide> lista = new ArrayList<LoadingGuide>();
        Cursor c =null;
        try{
            String where=DataBaseHelper.COMPLETE+"="+complete;
            c=database.query(DataBaseHelper.TBL_LOADING_GUIDE,allColumns,where,null,null,null,null);
            c.moveToFirst();
            while(!c.isAfterLast()){
                lista.add(cursorLoadingGuide(c));
                c.moveToNext();
            }
        }finally{
            c.close();
        }
        return lista;
    }

    private LoadingGuide cursorLoadingGuide(Cursor c){
        LoadingGuide lg= new LoadingGuide();
        lg.setId(c.getInt(c.getColumnIndex(DataBaseHelper.LOADING_GUIDE_ID)));
        lg.setCode(c.getString(c.getColumnIndex(DataBaseHelper.LG_CODE)));
        lg.setType(c.getString(c.getColumnIndex(DataBaseHelper.TYPE)));
        lg.setComplete(c.getInt(c.getColumnIndex(DataBaseHelper.COMPLETE)));
        lg.setTotal_pcs(c.getInt(c.getColumnIndex(DataBaseHelper.TOTAL_PCS)));
        return lg;
    }
}
