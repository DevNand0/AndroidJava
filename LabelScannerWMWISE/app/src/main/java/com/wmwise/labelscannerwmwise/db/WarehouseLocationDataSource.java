package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.WarehouseLocation;

import java.util.ArrayList;


public class WarehouseLocationDataSource {

    public static String allColumns[]= {
            DataBaseHelper.ID_WR_LOCATION,
            DataBaseHelper.WR_LOCATION_CODE,
            DataBaseHelper.WR_BIN_LOCATION,
            DataBaseHelper.WR_LOCATION_USER_ID,
            DataBaseHelper.STATUS_WR_LOCATION,
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public WarehouseLocationDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public boolean save(WarehouseLocation wl){
        disableLocation(wl.getLocation_bin());
        if(!update(wl))
            return insert(wl);
        return false;
    }

    public boolean update(WarehouseLocation wl){
        String where =DataBaseHelper.WR_BIN_LOCATION+"='"+wl.getLocation_bin()+"' AND "+DataBaseHelper.WR_LOCATION_CODE+"='"+wl.getWr_code()+"'";
        ContentValues v = WarehouseLocationValues(wl);
        long resp = database.update(DataBaseHelper.TBL_WR_LOCATION,v,where,null);
        return (resp>0);
    }

    public boolean insert(WarehouseLocation wl){
        ContentValues v = WarehouseLocationValues(wl);
        long resp = database.insert(DataBaseHelper.TBL_WR_LOCATION,null,v);
        return (resp>0);
    }


    private ContentValues WarehouseLocationValues(WarehouseLocation wl){
        ContentValues values = new ContentValues();
        //values.put(DataBaseHelper.ID_WR_LOCATION,wl.getId());
        values.put(DataBaseHelper.WR_LOCATION_CODE,wl.getWr_code());
        values.put(DataBaseHelper.WR_BIN_LOCATION,wl.getLocation_bin());
        values.put(DataBaseHelper.WR_LOCATION_USER_ID,wl.getUser_id());
        values.put(DataBaseHelper.STATUS_WR_LOCATION,1);
        return values;
    }

    private WarehouseLocation cursorWarehouseLocation(Cursor c){
        WarehouseLocation wl=new WarehouseLocation();
        wl.setId(c.getInt(c.getColumnIndex(DataBaseHelper.ID_WR_LOCATION)));
        wl.setWr_code(c.getString(c.getColumnIndex(DataBaseHelper.WR_LOCATION_CODE)));
        wl.setLocation_bin(c.getString(c.getColumnIndex(DataBaseHelper.WR_BIN_LOCATION)));
        wl.setUser_id(c.getInt(c.getColumnIndex(DataBaseHelper.WR_LOCATION_USER_ID)));
        wl.setStatus(c.getInt(c.getColumnIndex(DataBaseHelper.STATUS_WR_LOCATION)));
        return wl;
    }

    public ArrayList<WarehouseLocation> warehouseLocationsList(){
        ArrayList<WarehouseLocation> lista = new ArrayList<>();
        Cursor c = null;
        try{
            String where= DataBaseHelper.STATUS_WR_LOCATION+"=1";
            c=database.query(DataBaseHelper.TBL_WR_LOCATION,allColumns,where,null,null,null,null);
            c.moveToFirst();
            while(!c.isAfterLast()){
                lista.add(cursorWarehouseLocation(c));
                c.moveToNext();
            }
        }catch(SQLException ex){
            ex.getMessage();
        }
        return lista;
    }

    public boolean destroy(int status){
        String where= DataBaseHelper.STATUS_WR_LOCATION+"="+status;
        int delete_detail = database.delete(DataBaseHelper.TBL_WR_LOCATION,where,null);
        return (delete_detail>0);
    }

    public boolean destroyLocation(int status, String location){
        String where= DataBaseHelper.STATUS_WR_LOCATION+"="+status+" AND "+DataBaseHelper.WR_BIN_LOCATION+"='"+location+"'";
        int delete_detail = database.delete(DataBaseHelper.TBL_WR_LOCATION,where,null);
        return (delete_detail>0);
    }


    private long disableLocation(String location){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.STATUS_WR_LOCATION,0);
        long resp = database.update(DataBaseHelper.TBL_WR_LOCATION, v, DataBaseHelper.WR_BIN_LOCATION+"<> '"+location+"'",null);
        return resp;
    }


}
