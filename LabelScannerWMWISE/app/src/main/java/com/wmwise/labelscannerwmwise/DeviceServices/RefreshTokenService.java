package com.wmwise.labelscannerwmwise.DeviceServices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
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
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.obj.Params;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshTokenService extends Service {

    public RefreshTokenService(Context applicationContext) {
        super();
    }

    public RefreshTokenService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent("com.wmwise.labelscannerwmwise.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private Timer timer;
    private TimerTask timerTask;


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();


        //schedule the timer, to wake up every 1 minute
        timer.schedule(timerTask, 45*60*1000, 45*60*1000);
        //timer.schedule(timerTask, 10*1000, 10*1000);
    }


    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                String url =Params.appURL()+"refresh";
                RequestQueue request = ConnectionLinkWebService.getInstance().getQueue();
                refreshToken(request,url);
            }
        };
    }



    private void refreshToken(RequestQueue request, String url){
        StringRequest SR =  new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObjectParam= null;
                try {
                    jsonObjectParam = new JSONObject(response);
                    User user = getCurrentUser(AppConn.getInstance().getApplicationContext());
                    if(user!=null){
                        UserDataSource ds = new UserDataSource(AppConn.getInstance().getApplicationContext());
                        ds.open();
                        String ip = Params.deviceIp(AppConn.getInstance().getApplicationContext());
                        int id = jsonObjectParam.getInt("id");
                        String token = jsonObjectParam.getString("token");
                        user.setIp(ip);
                        user.setToken(token);
                        ds.updateToken(id,token);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    //handleError(e1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        //JSONObject obj = new JSONObject(res);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                        //handleError(e1);
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                User user = null;
                try{
                    user = getCurrentUser(AppConn.getInstance().getApplicationContext());
                    params.put("Authorization", "Bearer "+user.getToken());
                }catch(SQLException e){
                    e.printStackTrace();
                    user = null;
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

    private User getCurrentUser(Context context){
        UserDataSource uds = new UserDataSource(context);
        User user =null;
        try{
            uds.open();
            user = uds.getActiveUser();
        }catch(SQLException e){
            e.printStackTrace();
            user = null;
        }
        return user;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
