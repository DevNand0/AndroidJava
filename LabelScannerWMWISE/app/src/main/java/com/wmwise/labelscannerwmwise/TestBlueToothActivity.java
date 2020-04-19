package com.wmwise.labelscannerwmwise;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.obj.Params;

import java.util.ArrayList;
import java.util.Set;

public class TestBlueToothActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv_status_bluetooth;
    private Button btn_on, btn_off, btn_discoverable, btn_paired;
    private ImageView iv_bluetooth;
    private ListView lv_devices;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> devicesList;

    private BluetoothAdapter bluetoothAdapter;

    private static final int REQUEST_ENABLE_BT=0;
    private static final int REQUEST_DISCOVER_BT=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_blue_tooth);

        initializeInputs();
        setListener();



        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Params.displayDialog(this,"Warning","The bluetooth is not available",R.drawable.ic_modal);
            tv_status_bluetooth.setText("The bluetooth is not available");
        }else{
            tv_status_bluetooth.setText("Bluetooth Available");
        }

        if(bluetoothAdapter.isEnabled()){
            iv_bluetooth.setImageResource(R.drawable.ic_bluetooth_on);
        }else{
            iv_bluetooth.setImageResource(R.drawable.ic_bluetooth_off);
        }
    }


    private void initializeInputs(){
        tv_status_bluetooth = (TextView)findViewById(R.id.tv_status_bluetooth);
        btn_on = (Button)findViewById(R.id.btn_on);
        btn_off = (Button)findViewById(R.id.btn_off);
        btn_discoverable = (Button)findViewById(R.id.btn_discoverable);
        btn_paired = (Button)findViewById(R.id.btn_paired);
        iv_bluetooth = (ImageView)findViewById(R.id.iv_bluetooth);

        lv_devices = (ListView)findViewById(R.id.lv_devices);
        devicesList = new ArrayList();
    }

    private void setListener(){
        btn_on.setOnClickListener(this);
        btn_off.setOnClickListener(this);
        btn_discoverable.setOnClickListener(this);
        btn_paired.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v==btn_on){
            enableBlueTooth();
        }else if(v==btn_off){
            disableBlueTooth();
        }else if(v==btn_discoverable){
            discoveredBlueTooth();
        }else if(v==btn_paired){
            pairBlueTooth();
        }
    }


    private void pairBlueTooth(){
        if(bluetoothAdapter.isEnabled()){
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : devices){
                devicesList.add(device.getName());
            }
            Toast.makeText(TestBlueToothActivity.this,"hay "+devicesList.size()+" dispositivos ",Toast.LENGTH_LONG).show();
            adapter=new ArrayAdapter<String>(AppConn.getInstance().getApplicationContext(),android.R.layout.simple_list_item_1,devicesList);
            lv_devices.setAdapter(adapter);
        }else{
            Params.displayDialog(this,"info","Bluetooth is already off",R.drawable.ic_modal);
        }
    }


    private void enableBlueTooth(){
        if(!bluetoothAdapter.isEnabled()){
            Intent intent =  new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }else{
            tv_status_bluetooth.setText("Turning bluetooth on ");
            Params.displayDialog(this,"Warning","The Bluetooth is already on!!",R.drawable.ic_modal);
        }

    }

    private void disableBlueTooth(){
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
            iv_bluetooth.setImageResource(R.drawable.ic_bluetooth_off);
            tv_status_bluetooth.setText("Turning bluetooth off ");
            if(lv_devices.getCount()>0){
                for(int i=0;i<devicesList.size();i++){
                    devicesList.remove(i);
                }
                //devicesList.clear();
                lv_devices.setAdapter(null);
            }
        }else{
            tv_status_bluetooth.setText("Bluetooth is already off ");
            Params.displayDialog(this,"info","Bluetooth is already off",R.drawable.ic_modal);
        }
    }

    private void discoveredBlueTooth(){
        if(!bluetoothAdapter.isEnabled()){
            Intent intent =  new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(intent, REQUEST_DISCOVER_BT);
        }else{
            Params.displayDialog(this,"Warning","The Bluetooth is already on!!",R.drawable.ic_modal);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode==RESULT_OK){
                    iv_bluetooth.setImageResource(R.drawable.ic_bluetooth_on);
                }else{
                    Params.displayDialog(this,"Error","could't on bluetooth!!",R.drawable.ic_error_outline_black_24dp);
                }
            break;
        }
        super.onActivityResult(requestCode,resultCode,data);

    }

}
