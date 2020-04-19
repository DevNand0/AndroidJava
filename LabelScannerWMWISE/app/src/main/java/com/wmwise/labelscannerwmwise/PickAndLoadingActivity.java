package com.wmwise.labelscannerwmwise;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.appbtn.AppButtonManager;
import com.panasonic.toughpad.android.api.barcode.BarcodeData;
import com.panasonic.toughpad.android.api.barcode.BarcodeException;
import com.panasonic.toughpad.android.api.barcode.BarcodeListener;
import com.panasonic.toughpad.android.api.barcode.BarcodeReader;
import com.panasonic.toughpad.android.api.barcode.BarcodeReaderManager;
import com.wmwise.labelscannerwmwise.DeviceServices.RefreshTokenService;
import com.wmwise.labelscannerwmwise.Models.Item;
import com.wmwise.labelscannerwmwise.Models.LoadingGuide;
import com.wmwise.labelscannerwmwise.Models.SpinnerData;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.Models.Warehouse;
import com.wmwise.labelscannerwmwise.adapters.ItemSpinnerAdapter;
import com.wmwise.labelscannerwmwise.adapters.ItemSpinnerTextBoldAdapter;
import com.wmwise.labelscannerwmwise.adapters.ItemWarehouseDetailsAdapter;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.ItemWarehouseDataSource;
import com.wmwise.labelscannerwmwise.db.LoadingGuideDataSource;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.obj.Params;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static android.Manifest.permission.CAMERA;

public class PickAndLoadingActivity extends AppCompatActivity implements View.OnClickListener, BarcodeListener, ToughpadApiListener {

    private static PickAndLoadingActivity instance;
    private boolean isLoaded =false;

    public static PickAndLoadingActivity getInstance(){
        return instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }


    public void updateButtonState(Intent buttonIntent) {
        if (buttonIntent.getAction().equals(AppButtonManager.ACTION_APPBUTTON)) {
            int buttonId = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_BUTTON, 0);
            boolean down = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_STATE, 0) == AppButtonManager.EXTRA_APPBUTTON_STATE_DOWN;
            if(BtnMethod == 'H')
                updateButtonState(buttonId, down);
        }
    }

    private void updateButtonState(final int buttonId, final boolean newState) {
        this.runOnUiThread(new Runnable() {
            public void run() {

                switch (buttonId) {
                    case AppButtonManager.BUTTON_A1:
                        scan();
                        break;
                    case AppButtonManager.BUTTON_A2:
                        scan();
                        break;
                    case AppButtonManager.BUTTON_A3:
                        scan();
                        break;
                    case AppButtonManager.BUTTON_USER:
                        scan();
                        break;
                    case AppButtonManager.BUTTON_SIDE:
                        scan();
                        break;
                }
            }
        });
    }


    public static EditText et_loading_guide;
    private TextView tv_title;
    private ListView lv_warehouse;
    private Button btn_scan,btn_load,btn_wr_modal,btn_back;
    private Spinner spn_type;
    private char scanMethod = 'L';//L(laser),C(camera)
    private char BtnMethod = 'H';//H(hardware),S(software)
    private ProgressDialog dialogo;
    private RequestQueue request;
    private ArrayList<SpinnerData> items;
    public User user;
    public LoadingGuideDataSource lg_ds;

    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;


    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(PickAndLoadingActivity.this);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setMessage("Scanning");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(BarcodeReader... params) {
            boolean response = false;
            try {

                params[0].enable(10000);
                params[0].addBarcodeListener(PickAndLoadingActivity.this);
                response = true;
            } catch (BarcodeException ex) {
                //Toast.makeText(ScanBarCodeActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("BarcodeException",ex.getMessage().toString());
                //handleError(ex);
            } catch (TimeoutException ex) {
                //Toast.makeText(ScanBarCodeActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("TimeoutException",ex.getMessage().toString());
                //handleError(ex);
            } catch(Exception ex){
                //Toast.makeText(ScanBarCodeActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("Exception",ex.getMessage().toString());
                //handleError(ex);
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();

            if (result) {
                //Toast.makeText(PickAndLoadingActivity.this, selectedReader.getDeviceName(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    SpinnerData itemSelected;
    private BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_and_loading);
        user = loadUser(AppConn.getInstance().getApplicationContext());
        selectedReader = null;
        readers = null;

        try{
            ToughpadApi.initialize(this,this);
        }catch (Exception ex){
            setToughpadHardwareException(user);
        }

        initializeInputs();

        request = ConnectionLinkWebService.getInstance().getQueue();

        if(scanMethod=='C'||scanMethod=='L')
            et_loading_guide.setKeyListener(null);
        setListeners();
    }

    private static final int REQUEST_ENABLE_BT=0;
    String bluetoothMessage = "";
    private void bluetoothInstances(){

        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Params.displayDialog(this,"Warning","The bluetooth is not available",R.drawable.ic_modal);
            bluetoothMessage ="The bluetooth is not available";
        }else{
            bluetoothMessage ="Bluetooth Available";
        }
    }


    private void setToughpadHardwareException(User user){
        if( user.getOp_lector()==1 ){
            user.setOp_lector(0);
            user.setOp_buttons(1);
            exceptionUserSetting(AppConn.getInstance().getApplicationContext(),user);
            scanMethod = (user.getOp_lector()==0)?'C':((user.getOp_lector()==1)?'L':'B');
            BtnMethod = (user.getOp_buttons()==0)?'H':((user.getOp_buttons()==1)?'S':'R');
            Toast.makeText(AppConn.getInstance().getApplicationContext(), "No Barcode Hardware detected", Toast.LENGTH_SHORT).show();
        }
    }


    private void exceptionUserSetting(Context c,User user){
        UserDataSource uds = new UserDataSource(c);
        try{
            uds.open();
            uds.actualizar(user);
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    private void initializeInputs(){
        et_loading_guide = (EditText)findViewById(R.id.et_loading_guide);
        lv_warehouse = (ListView)findViewById(R.id.lv_warehouse);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_load= (Button) findViewById(R.id.btn_load);
        btn_wr_modal = (Button) findViewById(R.id.btn_wr_modal);
        btn_back = (Button)findViewById(R.id.btn_back);
        spn_type = (Spinner) findViewById(R.id.spn_type);
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("Loading Guide");

        items = new ArrayList<SpinnerData>();//list of types

        items.add(new SpinnerData("","SELECT AN ITEM",R.drawable.ic_home_black_24dp));
        items.add(new SpinnerData("A","AIR",R.drawable.ic_local_airport_black_24dp));
        items.add(new SpinnerData("O","OCEAN",R.drawable.ic_directions_boat_black_24dp));
        items.add(new SpinnerData("C","COURIER",R.drawable.ic_courier_black_24dp));


        SpinnerAdapter spinnerAdapter = new ItemSpinnerAdapter(AppConn.getInstance().getApplicationContext(),items);
        spn_type.setAdapter(spinnerAdapter);
        spn_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSelected=items.get(position);
                lv_warehouse.setAdapter(null);
                et_loading_guide.setText("");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        bluetoothInstances();
    }

    private void setListeners(){
        btn_scan.setOnClickListener(this);
        btn_load.setOnClickListener(this);
        btn_wr_modal.setOnClickListener(this);
        btn_back.setOnClickListener(this);

        if(BtnMethod=='H')
            btn_scan.setVisibility(View.GONE);
        else{
            btn_scan.setVisibility(View.VISIBLE);
            if(BtnMethod=='R'){
                btn_scan.setText("Enable");
                btn_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);
            }
        }

    }

    private int actionButtonIcon(){
        if(bluetoothAdapter.isEnabled())
            return R.drawable.ic_keyboard_black_24dp;
        else
            return R.drawable.ic_bluetooth_connected_black_24dp;
    }


    private User loadUser(Context c){
        User user = null;
        UserDataSource ds = new UserDataSource(c);
        try{
            ds.open();
            user = ds.getActiveUser();
            scanMethod = (user.getOp_lector()==0)?'C':((user.getOp_lector()==1)?'L':'B');
            BtnMethod = (user.getOp_buttons()==0)?'H':((user.getOp_buttons()==1)?'S':'R');
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(c,"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            user = null;
        }
        return user;
    }

    private LoadingGuide getLoadingGuide(Context c, String loadingGuideCode){
        LoadingGuide loadingGuide = null;
        LoadingGuideDataSource lg_ds = new LoadingGuideDataSource(c);
        try{
            lg_ds.open();
            loadingGuide=lg_ds.getLoadingGuide(loadingGuideCode);
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return loadingGuide;
    }

    private void saveLoadingGuide(LoadingGuide lg,Context c){
        LoadingGuideDataSource lg_ds = new LoadingGuideDataSource(c);
        try{
            lg_ds.open();
            if(!lg_ds.saveLoadingGuide(lg)){
                BasicAlertDialog alerta = new BasicAlertDialog(PickAndLoadingActivity.this,"Error","Close the app process..",R.drawable.ic_warning_black_24dp);
                alerta.OkButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(PickAndLoadingActivity.this,SplashActivity.class));
                        finish();
                        dialog.dismiss();
                    }
                });
                alerta.make();
            }
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void onClick(View v) {
        if(v==btn_scan){
            scan();
        }else if(v==btn_load){
            load();
        }else if(v==btn_wr_modal){
            openModal();
        }else if(v==btn_back){
            backToMenu();
        }
    }

    private void scan(){

        if(itemSelected.keySelected()==""){
            Toast.makeText(AppConn.getInstance().getApplicationContext()," Plase, Select a Type of Loading Guide",Toast.LENGTH_LONG).show();
        }else {
            if(scanMethod=='C'){
                cameraMethod();
            }else if(scanMethod=='L'){
                laserMethod();
            }else if(scanMethod=='B'){
                bluetoothDeviceMethod();
            }
        }
    }


    private void bluetoothDeviceMethod(){

        if(!bluetoothAdapter.isEnabled()){
            Intent intent =  new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        btn_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);

    }

    private void load(){
        if (!et_loading_guide.getText().toString().equals("")) {
            et_loading_guide.setText(Params.textFilterFixer(et_loading_guide.getText().toString()));
            LoadingGuide loadingGuide = getLoadingGuide(AppConn.getInstance().getApplicationContext(), et_loading_guide.getText().toString());
            if(loadingGuide!=null && loadingGuide.getComplete()==2){
                BasicAlertDialog alerta = new BasicAlertDialog(PickAndLoadingActivity.this,"Info","This Loading Guide has been loaded!!",R.drawable.ic_modal);
                alerta.OkButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alerta.make();
            }else{
                String url = Params.appURL()+"loading_guide";
                lv_warehouse.setAdapter(null);
                dialogo = ProgressDialog.show(PickAndLoadingActivity.this, "Loading Warehouses...", "Please Wait...");
                requestPOST_Type_LoadingGuide(request, url);
            }
        }else{
            BasicAlertDialog alerta = new BasicAlertDialog(PickAndLoadingActivity.this,"Info","Please, Scan a loading guide to list",R.drawable.ic_modal);
            alerta.OkButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alerta.make();
        }

    }

    private void openModal(){
        if(!et_loading_guide.getText().toString().equals("")&&lv_warehouse.getCount()>0){
            setCustomDialog();
        }else{
            if(et_loading_guide.getText().toString().equals(""))
                Toast.makeText(PickAndLoadingActivity.this, "Plase, Scan a Loading Guide First", Toast.LENGTH_SHORT).show();
            else if(lv_warehouse.getCount()==0)
                Toast.makeText(PickAndLoadingActivity.this, "Plase, Load the Warehouses from this Loading Guide", Toast.LENGTH_SHORT).show();
            else if(et_loading_guide.getText().toString().equals("")&&lv_warehouse.getCount()==0)
                Toast.makeText(PickAndLoadingActivity.this, "Plase, Load the Warehouses from this Loading Guide", Toast.LENGTH_SHORT).show();
        }
    }

    private void backToMenu(){
        Intent i = new Intent(PickAndLoadingActivity.this,MenuActivity.class);
        startActivity(i);
        finish();
    }

    private void POST_pick_load(final RequestQueue request, String url, final Warehouse warehouse){
        StringRequest SR =  new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObjectParam= null;
                try {
                    jsonObjectParam = new JSONObject(response);
                    boolean success = jsonObjectParam.getBoolean("success");
                    String message =jsonObjectParam.getString("message");
                    String title = "";
                    int icon = R.drawable.ic_warning_black_24dp;
                    if(success){
                        title = "SUCCESS";
                        icon = R.drawable.ic_modal;
                        saveDialogTrigger(et_warehouse_code.getText().toString(),spn_option.getSelectedItemPosition(),warehouse);
                    }
                    else
                        title ="WARNING";

                    final BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,title,message,icon);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //reload listview
                            String url_to_load = Params.appURL()+"loading_guide";
                            requestPOST_Type_LoadingGuide(request, url_to_load);
                        }
                    });
                    basicAlert.make();

                } catch (JSONException e1) {
                    e1.printStackTrace();
                    //handleError(e1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialogo.dismiss();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {

                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        //JSONObject obj = new JSONObject(res);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        basicAlert.make();
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                        //handleError(e1);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,"Info","Wifi turned off",R.drawable.ic_cloud_off_black_24dp);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    basicAlert.make();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                user = loadUser(AppConn.getInstance().getApplicationContext());
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer "+user.getToken());
                return params;
            }

            @Override
            protected Map<String, String> getParams(){
                String warehouse_code[]= null;
                String piece="",line="",code="";

                if(!et_warehouse_code.getText().toString().equals("")){
                    warehouse_code=et_warehouse_code.getText().toString().split("-");
                    code = warehouse_code[0].trim();
                    line = warehouse_code[1].trim();
                    piece = warehouse_code[2].trim();
                }
                int loadingGuide_id =getLoadingGuide(AppConn.getInstance().getApplicationContext(),et_loading_guide.getText().toString()).getId();
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("type",itemSelected.keySelected());
                params.put("lg_id",String.valueOf(loadingGuide_id));
                params.put("code",code);
                params.put("line",line);
                params.put("piece",piece);
                params.put("user",String.valueOf(user.getId()));

                return params;
            }
        };
        int socketTimeout = 15000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        SR.setRetryPolicy(policy);
        request.add(SR);
    }

    private int total_elementos=0;
    private int total_loaded = 0;
    ArrayList<Warehouse> warehouseArrayList =null;
    private void requestPOST_Type_LoadingGuide(RequestQueue request, String url){
        total_elementos=0;
        total_loaded=0;
        StringRequest SR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                JSONObject jsonHeadParam= null;
                JSONObject jsonObjectParam = null;
                try {
                    jsonHeadParam = new JSONObject(response);

                    LoadingGuide lg = getLoadingGuide(PickAndLoadingActivity.this,et_loading_guide.getText().toString());
                    int complete=0;
                    int loading_guide_id = jsonHeadParam.getInt("id");
                    if(lg==null){
                        lg = new LoadingGuide();
                        lg.setId(loading_guide_id);
                        lg.setCode(et_loading_guide.getText().toString());
                        lg.setType(itemSelected.keySelected());
                    }else{
                        complete = 1;
                    }

                    JSONArray collection = jsonHeadParam.getJSONArray("receipt_entries");
                    warehouseArrayList = new ArrayList<>();
                    Warehouse w=null;
                    for(int i=0;i<collection.length();i++){
                        w = new Warehouse();
                        jsonObjectParam = collection.getJSONObject(i);
                        w.setLoading_guide_id(jsonHeadParam.getInt("id"));
                        w.setConsignee_name(jsonObjectParam.getString("consignee_name"));
                        w.setConsignee_id(jsonObjectParam.getInt("consignee_id"));
                        w.setShipper_name(jsonObjectParam.getString("shipper_name"));
                        w.setShipper_id(jsonObjectParam.getInt("shipper_id"));
                        String code = jsonObjectParam.getString("code");
                        w.setCode(code);
                        w.setReceipt_id(jsonObjectParam.getInt("id"));

                        if(jsonObjectParam.has("items")){
                            JSONArray nodeArray = jsonObjectParam.getJSONArray("items");
                            int elementos = nodeArray.length();
                            Item item=null;
                            w.items.clear();
                            JSONObject itemJSON = null;
                            for(int j=0;j<elementos;j++){
                                item = new Item();
                                itemJSON = nodeArray.getJSONObject(j);
                                item.setId(j);
                                item.setReceipt_id(itemJSON.getInt("receipt_entry_id"));
                                item.setLg_id(loading_guide_id);
                                item.setCargo_type_id(itemJSON.getInt("cargo_type_id"));
                                item.setCargo_type_code(itemJSON.getString("cargo_type_code"));
                                int pcs = itemJSON.getInt("pcs");
                                total_elementos+=pcs;
                                item.setPcs(pcs);
                                int line = itemJSON.getInt("line");
                                item.setLine(line);
                                String line_toString= Params.numberToString(line);
                                String wr_basecode=code+"-"+line_toString;
                                Item i_tmp = getDetailsPickedLoad(wr_basecode);

                                JSONObject dims = itemJSON.getJSONObject("dims");
                                item.setHeight(dims.getString("height"));
                                item.setLength(dims.getString("length"));
                                item.setWidth(dims.getString("width"));
                                int pcs_picked = (i_tmp.getPcs_picked()>0)?i_tmp.getPcs_picked():itemJSON.getInt("pcs_picked");
                                int pcs_loaded = (i_tmp.getPcs_loaded()>0)?i_tmp.getPcs_loaded():itemJSON.getInt("pcs_loaded");
                                total_loaded+=pcs_loaded;
                                item.setPcs_picked(pcs_picked);
                                item.setPcs_loaded(pcs_loaded);
                                w.items.add(item);
                            }

                        }
                        warehouseArrayList.add(w);
                    }
                    complete = (total_loaded==total_elementos)?2:complete;
                    lg.setComplete(complete);
                    lg.setTotal_pcs(total_elementos);
                    saveLoadingGuide(lg,AppConn.getInstance().getApplicationContext());
                    fillListView(warehouseArrayList);
                    dialogo.dismiss();
                } catch (JSONException e1) {
                    dialogo.dismiss();
                    try {
                        jsonHeadParam = new JSONObject(response);
                        String message =jsonHeadParam.getString("message");
                        String title = "";

                        if(jsonHeadParam.getBoolean("success"))
                            title ="SUCCESS";
                        else
                            title ="ERROR";

                        BasicAlertDialog alerta = new BasicAlertDialog(PickAndLoadingActivity.this,title,message,R.drawable.ic_warning_black_24dp);
                        alerta.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alerta.make();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AppConn.getInstance().getApplicationContext(), "Error al conectar "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    e1.printStackTrace();

                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                dialogo.dismiss();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        //JSONObject obj = new JSONObject(res);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //et_loading_guide.setText("");
                            }
                        });
                        basicAlert.make();
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                        //handleError(e1);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //et_loading_guide.setText("");
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,"Info",error.getMessage(),R.drawable.ic_cloud_off_black_24dp);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    basicAlert.make();
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                user = loadUser(AppConn.getInstance().getApplicationContext());
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer "+user.getToken());

                return params;
            }
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("type",itemSelected.keySelected());
                params.put("code",et_loading_guide.getText().toString());
                return params;
            }
        };
        int socketTimeout = 15000;//10 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        SR.setRetryPolicy(policy);
        request.add(SR);
    }


    private void fillListView(final ArrayList<Warehouse> lista){
        ItemWarehouseDetailsAdapter adaptador = new ItemWarehouseDetailsAdapter(AppConn.getInstance().getApplicationContext(),lista);
        lv_warehouse.setAdapter(adaptador);
        lv_warehouse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Warehouse w=lista.get(position);
                //Toast.makeText(AppConn.getInstance().getApplicationContext(),w.getConsignee_name()+"|"+position,Toast.LENGTH_LONG).show();
            }
        });
    }


    private Item getDetailsPickedLoad(String wr_sub_code){
        Item i = null;
        ItemWarehouseDataSource DS = new ItemWarehouseDataSource(AppConn.getInstance().getApplicationContext());
        try{
            DS.open();
            i = DS.getDetailsPickedLoad(wr_sub_code);
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return i;
    }


    private void laserMethod(){
        if (!selectedReader.isEnabled()) {
            EnableReaderTask task = new EnableReaderTask();
            task.execute(selectedReader);
        }
        try {
            selectedReader.pressSoftwareTrigger(true);
        } catch (Exception ex) {
            Toast.makeText(AppConn.getInstance().getApplicationContext(),ex.getMessage().toString(),Toast.LENGTH_LONG).show();
        }
    }

    private static final int REQUEST_CAMERA = 1;
    private void cameraMethod(){
        IntentIntegrator in = new IntentIntegrator(this);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();

            } else {
                requestPermission();
            }
        }
        in.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        in.setPrompt("scan");
        in.setCameraId(0);
        in.setBeepEnabled(false);
        in.initiateScan();
    }


    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                Toast.makeText(getApplicationContext(), "You need to allow access to both the permissions", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null)
        {
            if(result.getContents()==null)
            {
                Log.d("ScanBarCodeActivity","Cancelled scan");
                Toast.makeText(this,"Cancelled",Toast.LENGTH_LONG).show();;
            }
            else
            {
                if(dialogActivated){
                    if(et_loading_guide.getText().length()>0)
                        et_warehouse_code.setText(result.getContents());
                    dialogActivated = !dialogActivated;
                }else{
                    et_loading_guide.setText(result.getContents());
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }
    private Dialog dialog_scanWR;
    private Button btn_scan_wr, btn_set_wr, btn_close_wr;
    private Spinner spn_option;
    public static EditText et_warehouse_code;
    public static boolean dialogActivated = false;
    public void setCustomDialog(){
        dialog_scanWR = new Dialog(PickAndLoadingActivity.this);
        dialog_scanWR.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_scanWR.setContentView(R.layout.scandialog);
        dialog_scanWR.setTitle("Scan a warehouse");
        dialog_scanWR.setCancelable(false);
        dialog_scanWR.setCanceledOnTouchOutside(false);

        btn_scan_wr = (Button)dialog_scanWR.findViewById(R.id.btn_scan_wr);
        btn_set_wr = (Button)dialog_scanWR.findViewById(R.id.btn_set_wr);
        btn_close_wr = (Button)dialog_scanWR.findViewById(R.id.btn_close_wr);
        et_warehouse_code = (EditText)dialog_scanWR.findViewById(R.id.et_warehouse_code);
        spn_option = (Spinner) dialog_scanWR.findViewById(R.id.spn_option);

        btn_scan_wr.setEnabled(true);
        btn_set_wr.setEnabled(true);
        btn_close_wr.setEnabled(true);

        if(scanMethod=='C'||scanMethod=='L'){
            et_warehouse_code.setKeyListener(null);
        }

        String[] arraySpinner = new String[] {
                "PICK", "LOAD"
        };
        ItemSpinnerTextBoldAdapter istba = new ItemSpinnerTextBoldAdapter(PickAndLoadingActivity.this,arraySpinner);
        spn_option.setAdapter(istba);

        dialogActivated=true;




        if(BtnMethod=='H'){
            btn_scan_wr.setVisibility(View.GONE);
        }
        else{

            if(BtnMethod=='R')
                btn_scan_wr.setText("Enable");

            btn_scan_wr.setVisibility(View.VISIBLE);
            btn_scan_wr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(scanMethod=='C'){
                        cameraMethod();
                    }else if(scanMethod=='L'){
                        laserMethod();
                    }else if(scanMethod=='B'){
                        bluetoothDeviceMethod();
                    }
                }
            });
        }
        btn_set_wr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_warehouse_code.setText(Params.textFilterFixer(et_warehouse_code.getText().toString()));
                int total=warehouseArrayList.size();
                int notFound =0;
                for(int c=0;c<total;c++){
                    String wr[] = et_warehouse_code.getText().toString().split("-");
                    if(wr[0].equals(warehouseArrayList.get(c).getCode())){
                        Warehouse requestWarehouse = warehouseArrayList.get(c);
                        c=total;
                        lv_warehouse.setAdapter(null);
                        dialogo = ProgressDialog.show(PickAndLoadingActivity.this, "Loading Warehouses...", "Please Wait...");
                        //STORE TO REMOTE DB
                        String route = (spn_option.getSelectedItemPosition()==0)?"picked":"loaded";
                        String url = Params.appURL()+route;
                        POST_pick_load(request,url,requestWarehouse);
                        dialog_scanWR.cancel();
                    }else{
                        notFound++;
                    }
                }
                if(notFound==total){
                    Toast.makeText(AppConn.getInstance().getApplicationContext(),"Warehouse Not Belongs to Loading Guide: "+et_loading_guide.getText().toString(),Toast.LENGTH_LONG).show();
                }
                dialogActivated=false;

            }
        });

        btn_close_wr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogActivated=false;
                dialog_scanWR.cancel();
            }
        });
        dialog_scanWR.show();
    }


    private boolean saveDialogTrigger(String warehouse_full_code,int indexSelected,Warehouse warehouse){
        boolean saved = false;
        ItemWarehouseDataSource wrDS = new ItemWarehouseDataSource(AppConn.getInstance().getApplicationContext());
        try{
            wrDS.open();
            String wr[]=warehouse_full_code.split("-");
            int posItem = Integer.parseInt(wr[1])-1;
            Item i = warehouse.items.get(posItem);
            String title;
            String message;
            Item pivot_i = wrDS.getItem(wr[0],Integer.parseInt(wr[1]));
            if(i.isPicked()&&i.isLoaded()){
                title = "Warning";
                message = "This label is loaded and sent";
                displayDialog(title,message,R.drawable.ic_warning_black_24dp);
                return true;
            }else{
                int save_code =0;
                boolean picked = (indexSelected==0);

                int pcs_picked = i.getPcs_picked();
                int pcs_loaded = i.getPcs_loaded();
                if( picked && i.getPcs_picked() < pivot_i.getPcs() ){
                    pcs_picked++;
                    i.setPcs_picked(pcs_picked);
                    save_code =saveItem(i,wrDS,warehouse_full_code,picked);
                }else if( !picked && (i.getPcs_picked() > pivot_i.getPcs_loaded()) && (i.getPcs_loaded() < pivot_i.getPcs()) ){
                    pcs_loaded++;
                    i.setPcs_loaded(pcs_loaded);
                    save_code =saveItem(i,wrDS,warehouse_full_code,picked);
                }else if( picked && (pivot_i.getPcs()==0) ){
                    pcs_picked++;
                    i.setPcs_picked(pcs_picked);
                    save_code =saveItem(i,wrDS,warehouse_full_code,picked);
                }
                if(save_code==0||save_code==1||save_code==2||save_code==3){
                    switch(save_code){
                        case 1 :
                            title ="Warning";
                            message ="This label, has already pick"; break;
                        case 2 :
                            title ="Warning";
                            message ="for load this label, first must do pick in this label"; break;
                        case 3:
                            title ="Warning";
                            message ="This label, has already load"; break;
                        default :
                            title="Error";
                            message="Error to save";
                            break;
                    }

                    displayDialog(title,message,R.drawable.ic_error_outline_black_24dp);
                    saved=false;
                }else if(save_code==4||save_code==5){
                    saved=true;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return saved;
    }


    private int saveItem(Item i,ItemWarehouseDataSource wrDS,String warehouse_full_code, boolean picked){
        int save_code = wrDS.saveDataDetail(i,warehouse_full_code,picked);
        if(save_code==4||save_code==5){
           wrDS.SaveData(i,warehouse_full_code,picked);
        }
        return save_code;
    }

    private void displayDialog(String title, String message, int ico){
        BasicAlertDialog basicAlert = new BasicAlertDialog(PickAndLoadingActivity.this,title,message,ico);
        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        basicAlert.make();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ToughpadApi.destroy();
        instance = null;
        isLoaded = false;
    }

    @Override
    public void onRead(BarcodeReader barcodeReader, final BarcodeData result) {

        runOnUiThread(new Runnable() {
            public void run() {
                if(dialogActivated){
                    if(et_loading_guide.getText().length()>0)
                        et_warehouse_code.setText(result.getTextData());
                }else{
                    et_loading_guide.setText(result.getTextData());
                }
            }
        });

    }

    @Override
    public void onApiConnected(int i) {
        readers = BarcodeReaderManager.getBarcodeReaders();
        List<String> readerNames = new ArrayList<String>();
        for (BarcodeReader reader : readers) {
            readerNames.add(reader.getDeviceName());
        }
        selectedReader = readers.get(0);

        //--------------------------------
        instance = this;
        isLoaded = true;
        try {
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_A1)) {
                //txtA1.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_A2)) {
                //txtA2.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_A3)) {
                //txtA3.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_USER)) {
                //txtUser.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_SIDE)) {
                //txtSide.setVisibility(View.GONE);
            }
            // Check if we have control
            if (!AppButtonManager.hasButtonControl()) {
                // We do not have control, request it.
                Intent reconfigureApp = new Intent(Intent.ACTION_MAIN);
                reconfigureApp.addCategory(Intent.CATEGORY_LAUNCHER);
                reconfigureApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reconfigureApp.setComponent(new ComponentName("com.panasonic.toughpad.android.service",
                        "com.panasonic.toughpad.android.appbuttondelegator.ConfigActivity"));
                startActivity(reconfigureApp);
            }
        } catch (final Exception ex) {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setMessage(ex.getMessage());
                    builder.setTitle("API Error");
                    builder.show();
                }
            });
        }
    }

    @Override
    public void onApiDisconnected() {

    }
}
