package com.wmwise.labelscannerwmwise;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.appbtn.AppButtonManager;
import com.panasonic.toughpad.android.api.barcode.BarcodeReader;
import com.panasonic.toughpad.android.api.barcode.BarcodeReaderManager;
import com.wmwise.labelscannerwmwise.DeviceServices.RefreshTokenService;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,ToughpadApiListener {


    private static SettingActivity instance;
    private boolean isLoaded =false;

    public static SettingActivity getInstance(){
        return instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    private Button btn_back;
    private CardView cv_save,cv_test_bluetooth;
    private TextView tv_recordar_sesion,tv_username,tv_title;
    private RadioGroup rbg_lector,rbg_mensaje,rbg_buttons;
    private RadioButton rb_lector_selected, rb_mensaje_selected, rb_button_op_selected;
    private User user;
    private boolean checked =false;
    private ProgressDialog dialogo;

    private UserDataSource ds;
    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initializeInputs();

        try{
            ToughpadApi.initialize(this,this);
        }catch (Exception ex){
            ((RadioButton)rbg_lector.getChildAt(1)).setEnabled(false);
            RadioButton rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(0);
            rb_btn_selected.setEnabled(false);
        }

        rbg_lector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup arg0, int id) {
                RadioButton rb_btn_selected = null;
                //int visibility = View.VISIBLE;
                int visibility = View.GONE;
                switch (id) {
                    case R.id.rb_bluetooth:
                        rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(0);
                        rb_btn_selected.setEnabled(false);
                        rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(1);
                        rb_btn_selected.setEnabled(false);
                        ((RadioButton)rbg_buttons.getChildAt(1)).setChecked(true);

                        break;
                    case R.id.rb_camera:
                        rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(0);
                        rb_btn_selected.setEnabled(false);
                        rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(1);
                        rb_btn_selected.setEnabled(true);
                        ((RadioButton)rbg_buttons.getChildAt(1)).setChecked(true);
                        visibility =View.GONE;
                        break;
                    case R.id.rb_adapter:
                        rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(0);
                        rb_btn_selected.setEnabled(true);
                        rb_btn_selected = (RadioButton) rbg_buttons.getChildAt(1);
                        rb_btn_selected.setEnabled(true);
                        visibility =View.GONE;
                        break;
                }
                cv_test_bluetooth.setVisibility(visibility);
            }
        });


        cv_save.setOnClickListener( this);
        btn_back.setOnClickListener( this);
        cv_test_bluetooth.setOnClickListener(this);
        tv_recordar_sesion.setOnClickListener( this);
        ds = new UserDataSource(AppConn.getInstance().getApplicationContext());
        try{
            ds.open();
            user = ds.getActiveUser();

            tv_username.setText(user.getName());
            checked=user.isSin_login();
            int radio_button_lector_id = rbg_lector.getChildAt(user.getOp_lector()).getId();
            rbg_lector.check( radio_button_lector_id );

            if(radio_button_lector_id<2){
                int radio_button_btn_op_id = rbg_buttons.getChildAt(user.getOp_buttons()).getId();
                rbg_buttons.check(radio_button_btn_op_id);
            }


            int radio_button_mensaje_id = rbg_mensaje.getChildAt(user.getOp_confirmacion()).getId();
            rbg_mensaje.check( radio_button_mensaje_id );
            setCheck(false);
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    private void initializeInputs(){
        rbg_lector = (RadioGroup) findViewById(R.id.rbg_lector);
        rbg_mensaje = (RadioGroup) findViewById(R.id.rbg_mensaje);
        rbg_buttons = (RadioGroup) findViewById(R.id.rbg_buttons);
        btn_back = (Button) findViewById(R.id.btn_back);
        cv_save = (CardView) findViewById(R.id.cv_save);
        cv_test_bluetooth = (CardView) findViewById(R.id.cv_test_bluetooth);
        tv_recordar_sesion = (TextView)findViewById(R.id.tv_recordar_sesion);
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("User Setting");
        cv_test_bluetooth.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View v) {
        if(v==btn_back){
            BackToMenu();
        }else if(v==cv_save){
            dialogo = ProgressDialog.show(SettingActivity.this,"Saving...","Saving, Please Wait...");
            String message ="";
            String title ="";

            if(saveConfiguration()){
                title="Success";
                message="Successfully Saved Configuration";
            }else{
                title="Error";
                message="Error to configure";
            }

            dialogo.dismiss();
            BasicAlertDialog alert = new BasicAlertDialog(SettingActivity.this,title,message,R.drawable.ic_modal);
            alert.OkButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.make();

        }else if(v==tv_recordar_sesion){
            setCheck(true);
        }else if(v==cv_test_bluetooth){
            startActivity(new Intent(SettingActivity.this,TestBlueToothActivity.class));
        }
    }

    private void setCheck(boolean clicked){
        int icon = R.drawable.ic_check_box_outline_blank_black_24dp;
        if(clicked)
            checked=!checked;

        if(checked)
           icon = R.drawable.ic_check_box_black_24dp;

        tv_recordar_sesion.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);
    }

    private void BackToMenu(){
        Intent i = new Intent(SettingActivity.this,MenuActivity.class);
        startActivity(i);
        finish();
    }

    private boolean saveConfiguration(){

        int rb_lector_ID = rbg_lector.getCheckedRadioButtonId();
        rb_lector_selected = (RadioButton) rbg_lector.findViewById(rb_lector_ID);
        int op_input = rbg_lector.indexOfChild(rb_lector_selected);//camera,laser,bluetooth

        int rb_buttons_ID = rbg_buttons.getCheckedRadioButtonId();
        rb_button_op_selected = (RadioButton) rbg_buttons.findViewById(rb_buttons_ID);
        int op_btn_sel = rbg_buttons.indexOfChild(rb_button_op_selected);//si, no
        op_btn_sel=(op_input==2)?2:op_btn_sel;

        int rb_mensaje_ID = rbg_mensaje.getCheckedRadioButtonId();
        rb_mensaje_selected = (RadioButton) rbg_mensaje.findViewById(rb_mensaje_ID);
        int op_msj = rbg_mensaje.indexOfChild(rb_mensaje_selected);//si, no



        return saveUser(checked, op_input, op_btn_sel, op_msj);
    }

    public boolean saveUser(boolean checked, int input_op, int btn_op, int msj_op){
        boolean saved = false;
        user.setSin_login(checked);
        user.setOp_lector(input_op);
        user.setOp_buttons(btn_op);
        user.setOp_confirmacion(msj_op);
        long res=ds.actualizar(user);
        if(res>0)
            saved=true;
        return saved;
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
    public void onDestroy() {
        super.onDestroy();
        ToughpadApi.destroy();
        instance = null;
        isLoaded = false;
    }

}
