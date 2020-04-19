package com.wmwise.labelscannerwmwise;

import android.app.Activity;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.wmwise.labelscannerwmwise.Models.MessageTransaction;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.Models.Warehouse;
import com.wmwise.labelscannerwmwise.Models.WarehouseLocation;
import com.wmwise.labelscannerwmwise.adapters.ItemMessageTransactionAdapter;
import com.wmwise.labelscannerwmwise.adapters.ItemWarehouseCodeAdapter;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.db.WarehouseLocationDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.dialogs.SimpleConfirmDialog;
import com.wmwise.labelscannerwmwise.obj.Params;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;

public class MassiveAssignationActivity extends AppCompatActivity implements View.OnClickListener, BarcodeListener, ToughpadApiListener {

    private static MassiveAssignationActivity instance;
    private boolean isLoaded =false;

    public static MassiveAssignationActivity getInstance(){
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


    private EditText et_location;
    private ListView lv_warehouse_codes;
    private ArrayList<Warehouse> warehouse_codes_list;
    private ItemWarehouseCodeAdapter adaptador;
    private CardView cv_scan, cv_send, cv_clear;
    private Button btn_back;
    private TextView tv_title,tv_texto_scan;

    private char scanMethod = 'L';//L(laser),C(camera)
    private char BtnMethod = 'H';//H(hardware),S(software)

    public User user;
    private ProgressDialog dialogo;
    private RequestQueue request;

    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;


    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(MassiveAssignationActivity.this);
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
                params[0].addBarcodeListener(MassiveAssignationActivity.this);
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

    private LinearLayout layout_input_bluetooth;
    private EditText et_barcode;
    private Button btn_add;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_massive_asignation);

        user = loadUser(AppConn.getInstance().getApplicationContext());
        selectedReader = null;
        readers = null;

        try{
            ToughpadApi.initialize(this,this);
        }catch (Exception ex){
            setToughpadHardwareException(user);
        }

        initializeInputs();
        setListeners();

        if(scanMethod=='C'||scanMethod=='L'){
            et_location.setKeyListener(null);
        }

        request = ConnectionLinkWebService.getInstance().getQueue();

        preloadWarehouses(AppConn.getInstance().getApplicationContext());

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

    private void preloadWarehouses(Context c){
        WarehouseLocationDataSource ds = new WarehouseLocationDataSource(c);
        ArrayList<WarehouseLocation> listWarehouseLocation;
        try{
            ds.open();
            listWarehouseLocation=ds.warehouseLocationsList();
            int total_locations=listWarehouseLocation.size();
            if(total_locations>0){
                String location="";
                for(int g=0;g<total_locations;g++){
                    Warehouse w=new Warehouse();
                    location = listWarehouseLocation.get(g).getLocation_bin();
                    w.setCode(listWarehouseLocation.get(g).getWr_code());
                    warehouse_codes_list.add(w);
                }
                et_location.setText(location);
                adaptador.notifyDataSetChanged();
            }

        }catch(SQLException ex){
            Toast.makeText(MassiveAssignationActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }


    }

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
        et_location=(EditText)findViewById(R.id.et_location);
        lv_warehouse_codes=(ListView)findViewById(R.id.lv_warehouse_codes);
        cv_scan = (CardView)findViewById(R.id.cv_scan);
        cv_send = (CardView)findViewById(R.id.cv_send);
        cv_clear = (CardView)findViewById(R.id.cv_clear);
        btn_back = (Button)findViewById(R.id.btn_back);
        tv_texto_scan = (TextView)findViewById(R.id.tv_texto_scan);
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("Warehouse Massive Assignation");
        warehouse_codes_list = new ArrayList<Warehouse>();
        adaptador=new ItemWarehouseCodeAdapter(AppConn.getInstance().getApplicationContext(),warehouse_codes_list);
        lv_warehouse_codes.setAdapter(adaptador);

        lv_warehouse_codes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long arg3) {
                final Warehouse deleted = warehouse_codes_list.get(position);
                SimpleConfirmDialog confirm =  new SimpleConfirmDialog(MassiveAssignationActivity.this,"Info","Do you remove the warehouse from the list?",R.drawable.ic_modal);
                confirm.setPositive("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adaptador.remove(deleted);
                        adaptador.notifyDataSetChanged();
                        Toast.makeText(MassiveAssignationActivity.this,"Warehouse "+deleted.getCode()+" deleted",Toast.LENGTH_LONG).show();
                    }
                });
                confirm.setNegative("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                confirm.make();
                return false;
            }

        });
        bluetoothInstances();
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


    private void bluetoothDeviceMethod(){
        //Toast.makeText(MassiveAssignationActivity.this,"bluetooth activado "+et_location.getText().toString(),Toast.LENGTH_LONG).show();
        if(!bluetoothAdapter.isEnabled()){
            Intent intent =  new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        tv_texto_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);
    }


    private void addToList(){
        if(et_barcode.getText().length()>0&&et_location.getText().length()>0){
            fixInput();
            onTriggerScanner(et_barcode.getText().toString());
        }
        et_barcode.setText("");
        et_barcode.requestFocus();
    }


    private int actionButtonIcon(){
        if(bluetoothAdapter.isEnabled())
            return R.drawable.ic_keyboard_black_24dp;
        else
            return R.drawable.ic_bluetooth_connected_black_24dp;
    }


    private void setListeners(){
        cv_clear.setOnClickListener(this);
        cv_scan.setOnClickListener(this);
        cv_send.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        if(BtnMethod=='H')
            cv_scan.setVisibility(View.GONE);
        else{
            cv_scan.setVisibility(View.VISIBLE);
            if(BtnMethod=='R'){
                tv_texto_scan.setText("Enable ");
                tv_texto_scan.setCompoundDrawablesWithIntrinsicBounds(actionButtonIcon(), 0, 0, 0);
            }
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


    private void onTriggerScanner(String input){
        //if(et_location.getText().length()>0) {
        if(input.length()>0) {
                if(et_location.getText().toString().equals("")){
                    et_location.setText(input);
                }else{
                    if (!warehouseFromListExist(warehouse_codes_list, input)) {
                        Warehouse w = new Warehouse();
                        w.setCode(input);

                        warehouse_codes_list.add(w);
                        setReverseCodesList(w);
                        adaptador.notifyDataSetChanged();
                        saveWarehouseLocation(MassiveAssignationActivity.this,input);
                    } else {
                        Params.displayDialog(MassiveAssignationActivity.this, "Warning", "The code is already in the list", R.drawable.ic_modal);
                    }
                }
        }else{
            Params.displayDialog(MassiveAssignationActivity.this, "Warning", "Please, Scan a code", R.drawable.ic_modal);
        }

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





    private boolean warehouseFromListExist(ArrayList<Warehouse> lista,String compare){
        for(int i=0;i<lista.size();i++){
            if(lista.get(i).getCode().equals(compare)){
                return true;
            }
        }
        return false;
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
                onTriggerScanner(result.getTextData());
                //et_warehouse_code.setText(result.getTextData());
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


    @Override
    public void onClick(View v) {
        if(v==cv_scan){
            scan();
        }else if(v==cv_send) {
            send();
        }else if(v==btn_back){
            backToMenu();
        }else if(v==btn_add){
            addToList();
        }else if(v==cv_clear){
            clearAll();
        }
    }


    private void clearAll(){
        deleteWarehouseLocation(AppConn.getInstance().getApplicationContext(),1);
        warehouse_codes_list.clear();
        adaptador.notifyDataSetChanged();
        et_barcode.setText("");
        et_location.setText("");
        et_location.requestFocus();

        if(messageTransactions.size()>0){
            messageTransactions.clear();
            transactionAdapter.notifyDataSetChanged();
        }

    }


    private void saveWarehouseLocation(Context c,String warehouse_code){
        WarehouseLocationDataSource ds = new WarehouseLocationDataSource(c);
        try{
            ds.open();
            WarehouseLocation wl=new WarehouseLocation();
            wl.setStatus(1);
            wl.setLocation_bin(et_location.getText().toString());
            wl.setUser_id(user.getId());
            wl.setWr_code(warehouse_code);
            if(!ds.save(wl)){
                ds.destroy(1);
            }
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    private boolean deleteWarehouseLocation(Context c,int status){
        WarehouseLocationDataSource ds = new WarehouseLocationDataSource(c);
        try{
            ds.open();
            return ds.destroyLocation(status,et_location.getText().toString().trim());
        }catch(SQLException ex){
            return false;
        }
    }


    private void scan(){
        if(scanMethod=='C'){
            cameraMethod();
        }else if(scanMethod=='L'){
            laserMethod();
        }else if(scanMethod=='B'){
            bluetoothDeviceMethod();
        }
    }



    private void send(){
        if(validate()){
            String url = Params.appURL()+"massive_assign";
            dialogo = ProgressDialog.show(MassiveAssignationActivity.this, "Sending...", "Please Wait...");
            requestPOST_Warehouses(request, url);
        }else{
            BasicAlertDialog basicAlert = new BasicAlertDialog(MassiveAssignationActivity.this,"Warning","First, yo must enter a Carrier and scan a Tracking",R.drawable.ic_warning_black_24dp);
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
        return et_location.getText().length()>0 && lv_warehouse_codes.getCount()>0;
    }


    private void fixInput(){
        if(et_barcode.getVisibility()==View.VISIBLE){
            if(et_barcode.getText().toString().length()>0)
                et_barcode.setText(Params.textFilterFixer(et_barcode.getText().toString()));
        }
        et_location.setText(Params.textFilterFixer(et_location.getText().toString()));
    }


    ArrayList<MessageTransaction> messageTransactions;
    ItemMessageTransactionAdapter transactionAdapter;
    private void requestPOST_Warehouses(RequestQueue request, String url){
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
                        deleteWarehouseLocation(MassiveAssignationActivity.this,1);
                        warehouse_codes_list.clear();
                        adaptador.notifyDataSetChanged();
                        JSONArray responsesArray = jsonObjectParam.getJSONArray("response");
                        int responseCount = responsesArray.length();
                        if(responseCount>0){
                            messageTransactions = new ArrayList<>();
                            JSONObject jsonMessage = null;
                            for(int c=0;c<responseCount;c++){
                                jsonMessage=responsesArray.getJSONObject(c);
                                MessageTransaction mt = new MessageTransaction();
                                boolean dataParam = jsonMessage.getBoolean("success");
                                String dataTitle = (dataParam)?"Success":"Warning";
                                mt.setMessage(jsonMessage.getString("message"));
                                mt.setTitle(dataTitle);
                                mt.setSuccess(dataParam);
                                messageTransactions.add(mt);
                            }
                            transactionAdapter = new ItemMessageTransactionAdapter(MassiveAssignationActivity.this,messageTransactions);
                            lv_warehouse_codes.setAdapter(transactionAdapter);
                        }
                    }
                    else
                        title ="WARNING";
                    dialogo.dismiss();
                    final BasicAlertDialog basicAlert = new BasicAlertDialog(MassiveAssignationActivity.this,title,message,icon);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            et_location.setText("");
                            et_location.requestFocus();
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(MassiveAssignationActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(MassiveAssignationActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(MassiveAssignationActivity.this,"Info","Wifi turned off",R.drawable.ic_cloud_off_black_24dp);
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
                String warehouses = "";
                for(int i=0;i<warehouse_codes_list.size();i++){
                    String flag = (i==(warehouse_codes_list.size()-1))?"":",";
                    warehouses +=warehouse_codes_list.get(i).getCode()+flag;
                }
                String location_label[]=et_location.getText().toString().split("-");
                String location =location_label[0];
                String bin =location_label[1];
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("location",location);
                params.put("bin",bin);
                params.put("labels",warehouses);
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

    private void backToMenu(){
        Intent i = new Intent(MassiveAssignationActivity.this,MenuActivity.class);
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
                Log.d("ScanBarCodeActivity","Cancelled scan");
                Toast.makeText(this,"Cancelled",Toast.LENGTH_LONG).show();;
            }
            else
            {
                onTriggerScanner(result.getContents());
            }
        }
        else
        {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

}
