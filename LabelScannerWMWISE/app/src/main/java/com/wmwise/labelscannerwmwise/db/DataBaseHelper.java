package com.wmwise.labelscannerwmwise.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String TBL_USER ="adm_user";
    public static final String ID_USER = "id";
    public static final String USERNAME = "username";
    public static final String FULLNAME = "name";
    public static final String DATE = "login_date";
    public static final String INPUTTYPE = "op_input";
    public static final String DIALOGTYPE = "op_dialog";
    public static final String KEEPSESSION = "keep_session";
    public static final String DEVICEIP = "ip";
    public static final String STATUS = "estado";
    public static final String CURRENTUSER = "active_user";
    public static final String BUTTON_TYPE = "op_button";
    public static final String TOKEN = "token";
    public static final String ISROOT = "is_root";
    public static final String SAVED_EVENT = "saved";

    public static final String DATABASE_NAME = "wmwise_lite";
    public static final int DATABASE_VERSION  =4;

    private static final String CREATE_USER = "CREATE TABLE "+TBL_USER+"("+ID_USER+" INTEGER, "+
                                              USERNAME+" VARCHAR(50), "+FULLNAME+" VARCHAR(50), "+
                                              DATE+" TEXT, "+INPUTTYPE+" INTEGER, "+
                                              DIALOGTYPE+" INTEGER, "+BUTTON_TYPE+" INTEGER, "+
                                              KEEPSESSION+" BOOLEAN, "+DEVICEIP+"  TEXT, "+
                                              STATUS+" INTEGER, "+CURRENTUSER+" BOOLEAN, "+
                                              TOKEN+" TEXT, "+ISROOT+" INTEGER DEFAULT 0, "+
                                              SAVED_EVENT+" INTEGER DEFAULT 0);";

    public static final String LOADING_GUIDE_ID ="id";
    public static final String TYPE="type";
    public static final String LG_CODE = "code";
    public static final String COMPLETE = "complete";
    public static final String TOTAL_PCS = "total_pcs";
    public static final String TBL_LOADING_GUIDE = "loading_guide";

    private static final String CREATE_LOADING_GUIDE ="CREATE TABLE "+TBL_LOADING_GUIDE+" ( "+LOADING_GUIDE_ID+" INTEGER, "+
                                                            TYPE+" VARCHAR(3), "+LG_CODE+" VARCHAR(30), "+COMPLETE+" INTEGER,"+
                                                            TOTAL_PCS+" INTEGER DEFAULT 0);";

    public static final String TBL_ITEM_WAREHOUSE_DETAIL = "oa_pick_load_warehouse_item_detail";
    public static final String DETAIL_RECEIPT_ID = "receipt_id";
    public static final String DET_PCS_LOADED = "pcs_loaded";
    public static final String DET_PCS_PICKED = "pcs_picked";
    public static final String DATEPICK = "date_pick";
    public static final String DATELOAD = "date_load";
    public static final String WAREHOUSE_FULL_CODE ="warehouse_full_code";

    private static final String CRATE_ITEM_WAREHOUSE_DETAIL = "CREATE TABLE "+TBL_ITEM_WAREHOUSE_DETAIL+"("+DETAIL_RECEIPT_ID+" INTEGER, "+
                                                               WAREHOUSE_FULL_CODE+" VARCHAR(50), "+
                                                               DET_PCS_PICKED+" INTEGER DEFAULT 0, "+
                                                               DET_PCS_LOADED+" INTEGER DEFAULT 0, "+
                                                               DATEPICK+" VARCHAR(50), "+DATELOAD+" VARCHAR(50));";

    public static final String TBL_ITEM_WAREHOUSE = "oa_pick_load_warehouse_item";
    public static final String LOADING_GUIDE_ITEM_ID = "lg_id";
    public static final String RECEIPT_ID = "receipt_id";
    public static final String DIMS = "dims";
    public static final String LINE = "line";
    public static final String PCS = "pcs";
    public static final String PCS_LOADED = "pcs_loaded";
    public static final String PCS_PICKED = "pcs_picked";
    public static final String PICKED = "picked";
    public static final String LOADED = "loaded";
    public static final String WAREHOUSE_CODE ="warehouse_code";

    private static final String CRATE_ITEM_WAREHOUSE = "CREATE TABLE "+TBL_ITEM_WAREHOUSE+"("+RECEIPT_ID+" INTEGER, "+LOADING_GUIDE_ITEM_ID+" INTEGER, "+
                                                        WAREHOUSE_CODE+" VARCHAR(50), "+LINE+" INTEGER, "+
                                                        PCS_PICKED+" INTEGER DEFAULT 0, "+PCS_LOADED+" INTEGER DEFAULT 0, "+
                                                        PCS+" INTEGER DEFAULT 0, "+DIMS+" VARCHAR(20), "+
                                                        PICKED+" BOOLEAN, "+LOADED+" BOOLEAN);";


    public static final String TBL_URL_PARAMS = "url_base_configuration";
    public static final String APP_URL = "app_url";
    public static final String URL_BASE = "url";
    public static final String APP_NAME = "name";
    public static final String STATUS_URL = "status";
    public static final String ID_URL = "id";

    private static final String CREATE_URL_PARAMS = "CREATE TABLE "+TBL_URL_PARAMS+"( "+ID_URL+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                                                                        URL_BASE+" TEXT,"+STATUS_URL+" INTEGER, "+
                                                                                        APP_NAME+" VARCHAR(30),"+APP_URL+" VARCHAR(50));";

    public static final String TBL_CARRIER ="carrier";
    public static final String ID_CARRIER="id";
    public static final String CODE_CARRIER="code";
    public static final String CARRIER_NAME="name";
    public static final String CARRIER_ADDRESS="address";
    public static final String CARRIER_CITY="city";
    public static final String CARRIER_TYPE ="type";
    public static final String CARRIER_STATUS = "status";

    private static final String CREATE_CARRIER = "CREATE TABLE "+TBL_CARRIER+" ("+ID_CARRIER+" INTEGER,"+CODE_CARRIER+" VARCHAR(50),"+
                                                 CARRIER_NAME+" VARCHAR(50),"+CARRIER_ADDRESS+" VARCHAR(50),"+CARRIER_CITY+" VARCHAR(20),"+
                                                 CARRIER_TYPE+" INTEGER,"+CARRIER_STATUS+" INTEGER);";

    public static final String TBL_TRACKING = "tracking";
    public static final String ID_TRACKING = "id";
    public static final String ID_CARRIER_TRACKING = "carrier_id";
    public static final String CARRIER_NAME_TRACKING ="carrier_name";
    public static final String CODE_TRACKING = "code";
    public static final String STATUS_TRACKING = "status";

    private static final String CREATE_TRACKING = "CREATE TABLE "+TBL_TRACKING+" ("+ID_TRACKING+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                                                                    ID_CARRIER_TRACKING+" INTEGER,"+
                                                                                    CARRIER_NAME_TRACKING+" VARCHAR(50),"+
                                                                                    CODE_TRACKING+" TEXT,"+
                                                                                    STATUS_TRACKING+" INTEGER);";

    public static final String TBL_WR_LOCATION ="warehouses_location";
    public static final String ID_WR_LOCATION ="id";
    public static final String WR_LOCATION_CODE="wr_code";
    public static final String WR_BIN_LOCATION="location_bin";
    public static final String WR_LOCATION_USER_ID ="user_id";
    public static final String STATUS_WR_LOCATION="status";

    private static final String CREATE_WR_LOCATION ="CREATE TABLE "+TBL_WR_LOCATION+" ("+ID_WR_LOCATION+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                                                                         WR_LOCATION_CODE+" VARCHAR(50), "+
                                                                                         WR_BIN_LOCATION+" VARCHAR(50),"+
                                                                                         WR_LOCATION_USER_ID+" INTEGER,"+
                                                                                         STATUS_WR_LOCATION+" INTEGER);";

    public static final String TBL_WR_TRANSACTION ="warehouse_transaction";
    public static final String ID_WR_TRANSACTION ="id";
    public static final String WR_TRANSACTION_CODE ="wr_code";
    public static final String WR_TRANSACTION_TYPE ="transaction_type";

    private static final String CREATE_WR_TRANSACTION ="CREATE TABLE "+TBL_WR_TRANSACTION+" ("+ID_WR_TRANSACTION+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                                        WR_TRANSACTION_CODE+" VARCHAR(50), "+WR_TRANSACTION_TYPE+" INTEGER);";


    public DataBaseHelper(Context c ){
        super(c,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_URL_PARAMS);
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_CARRIER);
        db.execSQL(CREATE_TRACKING);
        db.execSQL(CREATE_WR_TRANSACTION);
        db.execSQL(CREATE_WR_LOCATION);
        db.execSQL(CREATE_LOADING_GUIDE);
        db.execSQL(CRATE_ITEM_WAREHOUSE);
        db.execSQL(CRATE_ITEM_WAREHOUSE_DETAIL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TBL_URL_PARAMS);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_USER);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_CARRIER);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_TRACKING);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_WR_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_WR_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_LOADING_GUIDE);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_ITEM_WAREHOUSE);
        db.execSQL("DROP TABLE IF EXISTS "+TBL_ITEM_WAREHOUSE_DETAIL);
        onCreate(db);
    }
}
