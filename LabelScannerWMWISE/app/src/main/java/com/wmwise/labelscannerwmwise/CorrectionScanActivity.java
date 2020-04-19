package com.wmwise.labelscannerwmwise;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.wmwise.labelscannerwmwise.Models.SpinnerData;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.Models.Warehouse;
import com.wmwise.labelscannerwmwise.adapters.ItemSpinnerAdapter;
import com.wmwise.labelscannerwmwise.adapters.ItemWarehouseCodeAdapter;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.db.WarehouseTransactionDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.dialogs.SimpleConfirmDialog;
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

public class CorrectionScanActivity extends AppCompatActivity implements View.OnClickListener,ToughpadApiListener,BarcodeListener {

    private static CorrectionScanActivity instance;
    private boolean isLoaded =false;

    public static CorrectionScanActivity getInstance(){
        return instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void updateButtonState(Intent buttonIntent) {
        if (buttonIntent.getAction().equals(AppButtonManager.ACTION_APPBUTTON)) {
            int buttonId = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_BUTTON, 0);
            boolean down = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_STATE, 0) == AppButtonManager.EXTRA_APPBUTTON_STATE_DOWN;
            if(BtnMethod=='H')
                updateButtonState(buttonId, down);
        }
    }

    private void updateButtonState(final int buttonId, final boolean newState) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                int newBackground;
                /*
                if (newState) {
                    newBackground = R.drawable.btn_pressed;
                } else {
                    newBackground = R.drawable.btn_released;
                }
                */

                switch (buttonId) {
                    case AppButtonManager.BUTTON_A1:
                        laserMethod();
                        break;
                    case AppButtonManager.BUTTON_A2:
                        laserMethod();
                        break;
                    case AppButtonManager.BUTTON_A3:
                        laserMethod();
                        break;
                    case AppButtonManager.BUTTON_USER:
                        laserMethod();
                        break;
                    case AppButtonManager.BUTTON_SIDE:
                        laserMethod();
                        break;
                }
            }
        });
    }


    private Button btn_back,btn_add;
    private CardView cv_scan, cv_clean, cv_send;
    private TextView tv_title,tv_texto_scan;
    private EditText et_warehouse;
    private Spinner spn_type;
    private RequestQueue request;
    private ArrayList<SpinnerData> items;
    private ProgressDialog dialogo;
    private Context context;
    private int ScanOp =0;
    public User user;

    private char scanMethod = 'L';//L(laser),C(camera)
    private char BtnMethod = 'H';//H(hardware),S(software),R(remove)

    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;

    private ListView lv_warehouses;
    private ArrayList<Warehouse> warehouse_codes_list;
    private ItemWarehouseCodeAdapter adaptador;

    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(CorrectionScanActivity.this);
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
                params[0].addBarcodeListener(CorrectionScanActivity.this);
                response = true;
            } catch (BarcodeException ex) {
                Toast.makeText(CorrectionScanActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("BarcodeException",ex.getMessage().toString());
                //return false;
            } catch (TimeoutException ex) {
                Toast.makeText(CorrectionScanActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("TimeoutException",ex.getMessage().toString());
                //return false;
            } catch(Exception ex){
                Toast.makeText(CorrectionScanActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("Exception",ex.getMessage().toString());
                //return false;
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();

            if (result) {
                Toast.makeText(CorrectionScanActivity.this, selectedReader.getDeviceName(), Toast.LENGTH_SHORT).show();
                onBarcodeEnabled();
            }
        }
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

    @Override
    public void onRead(BarcodeReader barcodeReader, final BarcodeData result) {
        runOnUiThread(new Runnable() {
            public void run() {

                /*
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanBarCodeActivity.this);
                String formatted = String.format(getString(R.string.dlg_bcr_scanned),
                        result.getSymbology(), result.getEncoding(), result.getTextData());
                builder.setMessage(formatted);
                builder.setTitle(R.string.title_bcr_scanned);
                builder.setCancelable(true);
                builder.show();
                */

                onTriggerScanner(result.getTextData());
            }
        });
    }


    private void setReverseCodesList(Warehouse w){
        ArrayList<Warehouse> temp_list =new ArrayList<>();
        int size = (warehouse_codes_list.size()-1);
        for(int i=0;i<size;i++){
            temp_list.add(warehouse_codes_list.get(i));
        }
        for(int i=0;i<size;i++){
            warehouse_codes_list.set((i+1),temp_list.get(i));

        }
        warehouse_codes_list.set(0,w);
    }


    private boolean onTriggerScanner(String result){

        if(result.split("-").length==3){
            ScanOp=1;
            et_warehouse.setText(result);
            if (warehouseFromListExist(warehouse_codes_list, result)) {
                Params.displayDialog(CorrectionScanActivity.this, "Warning", "The code is already in the list", R.drawable.ic_modal);
                return false;
            } else {
                Warehouse w = new Warehouse();
                w.setCode(result);
                warehouse_codes_list.add(w);
                setReverseCodesList(w);
                adaptador.notifyDataSetChanged();
                saveWarehouseTransaction(CorrectionScanActivity.this,result);
            }
            return true;
        }
        else
            Toast.makeText(CorrectionScanActivity.this, "Label Invalid", Toast.LENGTH_SHORT).show();
        return false;
    }


    private boolean warehouseFromListExist(ArrayList<Warehouse> list,String compare){
        for(int i=0;i<list.size();i++){
            if(list.get(i).getCode().equals(compare)){
                return true;
            }
        }
        return false;
    }


    private void onBarcodeEnabled() {

        String deviceType = getString(R.string.bcr_type_unknown);
        switch (selectedReader.getBarcodeType()) {
            case BarcodeReader.BARCODE_TYPE_CAMERA:
                deviceType = getString(R.string.bcr_type_cam); break;
            case BarcodeReader.BARCODE_TYPE_ONE_DIMENSIONAL:
                deviceType = getString(R.string.bcr_type_1d); break;
            case BarcodeReader.BARCODE_TYPE_TWO_DIMENSIONAL:
                deviceType = getString(R.string.bcr_type_2d); break;
        }

        // Fill in device info
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.lbl_firmware_version, selectedReader.getDeviceFirmwareVersion())).append("\n");
            sb.append(getString(R.string.lbl_serial_number, selectedReader.getDeviceSerialNumber())).append("\n");
            sb.append(getString(R.string.lbl_external_device, getString(selectedReader.isExternal() ? R.string.lbl_is_charging_yes : R.string.lbl_is_charging_no))).append("\n");
            sb.append(getString(R.string.lbl_bcr_type, deviceType));
        } catch (BarcodeException ex) {
            // handleError(ex);
            Toast.makeText(CorrectionScanActivity.this,ex.getMessage().toString(),Toast.LENGTH_LONG).show();
        }


        if (selectedReader.isExternal()) {
            try {
                int charge = selectedReader.getBatteryCharge();
                if (charge != -1) {
                    // Battery Info Available
                }
            } catch (BarcodeException ex) {
                //handleError(ex);
                //txtDeviceInfo.setText(ex.getMessage().toString());
            }
        }
    }



    SpinnerData itemSelected;
    private BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correction_scan);
        context = AppConn.getInstance().getApplicationContext();
        selectedReader = null;
        readers = null;

        user = loadUser(context);

        try{
            ToughpadApi.initialize(this,this);
        }catch (Exception ex){
            setToughpadHardwareException(user);
        }

        initializeInputs();
        setListeners();

        if(scanMethod=='C'||scanMethod=='L'){
            et_warehouse.setKeyListener(null);
        }



        request = ConnectionLinkWebService.getInstance().getQueue();

        preloadWarehouses(AppConn.getInstance().getApplicationContext());

        inputKeyListenerActions();

    }

    private void preloadWarehouses(Context c){
        WarehouseTransactionDataSource ds = new WarehouseTransactionDataSource(c);
        ArrayList<Warehouse> listWarehouses;
        try{
            ds.open();
            listWarehouses=ds.warehouseList();
            int total_locations=listWarehouses.size();
            if(total_locations>0){
                for(int g=0;g<total_locations;g++){
                    Warehouse w=new Warehouse();
                    spn_type.setSelection(Integer.parseInt(String.valueOf(listWarehouses.get(g).getOp())));
                    w.setCode(listWarehouses.get(g).getCode());
                    warehouse_codes_list.add(w);
                }
                adaptador.notifyDataSetChanged();
            }

        }catch(SQLException ex){
            Toast.makeText(CorrectionScanActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }


    }

    private void inputKeyListenerActions(){
        if(et_warehouse.getVisibility()==View.VISIBLE){
            et_warehouse.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER||event.getKeyCode()==KeyEvent.KEYCODE_TAB||event.getKeyCode()==KeyEvent.ACTION_DOWN) {
                        addToList();
                        return true;
                    }
                    return false;
                }
            });
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToughpadApi.destroy();
        instance = null;
        isLoaded = false;
    }


    private void postingWarehouse(RequestQueue request, String url){
        StringRequest SR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                JSONObject jsonObjectParam= null;
                try {
                    jsonObjectParam = new JSONObject(response);
                    dialogo.dismiss();
                    boolean success = jsonObjectParam.getBoolean("success");
                    String message =jsonObjectParam.getString("message");
                    String title = "";
                    //txtDeviceInfo.setText(jsonObjectParam.getString("message"));

                    if(success){
                        title ="SUCCESS";
                        deleteWarehouseTransaction(CorrectionScanActivity.this);

                    }
                    else
                        title ="WARNING";

                    BasicAlertDialog basicAlert = new BasicAlertDialog(CorrectionScanActivity.this,title,message,R.drawable.ic_modal);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CleanInputs();
                        }
                    });
                    basicAlert.make();

                } catch (JSONException e1) {
                    e1.printStackTrace();
                    //handleError(e1);
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                dialogo.dismiss();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    dialogo.dismiss();
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        //JSONObject obj = new JSONObject(res);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(CorrectionScanActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CleanInputs();
                            }
                        });
                        basicAlert.make();
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                        //handleError(e1);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(CorrectionScanActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CleanInputs();
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(CorrectionScanActivity.this,"Info","Session error, close the app",R.drawable.ic_cloud_off_black_24dp);
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
                user =loadUser(AppConn.getInstance().getApplicationContext());
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer "+user.getToken());

                return params;
            }
            @Override
            protected Map<String, String> getParams(){
                String labels = "";
                for(int i=0;i<warehouse_codes_list.size();i++){
                    String flag = (i==(warehouse_codes_list.size()-1))?"":",";
                    labels +=warehouse_codes_list.get(i).getCode()+flag;
                }
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("labels", labels.toString());
                params.put("type", itemSelected.getKeys());
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


    private void initializeInputs(){
        et_warehouse=(EditText)findViewById(R.id.et_warehouse);

        spn_type = (Spinner)findViewById(R.id.spn_type);

        btn_back = (Button)findViewById(R.id.btn_back);
        cv_send = (CardView)findViewById(R.id.cv_send);
        cv_clean = (CardView)findViewById(R.id.cv_clean);
        cv_scan = (CardView)findViewById(R.id.cv_scan);

        btn_add = (Button)findViewById(R.id.btn_add);
        tv_texto_scan = (TextView)findViewById(R.id.tv_texto_scan);
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("Warehouse Correction");

        lv_warehouses=(ListView)findViewById(R.id.lv_warehouses);
        warehouse_codes_list = new ArrayList<Warehouse>();
        adaptador=new ItemWarehouseCodeAdapter(AppConn.getInstance().getApplicationContext(),warehouse_codes_list);
        lv_warehouses.setAdapter(adaptador);

        items = new ArrayList<SpinnerData>();//list of types

        items.add(new SpinnerData("","SELECT AN ITEM",R.drawable.ic_home_black_24dp));
        items.add(new SpinnerData("P","PICKED",R.drawable.ic_pick_black_24dp));
        //items.add(new SpinnerData("L","LOADED",R.drawable.ic_load_black_24dp));
        items.add(new SpinnerData("D","DELIVERED",R.drawable.ic_delivery_black_24dp));

        SpinnerAdapter spinnerAdapter = new ItemSpinnerAdapter(AppConn.getInstance().getApplicationContext(),items);
        spn_type.setAdapter(spinnerAdapter);
        spn_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSelected=items.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        bluetoothInstances();
    }


    private void setListeners(){
        btn_back.setOnClickListener( this);
        cv_clean.setOnClickListener( this);
        cv_scan.setOnClickListener( this);
        cv_send.setOnClickListener( this);
        btn_add.setOnClickListener( this);
        if(BtnMethod=='H')
            cv_scan.setVisibility(View.GONE);
        else{
            cv_scan.setVisibility(View.VISIBLE);
            if(BtnMethod=='R'){
                tv_texto_scan.setText("Enable");
                tv_texto_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);
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


    //buttons click in activity
    public void onClick(View v) {
        if (v == cv_clean) {
            CleanInputs();
        }else if(v == btn_back){
            BackToMenu();
        }else if(v == cv_scan){
            ScanWarehouse();
        }else if(v == cv_send){
            SendDataBarcode();
        }else if(v==btn_add){
            addToList();
        }
    }


    private void addToList(){
        if(!itemSelected.keySelected().equals("")&&et_warehouse.getText().length()>0){
            et_warehouse.setText(Params.textFilterFixer(et_warehouse.getText().toString()));
            onTriggerScanner(et_warehouse.getText().toString());
        }
        et_warehouse.setText("");
        et_warehouse.requestFocus();
    }

    private void saveWarehouseTransaction(Context c,String warehouse){
        WarehouseTransactionDataSource ds = new WarehouseTransactionDataSource(c);
        try{
            ds.open();
            Warehouse w=new Warehouse();
            w.setOp(String.valueOf(spn_type.getSelectedItemPosition()).charAt(0));
            w.setCode(warehouse);
            if(!ds.save(w)){
                ds.destroy(warehouse);
            }
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private boolean deleteWarehouseTransaction(Context c){
        WarehouseTransactionDataSource ds = new WarehouseTransactionDataSource(c);
        try{
            ds.open();
            return ds.destroyWarehousesCollection();
        }catch(SQLException ex){
            return false;
        }
    }

    private void SendDataBarcode(){
        if(user.getOp_confirmacion()==0){
            //enviar por WS los lectores
                SimpleConfirmDialog dialog = new SimpleConfirmDialog(this,"Info", "Do you want send the current information?",R.drawable.ic_modal);
            dialog.setPositive("Yes",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postData();
                }
            });
            dialog.setNegative("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //no enviar
                    dialog.dismiss();
                }
            });
            dialog.make();
        }else{
            postData();
        }

    }

    private void postData(){
        if(isCompleteToSend()){
            dialogo = ProgressDialog.show(CorrectionScanActivity.this, "Sending Data...", "Please Wait...");
            String url = Params.appURL()+"individual_assign";
            postingWarehouse(request, url);
        }else{
            String error = "";
            if(itemSelected.getKeys().equals("") && et_warehouse.getText().length()>0)
                error = "Please, select a type";
            else if (!itemSelected.getKeys().equals("") && et_warehouse.getText().length()==0)
                error = "Please, Scan a Warehouse to Send";
            else if(itemSelected.getKeys().equals("") && et_warehouse.getText().length()>0)
                error = "Please, select a type and scan a warehouse to send";
            Params.displayDialog(CorrectionScanActivity.this,"Warning",error,R.drawable.logo);
        }
    }


    private boolean isCompleteToSend(){
        return (et_warehouse.getText().length()>0 && !itemSelected.getKeys().equals("") );
    }


    private void CleanInputs(){
        deleteWarehouseTransaction(AppConn.getInstance().getApplicationContext());
        warehouse_codes_list.clear();
        adaptador.notifyDataSetChanged();
        et_warehouse.setText("");
        et_warehouse.requestFocus();
        spn_type.setSelection(0);
        ScanOp=0;
    }

    private void BackToMenu(){
        Intent i = new Intent(CorrectionScanActivity.this,MenuActivity.class);
        startActivity(i);
        finish();
    }

    private void ScanWarehouse(){
        if(scanMethod=='C'){
            cameraMethod();
        }else if(scanMethod=='L'){
            laserMethod();
        }else if(scanMethod=='B'){
            bluetoothDeviceMethod();
        }
    }


    private void bluetoothDeviceMethod(){

        if(!bluetoothAdapter.isEnabled()){
            Intent intent =  new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        tv_texto_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);
/*
        et_warehouse.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    onTriggerScanner(et_warehouse.getText().toString());
                }
                return false;
            }
        });
*/
    }


    private void laserMethod(){
        if (!selectedReader.isEnabled()) {
            EnableReaderTask task = new EnableReaderTask();
            task.execute(selectedReader);
        }
        try {
            selectedReader.pressSoftwareTrigger(true);
        } catch (Exception ex) {
            Toast.makeText(CorrectionScanActivity.this,ex.getMessage().toString(),Toast.LENGTH_LONG).show();
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
                Toast.makeText(this,"Cancelled",Toast.LENGTH_LONG).show();;
            }
            else
            {
                if(scanMethod=='C')
                    onTriggerScanner(result.getContents());
            }
        }
        else
        {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

}
