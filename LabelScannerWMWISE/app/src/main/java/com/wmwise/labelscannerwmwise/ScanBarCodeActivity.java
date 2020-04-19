package com.wmwise.labelscannerwmwise;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.dialogs.SimpleConfirmDialog;
import com.wmwise.labelscannerwmwise.obj.Params;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static android.Manifest.permission.CAMERA;

public class ScanBarCodeActivity extends FragmentActivity implements View.OnClickListener, ToughpadApiListener, BarcodeListener{


    private static ScanBarCodeActivity instance;
    private boolean isLoaded =false;

    public static ScanBarCodeActivity getInstance(){
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

    private char scanMethod = 'L';//L(laser),C(camera)
    private char BtnMethod = 'H';
    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;

    //implementacion del lector de codigo de barras
    private EditText et_warehouse, et_location_bin;
    private CheckBox cb_reassignment;
    private Button btn_back;
    private CardView cv_scan,cv_send, cv_clean;
    private RequestQueue request;
    private ProgressDialog dialogo;
    private Context context;
    private TextView txtDeviceInfo,tv_title,tv_texto_scan;
    private int ScanOp =0;


    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(ScanBarCodeActivity.this);
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
                params[0].addBarcodeListener(ScanBarCodeActivity.this);
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
                Toast.makeText(ScanBarCodeActivity.this, selectedReader.getDeviceName(), Toast.LENGTH_SHORT).show();
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
                onTriggerScanner(result.getTextData());
            }
        });
    }

    private void onTriggerScanner(String result){
        String codes[];
        codes = result.split("-");
        if(ScanOp==0){
            if(result.split("-").length==3){
                ScanOp=1;
                et_warehouse.setText(result);
            }
            else{
                et_warehouse.setText("");
                Toast.makeText(ScanBarCodeActivity.this, "Label Invalid", Toast.LENGTH_SHORT).show();
            }
        }else if(ScanOp==1){
            codes = result.split("-");
            if(result.split("-").length==2){
                ScanOp=0;
                et_location_bin.setText(result);
            }
            else{
                et_location_bin.setText("");
                Toast.makeText(ScanBarCodeActivity.this, "Label Invalid", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private void onBarcodeEnabled() {

        txtDeviceInfo.setEnabled(true);
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
            txtDeviceInfo.setText(sb.toString());
        } catch (BarcodeException ex) {
            //handleError(ex);
            //txtDeviceInfo.setText(ex.getMessage().toString());
        }


        if (selectedReader.isExternal()) {
            try {
                int charge = selectedReader.getBatteryCharge();
                if (charge != -1) {
                    // Battery Info Available
                    txtDeviceInfo.setText(getString(R.string.lbl_is_charging,
                            selectedReader.isBatteryCharging() ?
                                    getString(R.string.lbl_is_charging_yes) :
                                    getString(R.string.lbl_is_charging_no)));
                }
            } catch (BarcodeException ex) {
                //handleError(ex);
                //txtDeviceInfo.setText(ex.getMessage().toString());
            }
        }
    }


    Intent mServiceIntent;
    private RefreshTokenService tokenService;
    public User user;
    private BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bar_code);
        selectedReader = null;
        readers = null;

        user =loadUser(AppConn.getInstance().getApplicationContext());

        try{
            ToughpadApi.initialize(this,this);
        }catch (Exception ex){
            Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            setToughpadHardwareException(user);
        }

        initializeInputs();
        setListeners();

        if(scanMethod=='C'||scanMethod=='L'){
           et_warehouse.setKeyListener(null);
           et_location_bin.setKeyListener(null);
        }

        request = ConnectionLinkWebService.getInstance().getQueue();

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

    private void initializeInputs(){
        cb_reassignment = (CheckBox)findViewById(R.id.cb_reassignment);

        et_warehouse=(EditText)findViewById(R.id.et_warehouse);
        et_location_bin=(EditText)findViewById(R.id.et_location_bin);

        btn_back = (Button)findViewById(R.id.btn_back);

        cv_send = (CardView)findViewById(R.id.cv_send);
        cv_clean = (CardView)findViewById(R.id.cv_clean);
        cv_scan = (CardView)findViewById(R.id.cv_scan);

        txtDeviceInfo = (TextView)findViewById(R.id.txtDeviceInfo);
        tv_texto_scan = (TextView)findViewById(R.id.tv_texto_scan);
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("Scan a Warehouse");

        bluetoothInstances();
    }


    private void setListeners(){
        cv_clean.setOnClickListener( this);
        btn_back.setOnClickListener( this);
        cv_scan.setOnClickListener( this);
        cv_send.setOnClickListener( this);
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

                    if(success)
                        title ="SUCCESS";
                    else
                        title ="WARNING";

                    BasicAlertDialog basicAlert = new BasicAlertDialog(ScanBarCodeActivity.this,title,message,R.drawable.ic_modal);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CleanInputs();
                        }
                    });
                    basicAlert.make();

                } catch (JSONException e1) {
                    e1.printStackTrace();
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
                    BasicAlertDialog basicAlert = new BasicAlertDialog(ScanBarCodeActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
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
                    BasicAlertDialog basicAlert = new BasicAlertDialog(ScanBarCodeActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CleanInputs();
                        }
                    });
                    basicAlert.make();
                }
            }else{
                BasicAlertDialog basicAlert = new BasicAlertDialog(ScanBarCodeActivity.this,"Info","Session error, close the app",R.drawable.ic_cloud_off_black_24dp);
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
            String [] warehouse_params = et_warehouse.getText().toString().split("-");
            String [] location_bin_params = et_location_bin.getText().toString().split("-");
            Map<String, String> params = new HashMap<>();
            params.put("Content-Type", "application/json; charset=UTF-8");
            params.put("code", warehouse_params[0]);
            params.put("line", warehouse_params[1]);
            params.put("piece", warehouse_params[2]);
            params.put("location", location_bin_params[0]);
            params.put("bin", location_bin_params[1]);
            params.put("user",String.valueOf(user.getId()));
            if(cb_reassignment.isChecked()){
                params.put("reassignment",String.valueOf(true));
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToughpadApi.destroy();
        instance = null;
        isLoaded = false;
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
        }
    }

    private void SendDataBarcode(){

        if(isCompleteToSend()){
            if(user.getOp_confirmacion()==0){
                //enviar por WS los lectores
                SimpleConfirmDialog dialog = new SimpleConfirmDialog(this,"Send Warehouse", "Do you want, send the warehouse and line?",R.drawable.ic_modal);
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
        }else{
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"Plase, Scan a Warehouse to Send",Toast.LENGTH_LONG).show();
        }
    }

    private void postData(){
        if(isCompleteToSend()){
            fixInput();
            dialogo = ProgressDialog.show(ScanBarCodeActivity.this, "Sending Data...", "Please Wait...");
            String url = Params.appURL()+"assign";
            postingWarehouse(request, url);
        }else{
            Toast.makeText(ScanBarCodeActivity.this,"Plase, Scan a Warehouse to Send",Toast.LENGTH_LONG).show();
        }
    }

    private void fixInput(){
        et_warehouse.setText(Params.textFilterFixer(et_warehouse.getText().toString()));
        et_location_bin.setText(Params.textFilterFixer(et_location_bin.getText().toString()));
    }

    private boolean isCompleteToSend(){

        return (et_warehouse.getText().length()>0 && et_location_bin.getText().length()>0);
    }

    private void CleanInputs(){
        et_warehouse.setText("");
        et_location_bin.setText("");
        cb_reassignment.setChecked(false);
        ScanOp=0;
    }

    private void BackToMenu(){
        Intent i = new Intent(ScanBarCodeActivity.this,MenuActivity.class);
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

        et_warehouse.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_ENTER||keyCode == KeyEvent.KEYCODE_TAB|| keyCode == KeyEvent.ACTION_DOWN){
                        onTriggerScanner(et_warehouse.getText().toString());
                        et_location_bin.requestFocus();
                    }
                    return false;
                }
        });

        et_location_bin.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER||keyCode == KeyEvent.KEYCODE_TAB){
                    onTriggerScanner(et_location_bin.getText().toString());
                }
                return false;
            }
        });


    }


    private void laserMethod(){
        if (!selectedReader.isEnabled()) {
            EnableReaderTask task = new EnableReaderTask();
            task.execute(selectedReader);
        }
        try {
            selectedReader.pressSoftwareTrigger(true);
        } catch (Exception ex) {
            //Toast.makeText(ScanBarCodeActivity.this,ex.getMessage().toString(),Toast.LENGTH_LONG).show();
            //handleError(ex);
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
        if(scanMethod=='C'){
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
        }else if(scanMethod=='B'){

            //layout_input_bluetooth.setVisibility(View.VISIBLE);
            //et_barcode.setVisibility(View.VISIBLE);
            et_warehouse.requestFocus();

            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void handleError(final Exception ex) {
        final String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        ex.printStackTrace();
        ScanBarCodeActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanBarCodeActivity.this);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(message);
                builder.setTitle("Error Found");
                builder.show();
            }
        });
    }

}
