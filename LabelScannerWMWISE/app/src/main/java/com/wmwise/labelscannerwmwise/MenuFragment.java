package com.wmwise.labelscannerwmwise;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.wmwise.labelscannerwmwise.DeviceServices.RefreshTokenService;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.adapters.ItemMenuAdater;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.obj.Params;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MenuFragment extends Fragment {
    private RecyclerView rv_main;
    private GridLayoutManager glm;


    private String titles[]={
                             "Warehouse","Assignment",
                             "Loading Guides","Settings",
                             "Tracking","Massive Asignation",
                             "Exit"
                            };
    private String descriptions[]={
                                    "Scan Barcode","Individual Assignment",
                                    "Ocean/Air","Configure User inputs",
                                    "Load a carrier and Send Trackings","Set a location to Massive warehouse",
                                    "Exit and Close app"
                                  };
    private int images[]={
                          R.drawable.ic_view_warehouse_black_24dp,R.drawable.ic_grid_on_black_24dp,
                          R.drawable.ic_chrome_reader_mode_black_24dp,R.drawable.ic_settings_black_24dp,
                          R.drawable.ic_tracking_black_24dp,R.drawable.ic_massive_asignation_black_24dp,
                          R.drawable.ic_exit_to_app_black_24dp
                         };
    private int backgrounds[]={
                               R.drawable.circlebackgroundyello,R.drawable.circlebackgroundlightgreen,
                               R.drawable.circlebackgoundpurple,R.drawable.circlebackgroundblue,
                               R.drawable.circlebackgroundgray,R.drawable.circlebackgrounddeeporange,
                               R.drawable.circlebackgroundpink
                              };

    public MenuFragment() {
        // Required empty public constructor
    }

    Intent mServiceIntent;
    private RefreshTokenService tokenService;
    private boolean isRoot = false;
    private User user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_menu, container, false);
        Params.clearApp(AppConn.getInstance().getApplicationContext());
        rv_main = (RecyclerView)v.findViewById(R.id.rv_main);
        glm=new GridLayoutManager(getActivity().getApplicationContext(),2);
        rv_main.setHasFixedSize(true);
        rv_main.setLayoutManager(glm);

        user = prepareUser(getActivity().getApplicationContext());
        bluetoothInstances(getActivity().getApplicationContext());

        if(getRootUser().getIs_root()==1){
            isRoot=true;
            String old_titles[] = new String [titles.length+1];
            String old_desctipcions[] = new String[descriptions.length+1];
            int old_images[] = new int[images.length+1];
            int old_backgrounds[] = new int[backgrounds.length+1];
            for(int i=0;i<titles.length;i++){
                old_titles[i] = titles[i];
                old_desctipcions[i] = descriptions[i];
                old_images[i] = images[i];
                old_backgrounds[i] = backgrounds[i];
            }
            old_titles[titles.length]="URL Setting";
            old_desctipcions[descriptions.length]="Secret Option, set an app URL";
            old_images[images.length]=R.drawable.ic_cloud_black_24dp;
            old_backgrounds[backgrounds.length]=R.drawable.circlebackgroundlightgreen;

            titles=old_titles;
            descriptions=old_desctipcions;
            images=old_images;
            backgrounds=old_backgrounds;

        }



        ItemMenuAdater adaptador = new ItemMenuAdater(getActivity().getApplicationContext(),images,backgrounds,titles,descriptions);
        rv_main.setAdapter(adaptador);

        final GestureDetector mGestureDetector = new GestureDetector(getActivity().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        rv_main.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                try {
                    View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    boolean exitApp=false;
                    if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                        Intent i =null;
                        int position = recyclerView.getChildAdapterPosition(child);
                        boolean changeActivity=true;
                        if(position==0){//warehouse
                            i =  new Intent(getActivity().getApplicationContext(),ScanBarCodeActivity.class);
                            changeActivity=!(isRoot);
                        }else if(position==1){//correction warehouse
                            i =  new Intent(getActivity().getApplicationContext(),CorrectionScanActivity.class);
                            changeActivity=!(isRoot);
                        }else if(position==2){//loading guide
                            i =  new Intent(getActivity().getApplicationContext(),PickAndLoadingActivity.class);
                            changeActivity=!(isRoot);
                        }else if(position==3){//setting
                            i =  new Intent(getActivity().getApplicationContext(),SettingActivity.class);
                            changeActivity=!(isRoot);
                        }else if(position==4) {//tracking
                            i = new Intent(getActivity().getApplicationContext(), ScanTrackingActivity.class);
                            changeActivity = !(isRoot);
                        }else if(position==5){//massive assignment
                            i = new Intent(getActivity().getApplicationContext(), MassiveAssignationActivity.class);
                            changeActivity = !(isRoot);
                        }else if(position==6){//exit app
                            i = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
                            exitApp=true;
                            if(Params.appURL()!=""){
                                RequestQueue request= ConnectionLinkWebService.getInstance().getQueue();
                                String url = Params.appURL()+"logout";
                                logout(request, url);
                            }
                        }else if(position==7){
                            i = new Intent(getActivity().getApplicationContext(),UrlConfigurationActivity.class);

                        }
                        if(changeActivity){
                            startActivity(i);
                            if(exitApp){
                                getActivity().finish();
                            }
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(),"Admin user only set base application URL",Toast.LENGTH_LONG).show();
                        }

                        return true;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });


        tokenService = new RefreshTokenService(getActivity().getApplicationContext());
        mServiceIntent = new Intent(getActivity().getApplicationContext(), tokenService.getClass());
        if (!isMyServiceRunning(tokenService.getClass())) {
            getActivity().startService(mServiceIntent);
        }
        return v;
    }


    private User prepareUser(Context c){
        User user = null;
        UserDataSource ds = new UserDataSource(c);
        try{
            ds.open();
            user = ds.getActiveUser();

        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(c,"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            user = null;
        }
        return user;
    }



    private BluetoothAdapter bluetoothAdapter;
    String bluetoothMessage = "";
    private void bluetoothInstances(Context c){

        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Toast.makeText(getActivity().getApplicationContext(),"The bluetooth is not available",Toast.LENGTH_LONG).show();
            bluetoothMessage ="The bluetooth is not available";
            setCameraDefault(c,user);
        }else{
            bluetoothMessage ="Bluetooth Available";
        }
    }



    private void setCameraDefault(Context c, User user){
        user.setOp_lector(0);
        user.setOp_buttons(1);
        UserDataSource uds = new UserDataSource(c);
        try{
            uds.open();
            uds.actualizar(user);
        }catch(SQLException ex){
            Toast.makeText(c,"error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private void logout(RequestQueue request, String url){
        StringRequest SR =  new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObjectParam= null;
                try {
                    jsonObjectParam = new JSONObject(response);
                    String message =jsonObjectParam.getString("message");
                    Toast.makeText(AppConn.getInstance().getApplicationContext(),message,Toast.LENGTH_SHORT).show();

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //dialogo.dismiss();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {

                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        //JSONObject obj = new JSONObject(res);
                        BasicAlertDialog basicAlert = new BasicAlertDialog(AppConn.getInstance().getApplicationContext(),"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
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

                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                UserDataSource userDataSource = new UserDataSource(AppConn.getInstance().getApplicationContext());
                try{
                    userDataSource.open();
                    User user =userDataSource.getActiveUser();
                    params.put("Authorization", "Bearer "+user.getToken());
                    userDataSource.logoutUser(user.getId());
                }catch(SQLException e){
                    e.printStackTrace();
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


    private User getRootUser(){
        User user=null;
        UserDataSource userDataSource = new UserDataSource(AppConn.getInstance().getApplicationContext());
        try{
            userDataSource.open();
            user =userDataSource.getActiveUser();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return user;
    }

}
