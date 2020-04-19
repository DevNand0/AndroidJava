package com.wmwise.labelscannerwmwise.DeviceServices;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.appbtn.AppButtonManager;
import com.panasonic.toughpad.android.api.barcode.BarcodeData;
import com.panasonic.toughpad.android.api.barcode.BarcodeException;
import com.panasonic.toughpad.android.api.barcode.BarcodeListener;
import com.panasonic.toughpad.android.api.barcode.BarcodeReader;
import com.panasonic.toughpad.android.api.barcode.BarcodeReaderManager;
import com.wmwise.labelscannerwmwise.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ButtonTriggerActivity extends AppCompatActivity implements ToughpadApiListener, BarcodeListener {

    private BarcodeReader selectedReader;
    private List<BarcodeReader> readers;

    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(ButtonTriggerActivity.this);
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
                params[0].addBarcodeListener(ButtonTriggerActivity.this);
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
                Toast.makeText(ButtonTriggerActivity.this, selectedReader.getDeviceName(), Toast.LENGTH_SHORT).show();
                onBarcodeEnabled();
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

        if (selectedReader.isExternal()) {
            try {
                int charge = selectedReader.getBatteryCharge();

            } catch (BarcodeException ex) {
                //handleError(ex);
                //txtDeviceInfo.setText(ex.getMessage().toString());
            }
        }
    }

    private static ButtonTriggerActivity instance;
    private boolean isLoaded =false;

    public static ButtonTriggerActivity getInstance(){
        return instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void onRead(BarcodeReader barcodeReader, final BarcodeData result) {

        runOnUiThread(new Runnable() {
            public void run() {


                AlertDialog.Builder builder = new AlertDialog.Builder(ButtonTriggerActivity.this);
                String formatted = String.format(getString(R.string.dlg_bcr_scanned),
                        result.getSymbology(), result.getEncoding(), result.getTextData());
                builder.setMessage(formatted);
                builder.setTitle(R.string.title_bcr_scanned);
                builder.setCancelable(true);
                builder.show();
            }
        });
    }

    public void onApiConnected(int version) {
        instance = this;
        isLoaded = true;

        readers = BarcodeReaderManager.getBarcodeReaders();
        List<String> readerNames = new ArrayList<String>();
        for (BarcodeReader reader : readers) {
            readerNames.add(reader.getDeviceName());
        }
        selectedReader = readers.get(0);

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

    public void onApiDisconnected() {
    }

    public void updateButtonState(Intent buttonIntent) {
        if (buttonIntent.getAction().equals(AppButtonManager.ACTION_APPBUTTON)) {
            int buttonId = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_BUTTON, 0);
            boolean down = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_STATE, 0) == AppButtonManager.EXTRA_APPBUTTON_STATE_DOWN;
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
                        //txtA1.setBackgroundResource(newBackground);
                        Toast.makeText(ButtonTriggerActivity.this," A1",Toast.LENGTH_LONG).show();
                        break;
                    case AppButtonManager.BUTTON_A2:
                        //txtA2.setBackgroundResource(newBackground);
                        Toast.makeText(ButtonTriggerActivity.this," A2",Toast.LENGTH_LONG).show();
                        break;
                    case AppButtonManager.BUTTON_A3:
                        //txtA3.setBackgroundResource(newBackground);
                        Toast.makeText(ButtonTriggerActivity.this," A3",Toast.LENGTH_LONG).show();
                        break;
                    case AppButtonManager.BUTTON_USER:
                        //txtUser.setBackgroundResource(newBackground);
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
                        Toast.makeText(ButtonTriggerActivity.this," USER",Toast.LENGTH_LONG).show();
                        break;
                    case AppButtonManager.BUTTON_SIDE:
                        //txtSide.setBackgroundResource(newBackground);
                        Toast.makeText(ButtonTriggerActivity.this," SIDE",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_trigger);

        ToughpadApi.initialize(this, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ToughpadApi.destroy();

        instance = null;
        isLoaded = false;
    }
}
