package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.UrlBaseConfiguration;

import java.util.ArrayList;

public class UrlBaseConfigurationDataSource {

    public static String allColumns[]= {
            DataBaseHelper.ID_URL,
            DataBaseHelper.STATUS_URL,
            DataBaseHelper.APP_NAME,
            DataBaseHelper.URL_BASE,
            DataBaseHelper.APP_URL
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public UrlBaseConfigurationDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public long update(UrlBaseConfiguration url){
        ContentValues v = UrlBaseConfigurationValues(url);
        long resp = database.update(DataBaseHelper.TBL_URL_PARAMS,v,DataBaseHelper.URL_BASE+"='"+url.getUrl()+"'",null);
        return resp;
    }

    public long insert(UrlBaseConfiguration url){
        ContentValues v = UrlBaseConfigurationValues(url);
        long resp = database.insert(DataBaseHelper.TBL_URL_PARAMS,null,v);
        return resp;
    }

    private ContentValues UrlBaseConfigurationValues(UrlBaseConfiguration url){
        ContentValues values = new ContentValues();
        if(url.getId()!=0)
            values.put(DataBaseHelper.ID_URL,url.getId());
        values.put(DataBaseHelper.URL_BASE,url.getUrl());
        values.put(DataBaseHelper.STATUS_URL,url.isStatus());
        values.put(DataBaseHelper.APP_NAME,url.getName());
        values.put(DataBaseHelper.APP_URL,url.getApp_url());
        return values;
    }


    public UrlBaseConfiguration getURL(int status){
        UrlBaseConfiguration url = null;
        Cursor c = null;
        try{
            c=database.query(DataBaseHelper.TBL_URL_PARAMS,allColumns,DataBaseHelper.STATUS_URL+"="+status,null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                url= new UrlBaseConfiguration();
                url.setId(c.getInt(c.getColumnIndex(DataBaseHelper.ID_URL)));
                url.setName(c.getString(c.getColumnIndex(DataBaseHelper.APP_NAME)));
                url.setStatus(c.getInt(c.getColumnIndex(DataBaseHelper.STATUS_URL)));
                url.setUrl(c.getString(c.getColumnIndex(DataBaseHelper.URL_BASE)));
                url.setApp_url(c.getString(c.getColumnIndex(DataBaseHelper.APP_URL)));
                enableOneURL(url.getId());
            }
        }finally{
            c.close();
        }
        return url;
    }


    public void setDefaultURL(String url){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.STATUS_URL,1);
        database.update(DataBaseHelper.TBL_URL_PARAMS,values,DataBaseHelper.URL_BASE+"='"+url+"'",null);
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.STATUS_URL,0);
        database.update(DataBaseHelper.TBL_URL_PARAMS,v,DataBaseHelper.URL_BASE+"<>'"+url+"'",null);
    }


    public UrlBaseConfiguration loadStoredUrlApplication(String link){
        UrlBaseConfiguration url = null;
        Cursor c = null;
        try{
            c=database.query(DataBaseHelper.TBL_URL_PARAMS,allColumns,DataBaseHelper.URL_BASE+" = '"+link.toLowerCase()+"'",null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                url= new UrlBaseConfiguration();
                url.setId(c.getInt(c.getColumnIndex(DataBaseHelper.ID_URL)));
                url.setName(c.getString(c.getColumnIndex(DataBaseHelper.APP_NAME)));
                url.setStatus(c.getInt(c.getColumnIndex(DataBaseHelper.STATUS_URL)));
                url.setUrl(c.getString(c.getColumnIndex(DataBaseHelper.URL_BASE)));
                url.setApp_url(c.getString(c.getColumnIndex(DataBaseHelper.APP_URL)));
            }
        }finally{
            c.close();
        }
        return url;
    }

    private long enableOneURL(int id){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.STATUS_URL,0);
        long resp = database.update(DataBaseHelper.TBL_URL_PARAMS, v, DataBaseHelper.ID_URL+"<>"+id,null);
        return resp;
    }


    public ArrayList<UrlBaseConfiguration> all(){
        ArrayList<UrlBaseConfiguration> lista = new ArrayList<UrlBaseConfiguration>();
        Cursor c =null;
        try{
            c=database.query(DataBaseHelper.TBL_URL_PARAMS,allColumns,null,null,null,null,null);
            c.moveToFirst();
            while(!c.isAfterLast()){
                lista.add(cursorURL(c));
                c.moveToNext();
            }
        }finally{
            c.close();
        }
        return lista;
    }

    private UrlBaseConfiguration cursorURL(Cursor c){
        UrlBaseConfiguration url= new UrlBaseConfiguration();
        url.setId(c.getInt(c.getColumnIndex(DataBaseHelper.LOADING_GUIDE_ID)));
        url.setName(c.getString(c.getColumnIndex(DataBaseHelper.APP_NAME)));
        url.setUrl(c.getString(c.getColumnIndex(DataBaseHelper.URL_BASE)));
        url.setStatus(c.getInt(c.getColumnIndex(DataBaseHelper.STATUS_URL)));
        url.setApp_url(c.getString(c.getColumnIndex(DataBaseHelper.APP_URL)));
        return url;
    }

}
