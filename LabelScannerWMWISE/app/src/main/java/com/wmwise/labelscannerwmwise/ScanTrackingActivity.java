package com.wmwise.labelscannerwmwise;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.zxing.client.android.Intents;
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
import com.wmwise.labelscannerwmwise.Models.Carrier;
import com.wmwise.labelscannerwmwise.Models.Tracking;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.adapters.ItemTrackingAdapter;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.CarrierDataSource;
import com.wmwise.labelscannerwmwise.db.TrackingDataSource;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.dialogs.SimpleConfirmDialog;
import com.wmwise.labelscannerwmwise.obj.Params;


import static android.Manifest.permission.CAMERA;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;


public class ScanTrackingActivity extends AppCompatActivity implements View.OnClickListener,ToughpadApiListener,BarcodeListener{

    private static ScanTrackingActivity instance;
    private boolean isLoaded =false;
    public static ScanTrackingActivity getInstance(){
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


    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;

    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(ScanTrackingActivity.this);
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
                params[0].addBarcodeListener(ScanTrackingActivity.this);
                response = true;
            } catch (BarcodeException ex) {
                Toast.makeText(ScanTrackingActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("BarcodeException",ex.getMessage().toString());
                //return false;
            } catch (TimeoutException ex) {
                Toast.makeText(ScanTrackingActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("TimeoutException",ex.getMessage().toString());
                //return false;
            } catch(Exception ex){
                Toast.makeText(ScanTrackingActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                Log.d("Exception",ex.getMessage().toString());
                //return false;
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();

            if (result) {
                Toast.makeText(ScanTrackingActivity.this, selectedReader.getDeviceName(), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onApiDisconnected() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToughpadApi.destroy();
        instance = null;
        isLoaded = false;
    }


    @Override
    public void onRead(BarcodeReader barcodeReader,final BarcodeData result) {
        runOnUiThread(new Runnable() {
            public void run() {
                onTriggerScanner(result.getTextData());
            }
        });
    }


    private Button btn_back;
    private TextView tv_title,tv_texto_scan,tv_total;
    private ListView lv_tracking;
    private CardView cv_send,cv_scan,cv_clear;
    private AutoCompleteTextView autocomplete_carrier;

    private User user;
    private char scanMethod = 'L';//L(laser),C(camera),B(BlueTooth)
    private char BtnMethod = 'H';//H(hardware),S(software),R(remove)
    private RequestQueue request;
    private ProgressDialog dialogo;

    private ArrayList<String> listaTracking;
    private ItemTrackingAdapter adapterTracking;
    private Carrier postCarrier;

    private LinearLayout layout_input_bluetooth;
    private EditText et_barcode;
    private Button btn_add;


    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_tracking);
        user = loadUser(AppConn.getInstance().getApplicationContext());
        try{
            ToughpadApi.initialize(this,this);
        }catch (Exception ex){
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            setToughpadHardwareException(user);
        }

        initializeInputs();
        setListeners();

        dialogo = ProgressDialog.show(ScanTrackingActivity.this, "Getting Carriers...", "Please Wait...");
        request = ConnectionLinkWebService.getInstance().getQueue();
        getCarriesList(request, Params.appURL()+"carriers");

        preloadCarrier(AppConn.getInstance().getApplicationContext());
        preloadTracking(AppConn.getInstance().getApplicationContext());

        inputKeyListenerActions();

    }


    private void inputKeyListenerActions(){
        if(et_barcode.getVisibility()==View.VISIBLE){
            et_barcode.setOnKeyListener(new View.OnKeyListener() {
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


    private void preloadTracking(Context c){
        TrackingDataSource tds = new TrackingDataSource(c);
        ArrayList<Tracking> trackings;
        try{
            tds.open();
            trackings=tds.trackingList();
            int totalTracking=trackings.size();
            if(totalTracking>0){
                postCarrier = new Carrier();
                for(int g=0;g<totalTracking;g++){
                    listaTracking.add(trackings.get(g).getCode());
                    postCarrier.setName(trackings.get(g).getCarrier_name());
                    postCarrier.setId(trackings.get(g).getId_carrier());
                }
                autocomplete_carrier.setText(postCarrier.getName());
                adapterTracking.notifyDataSetChanged();
                tv_total.setText(count_message+totalTracking);
            }

        }catch(SQLException ex){
                Toast.makeText(ScanTrackingActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
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



    ArrayList<Carrier> listaCarrier;
    private void getCarriesList(RequestQueue request, String url){
        StringRequest SR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                listaCarrier = new ArrayList<>();
                JSONArray jsonCarriers = null;
                try {
                    jsonCarriers = new JSONArray(response);
                    for(int idx=0;idx<jsonCarriers.length();idx++){
                        JSONObject jsonObj = jsonCarriers.getJSONObject(idx);
                        Carrier carrier = new Carrier();
                        carrier.setId(jsonObj.getInt("id"));
                        carrier.setCode(jsonObj.getString("code"));
                        String name =  jsonObj.getString("name");
                        carrier.setName(name);
                        carrier.setCity(jsonObj.getString("city"));
                        carrier.setType(jsonObj.getInt("type"));

                        listaCarrier.add(carrier);//revisar
                        /*salvado en DB en caso de excepcion*/
                    }
                    bindingCarrieresAutoComplete(listaCarrier);
                    dialogo.dismiss();
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        basicAlert.make();
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                        //handleError(e1);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Info","Session error, close the app",R.drawable.ic_cloud_off_black_24dp);
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
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
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


    private void setToughpadHardwareException(User user){
        if( user.getOp_lector()==1 ){
            user.setOp_lector(0);
            user.setOp_buttons(1);
            exceptionUserSetting(AppConn.getInstance().getApplicationContext(),user);
            scanMethod = (user.getOp_lector()==0)?'C':((user.getOp_lector()==1)?'L':'B');
            BtnMethod = (user.getOp_buttons()==0)?'H':((user.getOp_buttons()==1)?'S':'R');
            //Toast.makeText(AppConn.getInstance().getApplicationContext(), "No Barcode Hardware detected", Toast.LENGTH_SHORT).show();
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

    private String count_message = "Total trackings scanned ";
    private void initializeInputs(){
        int elemVisible =View.GONE;
        if(scanMethod=='B'){
            elemVisible = View.VISIBLE;
        }
        layout_input_bluetooth = (LinearLayout)findViewById(R.id.layout_input_bluetooth);
        et_barcode = (EditText)findViewById(R.id.et_barcode);
        btn_add = (Button)findViewById(R.id.btn_add);
        btn_add.setVisibility(elemVisible);
        et_barcode.setVisibility(elemVisible);
        layout_input_bluetooth.setVisibility(elemVisible);
        autocomplete_carrier = (AutoCompleteTextView)findViewById(R.id.autocomplete_carrier);
        //autocomplete_carrier.setFocusable(false);
        cv_send = (CardView)findViewById(R.id.cv_send);
        cv_scan = (CardView)findViewById(R.id.cv_scan);
        cv_clear = (CardView)findViewById(R.id.cv_clear);
        tv_total = (TextView)findViewById(R.id.tv_total);
        tv_total.setText(count_message+"0");
        tv_texto_scan = (TextView)findViewById(R.id.tv_texto_scan);
        lv_tracking = (ListView)findViewById(R.id.lv_tracking);
        btn_back = (Button)findViewById(R.id.btn_back);
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("Tracking");
        listaTracking = new ArrayList<String>();
        adapterTracking = new ItemTrackingAdapter(ScanTrackingActivity.this,listaTracking);
        lv_tracking.setAdapter(adapterTracking);
        bluetoothInstances();
    }

    private void bindingCarrieresAutoComplete(final ArrayList<Carrier> listado){
        ArrayAdapter<Carrier> adaptadorAutocomplete =  new ArrayAdapter<Carrier>(ScanTrackingActivity.this,
                                                                                       android.R.layout.simple_list_item_1,
                                                                                       listado);
        autocomplete_carrier.setAdapter(adaptadorAutocomplete);
        autocomplete_carrier.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                postCarrier = (Carrier)parent.getItemAtPosition(position);
                saveSafeCarrier(ScanTrackingActivity.this,postCarrier);
            }
        });
    }


    private void saveSafeCarrier(Context c,Carrier carrier){
        CarrierDataSource cds = new CarrierDataSource(c);
        try{
            cds.open();
            cds.saveCarrier(carrier);
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    private int actionButtonIcon(){
        if(bluetoothAdapter.isEnabled())
            return R.drawable.ic_keyboard_black_24dp;
        else
            return R.drawable.ic_bluetooth_connected_black_24dp;
    }

    private void setListeners(){
        btn_back.setOnClickListener(this);
        cv_scan.setOnClickListener(this);
        cv_send.setOnClickListener(this);
        cv_clear.setOnClickListener(this);
        btn_add.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if(v==btn_back){
            BackToMenu();
        }else if(v==cv_send){
            sendCarrierWithTrakings();
        }else if(v==cv_scan){
            scanTracking();
        }else if(v==btn_add){
            addToList();
        }else if(v==cv_clear){
            SimpleConfirmDialog confirm =  new SimpleConfirmDialog(ScanTrackingActivity.this,"Info","Do you like clear all data from the interface?",R.drawable.ic_modal);
            confirm.setPositive("Yes",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearAll();
                }
            });
            confirm.setNegative("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            confirm.make();
        }
    }


    private void clearAll(){
        deleteTrackingCarrier(AppConn.getInstance().getApplicationContext(),postCarrier.getId());
        listaTracking.clear();
        adapterTracking.notifyDataSetChanged();
        et_barcode.setText("");
        tv_total.setText(count_message+listaTracking.size());
        autocomplete_carrier.setText("");
        autocomplete_carrier.requestFocus();
    }

    private boolean deleteTrackingCarrier(Context c,int id){
        CarrierDataSource ds = new CarrierDataSource(c);
        TrackingDataSource tds = new TrackingDataSource(c);
        try{
            tds.open();
            ds.open();
            if(tds.destroyAllTracking(id))//delete the collection
               return ds.destroyCarrier(id);//delete the master
            return false;
        }catch(SQLException ex){
            return false;
        }
    }

    private void addToList(){
        if(et_barcode.getText().length()>0&&autocomplete_carrier.getText().length()>0){
            fixInput();
            onTriggerScanner(et_barcode.getText().toString());
        }
        et_barcode.setText("");
        et_barcode.requestFocus();
    }


    private void fixInput(){
        if(et_barcode.getVisibility()==View.VISIBLE){
            if(et_barcode.getText().toString().length()>0)
                et_barcode.setText(Params.textFilterFixer(et_barcode.getText().toString()));
        }
    }


    private void preloadCarrier(Context c){
        CarrierDataSource cds = new CarrierDataSource(c);
        try{
            cds.open();
            postCarrier=cds.getCarrier();
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void saveTracking(Context c,String warehouse){
        TrackingDataSource tds = new TrackingDataSource(c);
        try{
            tds.open();
            Tracking t=new Tracking();
            t.setStatus(1);
            t.setId_carrier(postCarrier.getId());
            t.setCarrier_name(postCarrier.getName());
            t.setCode(warehouse);
            if(!tds.save(t)){
                tds.destroy();
            }
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    private void onTriggerScanner(String result){
        if(!result.equals("")){
            if(Params.elemFromListExist(listaTracking,result)){
                Params.displayDialog(ScanTrackingActivity.this,"Warning","The tracking number already exist in the list",R.drawable.ic_modal);
            }else{
                if(scanMethod=='L'||scanMethod=='C'){
                    autocomplete_carrier.setFocusable(false);
                    autocomplete_carrier.setCursorVisible(false);
                    autocomplete_carrier.setKeyListener(null);
                }

                listaTracking.add(result);
                adapterTracking.notifyDataSetChanged();
                tv_total.setText(count_message+lv_tracking.getCount());
                saveTracking(ScanTrackingActivity.this,result);
            }
        }

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

        }


        if (selectedReader.isExternal()) {
            try {
                int charge = selectedReader.getBatteryCharge();
                if (charge != -1) {
                    // Battery Info Available
                    /*
                    txtDeviceInfo.setText(getString(R.string.lbl_is_charging,
                            selectedReader.isBatteryCharging() ?
                                    getString(R.string.lbl_is_charging_yes) :
                                    getString(R.string.lbl_is_charging_no)));
                                    */
                }
            } catch (BarcodeException ex) {
                //handleError(ex);
                //txtDeviceInfo.setText(ex.getMessage().toString());
            }
        }
    }


    private void sendCarrierWithTrakings(){
        if(validate()){
            SimpleConfirmDialog confirm =  new SimpleConfirmDialog(ScanTrackingActivity.this,"Info","Do you want send the information?",R.drawable.ic_modal);
            confirm.setPositive("Yes",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String url = Params.appURL()+"massive_tracking";
                    dialogo = ProgressDialog.show(ScanTrackingActivity.this, "Sending...", "Please Wait...");
                    requestPOST_Tracking_codes(request, url);
                }
            });
            confirm.setNegative("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            confirm.make();
        }else{
            BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Warning","First, yo must enter a Carrier and scan a Tracking",R.drawable.ic_warning_black_24dp);
            basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            basicAlert.make();
        }

    }

    private boolean validate(){
        return autocomplete_carrier.getText().length()>0 && lv_tracking.getCount()>0;
    }


    private boolean deleteCarrier(Context c){
        CarrierDataSource cds = new CarrierDataSource(c);
        try{
            cds.open();
            return cds.destroy();
        }catch(SQLException ex){
            return false;
        }
    }

    private boolean deleteTracking(Context c){
        TrackingDataSource tds = new TrackingDataSource(c);
        try{
            tds.open();
            return tds.destroy();
        }catch(SQLException ex){
            return false;
        }
    }


    private void requestPOST_Tracking_codes(RequestQueue request, String url){
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
                        deleteCarrier(ScanTrackingActivity.this);
                        deleteTracking(ScanTrackingActivity.this);
                    }
                    else
                        title ="WARNING";
                    dialogo.dismiss();
                    final BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,title,message,icon);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            autocomplete_carrier.setText("");
                            lv_tracking.setAdapter(null);
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(ScanTrackingActivity.this,"Info","Wifi turned off",R.drawable.ic_cloud_off_black_24dp);
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
                String tracking = "";
                for(int i=0;i<listaTracking.size();i++){
                    String flag = (i==(listaTracking.size()-1))?"":",";
                    tracking +=listaTracking.get(i)+flag;
                }
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("user",String.valueOf(user.getId()));
                params.put("tracking_numbers",tracking.toString());
                params.put("carrier_id",String.valueOf(postCarrier.getId()));
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

    private void scanTracking(){
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
        }else{
            if(autocomplete_carrier.getText().length()==0)
               bluetoothAdapter.disable();
        }
        tv_texto_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);
        /*
        if(et_barcode.getVisibility()==View.VISIBLE){
            et_barcode.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        onTriggerScanner(et_barcode.getText().toString());
                        et_barcode.setText("");
                        et_barcode.requestFocus();
                    }
                    return false;
                }
            });

        }
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
            Toast.makeText(ScanTrackingActivity.this,ex.getMessage().toString(),Toast.LENGTH_LONG).show();
            //txtDeviceInfo.setText(ex.getMessage().toString());
        }
    }


    private void BackToMenu(){
        Intent i = new Intent(ScanTrackingActivity.this,MenuActivity.class);
        startActivity(i);
        finish();
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
                Toast.makeText(this,"Cancelled",Toast.LENGTH_LONG).show();
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


    @Override
    protected void onPause(){
        super.onPause();
    }


}
