package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.Carrier;

import java.util.ArrayList;

public class CarrierDataSource {
    public String [] allColumns = {
            DataBaseHelper.ID_CARRIER,
            DataBaseHelper.CODE_CARRIER,
            DataBaseHelper.CARRIER_NAME,
            DataBaseHelper.CARRIER_ADDRESS,
            DataBaseHelper.CARRIER_CITY,
            DataBaseHelper.CARRIER_TYPE,
            DataBaseHelper.CARRIER_STATUS
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;


    public CarrierDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public long saveCarrier(Carrier c){
        disableCarriers(c.getId());
        if(CarrierExists(c.getId()))
            return actualizar(c);
        else
            return insert(c);
    }

    public ArrayList<Carrier> getCarriers(){
        ArrayList<Carrier> lista = new ArrayList<Carrier>();
        Cursor cursor = null;
        try{
            String where= DataBaseHelper.CARRIER_STATUS+"=1";
            cursor=database.query(DataBaseHelper.TBL_CARRIER,allColumns,where,null,null,null,null);
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                lista.add(cursorCarrier(cursor));
                cursor.moveToNext();
            }
        }catch(SQLException e){
            lista=null;
        }finally {
            cursor.close();
        }
        return lista;
    }

    public Carrier getCarrier(){
        Carrier carrier=null;
        Cursor c =null;
        try{
            String where = DataBaseHelper.CARRIER_STATUS+"="+1;
            c=database.query(DataBaseHelper.TBL_CARRIER,allColumns,null,null,null,null,null);
            if(c.getCount()>0){
                c.moveToFirst();
                carrier = cursorCarrier(c);
            }
        }catch(SQLException e){

        }
        return carrier;
    }


    private ContentValues carrierValues(Carrier c){
        ContentValues v = new ContentValues();

        v.put(DataBaseHelper.ID_CARRIER,c.getId());

        //if(!c.getName().equals(""))
            v.put(DataBaseHelper.CARRIER_NAME,c.getName());
        //if(!c.getAddress().equals(""))
            v.put(DataBaseHelper.CARRIER_ADDRESS,c.getAddress());
        //if(!c.getCity().equals(""))
            v.put(DataBaseHelper.CARRIER_CITY,c.getCity());
        //if(!c.getCode().equals(""))
            v.put(DataBaseHelper.CODE_CARRIER,c.getCode());
        v.put(DataBaseHelper.CARRIER_TYPE, c.getType());
        v.put(DataBaseHelper.CARRIER_STATUS,1);
        return v;
    }

    private long insert(Carrier c){
        ContentValues v = carrierValues(c);
        long resp = database.insert(DataBaseHelper.TBL_CARRIER,null,v);
        return resp;
    }

    private long actualizar(Carrier c){
        ContentValues v = carrierValues(c);
        long resp = database.update(DataBaseHelper.TBL_CARRIER,v,DataBaseHelper.ID_CARRIER+"="+c.getId(),null);
        return resp;
    }


    public long disableCarriers(int id){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.CARRIER_STATUS,0);
        long resp = database.update(DataBaseHelper.TBL_CARRIER, v, DataBaseHelper.ID_CARRIER+"<>"+id,null);
        return resp;
    }


    private boolean CarrierExists(int id){
        Cursor c =null;
        boolean exists=false;
        try{
            c=database.query(DataBaseHelper.TBL_CARRIER,allColumns,DataBaseHelper.ID_CARRIER+"="+id,null,null,null,null);
            exists=(c.getCount()>0);
        }catch(SQLException e){
            exists=false;
        }
        return exists;
    }

    private Carrier cursorCarrier(Cursor c){
        Carrier carrier= new Carrier();
        carrier.setId(c.getInt(c.getColumnIndex(DataBaseHelper.ID_CARRIER)));
        carrier.setCode(c.getString(c.getColumnIndex(DataBaseHelper.CODE_CARRIER)));
        carrier.setType(c.getInt(c.getColumnIndex(DataBaseHelper.CARRIER_TYPE)));
        carrier.setCity(c.getString(c.getColumnIndex(DataBaseHelper.CARRIER_CITY)));
        carrier.setName(c.getString(c.getColumnIndex(DataBaseHelper.CARRIER_NAME)));
        carrier.setAddress(c.getString(c.getColumnIndex(DataBaseHelper.CARRIER_ADDRESS)));
        carrier.setStatus(c.getInt(c.getColumnIndex(DataBaseHelper.CARRIER_STATUS)));
        return carrier;
    }

    public boolean destroy(){
        String where= DataBaseHelper.CARRIER_STATUS+"<>1";
        int delete_detail = database.delete(DataBaseHelper.TBL_CARRIER,where,null);
        return (delete_detail>0);
    }

    public boolean destroyCarrier(int carrier_id){
        String where= DataBaseHelper.ID_CARRIER+"="+carrier_id;
        int delete_carrier = database.delete(DataBaseHelper.TBL_CARRIER,where,null);
        return (delete_carrier>0);
    }

}
