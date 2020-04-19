package com.wmwise.labelscannerwmwise.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wmwise.labelscannerwmwise.Models.User;

public class UserDataSource {
    public String [] allColumns = {
            DataBaseHelper.ID_USER,
            DataBaseHelper.USERNAME,
            DataBaseHelper.FULLNAME,
            DataBaseHelper.DATE,
            DataBaseHelper.STATUS,
            DataBaseHelper.KEEPSESSION,
            DataBaseHelper.DIALOGTYPE,
            DataBaseHelper.INPUTTYPE,
            DataBaseHelper.DEVICEIP,
            DataBaseHelper.CURRENTUSER,
            DataBaseHelper.BUTTON_TYPE,
            DataBaseHelper.TOKEN,
            DataBaseHelper.ISROOT
    };

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private int countUsers;

    public UserDataSource(Context c){
        dbHelper = new DataBaseHelper(c);
    }

    public void open() throws SQLException{
        database = dbHelper.getReadableDatabase();
    }

    public void close(){
        dbHelper.close();
    }


    private ContentValues userValues(User u){
        ContentValues v = new ContentValues();

        v.put(DataBaseHelper.ID_USER,u.getId());
        if(!u.getUsername().toString().equals(""))
            v.put(DataBaseHelper.USERNAME,u.getUsername());
        if(!u.getName().toString().equals(""))
            v.put(DataBaseHelper.FULLNAME,u.getName());

        v.put(DataBaseHelper.DATE,u.getLogin_date());
        v.put(DataBaseHelper.DEVICEIP, u.getIp());
        v.put(DataBaseHelper.TOKEN, u.getToken());
        v.put(DataBaseHelper.STATUS,u.getStatus());
        v.put(DataBaseHelper.DIALOGTYPE,u.getOp_confirmacion());
        v.put(DataBaseHelper.INPUTTYPE, u.getOp_lector());
        v.put(DataBaseHelper.CURRENTUSER,u.getActive_user());
        v.put(DataBaseHelper.KEEPSESSION,u.isSin_login());
        v.put(DataBaseHelper.BUTTON_TYPE,u.getOp_buttons());
        v.put(DataBaseHelper.ISROOT,u.getIs_root());

        return v;
    }

    public long insert(User u){
        ContentValues v = userValues(u);
        long resp = database.insert(DataBaseHelper.TBL_USER,null,v);
        return resp;
    }


    public long updateSaveEvent(int saved,int idUser){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.SAVED_EVENT,saved);
        return database.update(DataBaseHelper.TBL_USER,values,DataBaseHelper.ID_USER+"="+idUser,null);
    }

    public long actualizar(User u){
        ContentValues v = userValues(u);
        long resp = database.update(DataBaseHelper.TBL_USER,v,DataBaseHelper.ID_USER+"="+u.getId(),null);
        return resp;
    }

    public long updateToken(int id,String token){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.TOKEN,token);
        return database.update(DataBaseHelper.TBL_USER,v,DataBaseHelper.ID_USER+"="+id,null);
    }

    public long disableUsers(int id){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.CURRENTUSER,0);
        v.put(DataBaseHelper.TOKEN,"");
        long resp = database.update(DataBaseHelper.TBL_USER, v, DataBaseHelper.ID_USER+"<>"+id,null);
        return resp;
    }

    public long logoutUser(int id){
        ContentValues v = new ContentValues();
        v.put(DataBaseHelper.CURRENTUSER,0);
        v.put(DataBaseHelper.TOKEN,"");
        long resp = database.update(DataBaseHelper.TBL_USER, v, DataBaseHelper.ID_USER+"="+id,null);
        return resp;
    }

    public User getUser(int id){
        User user = null;
        Cursor cursor = null;
        try {
            cursor = database.query(DataBaseHelper.TBL_USER,allColumns,DataBaseHelper.ID_USER+"="+id,null,null,null,null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                user =new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ID_USER)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(DataBaseHelper.USERNAME)));
                user.setName(cursor.getString(cursor.getColumnIndex(DataBaseHelper.FULLNAME)));
                user.setLogin_date(cursor.getString(cursor.getColumnIndex(DataBaseHelper.DATE)));
                user.setStatus(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.STATUS)));
                user.setSin_login(Boolean.parseBoolean(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.KEEPSESSION)))));
                user.setOp_confirmacion(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.DIALOGTYPE)));
                user.setOp_lector(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.INPUTTYPE)));
                user.setOp_buttons(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.BUTTON_TYPE)));
                user.setIp(cursor.getString(cursor.getColumnIndex(DataBaseHelper.DEVICEIP)));
                user.setToken(cursor.getString(cursor.getColumnIndex(DataBaseHelper.TOKEN)));
                user.setIs_root(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ISROOT)));
                countUsers=cursor.getCount();
            }
        }finally {
            cursor.close();
        }

        return user;
    }



    public User getActiveUser(){
        User user =  null;

        Cursor cursor = null;
        try {
            cursor = database.query(DataBaseHelper.TBL_USER,allColumns,DataBaseHelper.CURRENTUSER+"=1",null,null,null,null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                user =new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ID_USER)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(DataBaseHelper.USERNAME)));
                user.setName(cursor.getString(cursor.getColumnIndex(DataBaseHelper.FULLNAME)));
                user.setLogin_date(cursor.getString(cursor.getColumnIndex(DataBaseHelper.DATE)));
                user.setStatus(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.STATUS)));
                boolean keep_login = (cursor.getInt(cursor.getColumnIndex(DataBaseHelper.KEEPSESSION))==1)?true:false;
                user.setSin_login(keep_login);
                user.setOp_confirmacion(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.DIALOGTYPE)));
                user.setOp_lector(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.INPUTTYPE)));
                user.setOp_buttons(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.BUTTON_TYPE)));
                user.setIp(cursor.getString(cursor.getColumnIndex(DataBaseHelper.DEVICEIP)));
                user.setToken(cursor.getString(cursor.getColumnIndex(DataBaseHelper.TOKEN)));
                user.setIs_root(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ISROOT)));
                //countUsers=cursor.getCount();
            }
        }finally {
            cursor.close();
        }

        return user;
    }


}
