package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.Tracking;

import java.util.ArrayList;

public class TrackingDataSource {

    public static String allColumns[]= {
            DataBaseHelper.ID_TRACKING,
            DataBaseHelper.ID_CARRIER_TRACKING,
            DataBaseHelper.CARRIER_NAME_TRACKING,
            DataBaseHelper.CODE_TRACKING,
            DataBaseHelper.STATUS_TRACKING
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public TrackingDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public boolean save(Tracking t){
        if(!update(t))
            return insert(t);
        return false;
    }

    public boolean update(Tracking t){
        String where =DataBaseHelper.ID_CARRIER_TRACKING+"="+t.getId_carrier()+" AND "+DataBaseHelper.CODE_TRACKING+"='"+t.getCode()+"'";
        ContentValues v = TrackingValues(t);
        long resp = database.update(DataBaseHelper.TBL_TRACKING,v,where,null);
        return (resp>0);
    }

    public boolean insert(Tracking t){
        ContentValues v = TrackingValues(t);
        long resp = database.insert(DataBaseHelper.TBL_TRACKING,null,v);
        return (resp>0);
    }



    private ContentValues TrackingValues(Tracking t){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.ID_CARRIER_TRACKING,t.getId_carrier());
        values.put(DataBaseHelper.CARRIER_NAME_TRACKING,t.getCarrier_name());
        values.put(DataBaseHelper.CODE_TRACKING,t.getCode());
        values.put(DataBaseHelper.STATUS_TRACKING,1);
        return values;
    }



    public ArrayList<Tracking> trackingList(){
        ArrayList<Tracking> lista = new ArrayList<>();
        Cursor c = null;
        try{
            String where= DataBaseHelper.STATUS_TRACKING+"=1";
            c=database.query(DataBaseHelper.TBL_TRACKING,allColumns,where,null,null,null,null);
            c.moveToFirst();
            while(!c.isAfterLast()){
                lista.add(cursorTracking(c));
                c.moveToNext();
            }
        }catch(SQLException ex){
            ex.getMessage();
        }
        return lista;
    }



    private Tracking cursorTracking(Cursor c){
        Tracking t=new Tracking();
        t.setCarrier_name(c.getString(c.getColumnIndex(DataBaseHelper.CARRIER_NAME_TRACKING)));
        t.setId_carrier(c.getInt(c.getColumnIndex(DataBaseHelper.ID_CARRIER_TRACKING)));
        t.setStatus(c.getInt(c.getColumnIndex(DataBaseHelper.STATUS_TRACKING)));
        t.setCode(c.getString(c.getColumnIndex(DataBaseHelper.CODE_TRACKING)));
        return t;
    }



    public boolean destroy(){
        String where= DataBaseHelper.STATUS_TRACKING+"=1";
        int delete_detail = database.delete(DataBaseHelper.TBL_TRACKING,where,null);
        return (delete_detail>0);
    }


    public boolean destroyAllTracking(int carrier_id){
        String where= DataBaseHelper.ID_CARRIER_TRACKING+"="+carrier_id;
        int delete_detail = database.delete(DataBaseHelper.TBL_TRACKING,where,null);
        return (delete_detail>0);
    }


}
