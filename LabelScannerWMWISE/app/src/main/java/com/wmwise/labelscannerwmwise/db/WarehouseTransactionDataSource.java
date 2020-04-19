package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.Warehouse;

import java.util.ArrayList;

public class WarehouseTransactionDataSource {

    public static String allColumns[]= {
            DataBaseHelper.ID_WR_TRANSACTION,
            DataBaseHelper.WR_TRANSACTION_CODE,
            DataBaseHelper.WR_TRANSACTION_TYPE
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public WarehouseTransactionDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public boolean save(Warehouse wl){
        if(!update(wl))
            return insert(wl);
        return false;
    }

    public boolean update(Warehouse wl){
        //String where =DataBaseHelper.WR_TRANSACTION_CODE+"='"+wl.getCode()+"' AND "+DataBaseHelper.WR_TRANSACTION_TYPE+"='"+wl.getOp()+"'";
        String where =DataBaseHelper.WR_TRANSACTION_CODE+"='"+wl.getCode()+"'";
        ContentValues v = WarehouseValues(wl);
        long resp = database.update(DataBaseHelper.TBL_WR_TRANSACTION,v,where,null);
        return (resp>0);
    }

    public boolean insert(Warehouse wl){
        ContentValues v = WarehouseValues(wl);
        long resp = database.insert(DataBaseHelper.TBL_WR_TRANSACTION,null,v);
        return (resp>0);
    }


    private ContentValues WarehouseValues(Warehouse wl){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.WR_TRANSACTION_CODE,wl.getCode());
        values.put(DataBaseHelper.WR_TRANSACTION_TYPE,String.valueOf(wl.getOp()));
        return values;
    }


    public boolean destroy(String wr_code){
        String where =DataBaseHelper.WR_TRANSACTION_CODE+"='"+wr_code+"'";
        int delete_elem = database.delete(DataBaseHelper.TBL_WR_TRANSACTION,where,null);
        return (delete_elem>0);
    }

    public boolean destroyWarehousesCollection(){
        int delete_detail = database.delete(DataBaseHelper.TBL_WR_TRANSACTION,null,null);
        return (delete_detail>0);
    }


    private Warehouse cursorWarehouse(Cursor c){
        Warehouse w=new Warehouse();
        w.setReceipt_id(c.getInt(c.getColumnIndex(DataBaseHelper.ID_WR_TRANSACTION)));
        w.setCode(c.getString(c.getColumnIndex(DataBaseHelper.WR_TRANSACTION_CODE)));
        w.setOp(c.getString(c.getColumnIndex(DataBaseHelper.WR_TRANSACTION_TYPE)).charAt(0));
        return w;
    }

    public ArrayList<Warehouse> warehouseList(){
        ArrayList<Warehouse> lista = new ArrayList<>();
        Cursor c = null;
        try{
            c=database.query(DataBaseHelper.TBL_WR_TRANSACTION,allColumns,null,null,null,null,null);
            c.moveToFirst();
            while(!c.isAfterLast()){
                lista.add(cursorWarehouse(c));
                c.moveToNext();
            }
        }catch(SQLException ex){
            ex.getMessage();
        }
        return lista;
    }


}
