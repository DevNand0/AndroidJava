package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.wmwise.labelscannerwmwise.Models.Item;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.obj.Params;

import java.util.ArrayList;

public class ItemWarehouseDataSource {
    public String [] allColumns = {
            DataBaseHelper.RECEIPT_ID,
            DataBaseHelper.LOADING_GUIDE_ITEM_ID,
            DataBaseHelper.WAREHOUSE_CODE,
            DataBaseHelper.DIMS,
            DataBaseHelper.PCS,
            DataBaseHelper.PCS_LOADED,
            DataBaseHelper.PCS_PICKED,
            DataBaseHelper.PICKED,
            DataBaseHelper.LOADED,
            DataBaseHelper.LINE
    };

    public String [] allColumnsDetails = {
            DataBaseHelper.DETAIL_RECEIPT_ID,
            DataBaseHelper.WAREHOUSE_FULL_CODE,
            DataBaseHelper.DET_PCS_LOADED,
            DataBaseHelper.DET_PCS_PICKED,
            DataBaseHelper.DATEPICK,
            DataBaseHelper.DATELOAD
    };

    private boolean success;
    private String message;

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public ItemWarehouseDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
        success=false;
        message="";
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public boolean isSuccess(){
        return success;
    }

    public String getMessage(){
        return message;
    }

    private ContentValues item_warehouseValues(Item i,String warehouse,boolean pick){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.RECEIPT_ID,i.getReceipt_id());
        v.put(DataBaseHelper.DIMS,i.getDims());
        v.put(DataBaseHelper.PCS,i.getPcs());
        v.put(DataBaseHelper.LINE,i.getLine());
        v.put(DataBaseHelper.LOADING_GUIDE_ITEM_ID,i.getLg_id());
        if(pick){//caso de hacer pick
            v.put(DataBaseHelper.PCS_PICKED,i.getPcs_picked());
            v.put(DataBaseHelper.PICKED,i.isPicked());
        }else{
            v.put(DataBaseHelper.PCS_LOADED,i.getPcs_loaded());
            v.put(DataBaseHelper.LOADED,i.isLoaded());
        }
        v.put(DataBaseHelper.WAREHOUSE_CODE,warehouse);
        return v;
    }

    private ContentValues item_warehouseDetailsValues(Item i,String warehouse,boolean pick){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.DETAIL_RECEIPT_ID,i.getReceipt_id());
        if(pick){//caso de hacer pick
            v.put(DataBaseHelper.DATEPICK, Params.currentDate(true));
            v.put(DataBaseHelper.DET_PCS_PICKED,i.getPcs_picked());
        }else{
            v.put(DataBaseHelper.DATELOAD, Params.currentDate(true));
            v.put(DataBaseHelper.DET_PCS_LOADED,i.getPcs_loaded());
        }
        v.put(DataBaseHelper.WAREHOUSE_FULL_CODE,warehouse);

        return v;
    }

    private boolean UpdateMaster(Item i, String warehouse,boolean pick){
        ContentValues values = item_warehouseValues(i,warehouse,pick);
        String where = DataBaseHelper.WAREHOUSE_CODE+"= '"+warehouse+"' AND "+DataBaseHelper.LINE+"="+i.getLine();
        long resp = database.update(DataBaseHelper.TBL_ITEM_WAREHOUSE,values,where,null);
        return (resp>0)?true:false;
    }

    private boolean CreateMaster(Item i, String warehouse,boolean pick){
        ContentValues values = item_warehouseValues(i,warehouse,pick);
        long resp = database.insert(DataBaseHelper.TBL_ITEM_WAREHOUSE,null,values);
        return (resp>0)?true:false;
    }

    public boolean SaveData(Item i, String warehouse_full_code,boolean pick){
        String warehouse[] = warehouse_full_code.split("-");
        boolean saved = false;
        if(itemExist(warehouse[0],i.getLine()))
            saved = UpdateMaster(i,warehouse[0],pick);
        else
            saved = CreateMaster(i,warehouse[0],pick);
        return saved;
    }

    /**
    * saveDataDetail
    * return @int
    * 0=(error de grabado), 1=ya tiene pick, 2=quiere hacer load pero no ha hecho pick,
    * 3=ya ha hecho load, 4=pick creado con exito, 5=load creado con exito
    * */
    public int saveDataDetail(Item i, String warehouse_full_code,boolean pick){
        int saved_code=0;
        boolean saved=false;
        Item tmp_item = getDetailItem(warehouse_full_code);
        if( tmp_item!=null ){
            if( tmp_item.getPcs_picked()>1 && i.getPcs_picked()>1 && pick )
                saved_code=1;
            else if( tmp_item.getPcs_picked()==0 && i.getPcs_loaded()>0 && !pick )
                saved_code=2;
            else if( tmp_item.getPcs_loaded()>1 && i.getPcs_loaded()>1 && !pick )
                saved_code=3;
            else if(tmp_item.getPcs_picked()>0 && tmp_item.getPcs_loaded()==0 && i.getPcs_loaded()>0 && !pick){
                saved = UpdateDetail(i,warehouse_full_code,pick);
                saved_code=(saved)?5:0;
            }
        }else{
            if(i.getPcs_picked()>0 && pick){
                //hacer pick a la primera
                saved = CreateDetail(i,warehouse_full_code,pick);
                saved_code=(saved)?4:0;
            } else if(i.getPcs_loaded()>0 && !pick){
                //hacer load a la primera
                saved_code=2;
            }
        }
        return saved_code;
    }


    private boolean UpdateDetail(Item i, String warehouse, boolean pick){
        ContentValues values = item_warehouseDetailsValues(i,warehouse,pick);
        String where = DataBaseHelper.RECEIPT_ID+"="+i.getReceipt_id()+" AND "+DataBaseHelper.WAREHOUSE_FULL_CODE+"= '"+warehouse+"'";
        long resp = database.update(DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL,values,where,null);
        return (resp>0)?true:false;
    }

    private boolean CreateDetail(Item i, String warehouse, boolean pick){
        ContentValues values = item_warehouseDetailsValues(i,warehouse,pick);
        long resp = database.insert(DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL,null,values);
        return (resp>0)?true:false;
    }

    public Item getDetailItem(String wr_full_code){
        Item item = null;
        Cursor cursor = null;
        try{
            cursor = database.query(DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL,allColumnsDetails,DataBaseHelper.WAREHOUSE_FULL_CODE+"= '"+wr_full_code+"'",null,null,null,null);
            if(cursor.getCount()>0){
               cursor.moveToFirst();
                item =new Item();
                item.setReceipt_id(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.DETAIL_RECEIPT_ID)));
                item.setPcs_loaded(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.DET_PCS_LOADED)));
                item.setPcs_picked(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.DET_PCS_PICKED)));
            }
        }finally{
            cursor.close();
        }
        return item;
    }

    public Item getItem(String wr_code,int line){
        Item item =new Item();
        Cursor cursor=null;
        try{
            cursor = database.query(DataBaseHelper.TBL_ITEM_WAREHOUSE,allColumns,DataBaseHelper.WAREHOUSE_CODE+"= '"+wr_code+"' AND "+DataBaseHelper.LINE+"="+line,null,null,null,null);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                item.setReceipt_id(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.RECEIPT_ID)));
                item.setPcs_loaded(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.PCS_LOADED)));
                item.setPcs_picked(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.PCS_PICKED)));
                item.setLine(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.LINE)));
                item.setPcs(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.PCS)));
                item.setLg_id(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.LOADING_GUIDE_ITEM_ID)));
                String dims [] = cursor.getString(cursor.getColumnIndex(DataBaseHelper.DIMS)).split(" X ");
                item.setHeight(dims[0]);
                item.setWidth(dims[1]);
                item.setLength(dims[2]);
            }
        }finally{
            cursor.close();
        }
        return item;
    }


    private boolean itemExist(String wr_code, int line){
        Cursor c=null;
        try{
            String where = DataBaseHelper.WAREHOUSE_CODE+"= '"+wr_code+"' AND "+DataBaseHelper.LINE+"= "+line;
            c =database.query(DataBaseHelper.TBL_ITEM_WAREHOUSE,allColumns,where,null,null,null,null);
            if(c.getCount()>0){
                return true;
            }else{
                return false;
            }
        }finally{
            c.close();
        }
    }

    public Item getDetailsPickedLoad(String wr_sub_code){
        Item i = new Item();
        Cursor c= null;
        try{
            String pick = "SELECT COUNT("+DataBaseHelper.DET_PCS_PICKED+") " +
                          "FROM "+DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL+" " +
                          "WHERE "+DataBaseHelper.DET_PCS_PICKED+" > 0 " +
                          "AND "+DataBaseHelper.WAREHOUSE_FULL_CODE+" LIKE '"+wr_sub_code+"%'";
            String load = "SELECT COUNT("+DataBaseHelper.DET_PCS_LOADED+") " +
                          "FROM "+DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL+" " +
                          "WHERE "+DataBaseHelper.DET_PCS_LOADED+" > 0 " +
                          "AND "+DataBaseHelper.WAREHOUSE_FULL_CODE+" LIKE '"+wr_sub_code+"%'";
            String query = "SELECT DISTINCT ("+pick+") AS pick,("+load+") AS load FROM "+DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL;
            c = database.rawQuery(query, null);
            if(c.getCount()>0){
                c.moveToFirst();
                i.setPcs_picked(c.getInt(c.getColumnIndex("pick")));
                i.setPcs_loaded(c.getInt(c.getColumnIndex("load")));
            }
        }finally{
            c.close();
        }
        return i;
    }

    public boolean DestroyWarehousesLoadingGuide(int loadingGuide_id){
        Cursor c =null;
        boolean deleted = false;
        String where = DataBaseHelper.LOADING_GUIDE_ITEM_ID+"= "+loadingGuide_id;
        c =database.query(DataBaseHelper.TBL_ITEM_WAREHOUSE,allColumns,where,null,null,null,null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            int id=c.getInt(c.getColumnIndex(DataBaseHelper.RECEIPT_ID));
            String query_delete_items_detail = DataBaseHelper.DETAIL_RECEIPT_ID+"="+id;
            int delete_detail = database.delete(DataBaseHelper.TBL_ITEM_WAREHOUSE_DETAIL,query_delete_items_detail,null);
            if(delete_detail>0){
                String query_delete_master = DataBaseHelper.RECEIPT_ID+"="+id;
                int delete_master=database.delete(DataBaseHelper.TBL_ITEM_WAREHOUSE,query_delete_master,null);
                if(delete_master>0){
                    deleted = true;
                }else{
                    return false;
                }
            }
            c.moveToNext();
        }

        return deleted;
    }

}
