package com.wmwise.labelscannerwmwise;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.wmwise.labelscannerwmwise.Models.UrlBaseConfiguration;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UrlBaseConfigurationDataSource;
import com.wmwise.labelscannerwmwise.db.UserDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.obj.Params;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText et_user, et_password;
    private CardView cv_login;
    private RelativeLayout layoutlogin;

    private ProgressDialog dialogo;
    private RequestQueue request;

    private boolean canConnect=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //setURL();

        et_user=(EditText)findViewById(R.id.et_user);
        et_password=(EditText)findViewById(R.id.et_password);
        cv_login = (CardView)findViewById(R.id.cv_login);
        layoutlogin =(RelativeLayout)findViewById(R.id.layoutlogin);

        if(loadDefaultURL()==null){
            canConnect=false;
        }

        cv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = et_user.getText().toString().toLowerCase();
                String password = et_password.getText().toString();

                if(usuario.trim().equals("")){
                    et_user.setError("Please, enter your username");
                }else if(password.trim().equals("")){
                    et_password.setError("Please, enter your password");
                }else{
                    if(usuario.equals("admin")&&password.equals("root")){//usuario de configuracion
                        User user = new User();
                        user.setId(0);
                        user.setName("Super User");
                        user.setUsername("Root");
                        //String ip = Params.deviceIp(AppConn.getInstance().getApplicationContext());
                        int status = 1;
                        //user.setIp(ip);
                        user.setStatus(status);
                        user.setActive_user(1);
                        user.setIs_root(1);
                        user.setToken("N/A");
                        saveUser(user);
                        startActivity(new Intent(AppConn.getInstance().getApplicationContext(),MenuActivity.class));
                        finish();
                    }else{//usuario normal
                        if(canConnect){
                            connectToServer(usuario,password);
                        }else{
                            errorDialog();
                        }

                    }

                }
            }
        });

    }

    private void errorDialog(){
        BasicAlertDialog basicAlert = new BasicAlertDialog(LoginActivity.this,
                "Warning",
                "The application doesn't have a base url, please set a base url as admin user",
                R.drawable.ic_warning_black_24dp);
        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        basicAlert.make();
    }

    private void connectToServer(String usuario,String password){
        dialogo = ProgressDialog.show(LoginActivity.this, "Login..", "Connecting to Server, Please Stand By...");
        request = ConnectionLinkWebService.getInstance().getQueue();
        String url = Params.appURL()+"login";
        postLogin(usuario.trim().toLowerCase().toString(),password.trim().toString(), request, url);
    }

    private void setURL(){
        UrlBaseConfigurationDataSource url_ds =  new UrlBaseConfigurationDataSource(AppConn.getInstance().getApplicationContext());
        UrlBaseConfiguration url = null;
        try{
            url_ds.open();
            long res =0;

            url = url_ds.getURL(1);

            if(url==null){
                url = new UrlBaseConfiguration();
                url.setUrl("https://dev.wmwise.com/api/v1/");
                url.setStatus(1);
                url.setName("Vecologistics DEV");
                res=url_ds.insert(url);
            }else{
                res=url_ds.update(url);
            }
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }


    private UrlBaseConfiguration loadDefaultURL(){
        UrlBaseConfigurationDataSource dataSource=new UrlBaseConfigurationDataSource(AppConn.getInstance().getApplicationContext());
        UrlBaseConfiguration base =  null;
        try{
            dataSource.open();
            base=dataSource.getURL(1);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return base;
    }


    private void postLogin(final String name, final String password, RequestQueue request, String url){
        StringRequest SR = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                JSONObject jsonObjectParam = null;
                try {
                    jsonObjectParam = new JSONObject(response);
                    User user = new User();
                    user.setId(jsonObjectParam.getInt("id"));
                    user.setName(jsonObjectParam.getString("name"));
                    user.setUsername(jsonObjectParam.getString("username"));
                    String ip = Params.deviceIp(AppConn.getInstance().getApplicationContext());
                    int status = jsonObjectParam.getInt("status");
                    user.setIp(ip);
                    user.setStatus(status);
                    user.setActive_user(1);
                    user.setToken(jsonObjectParam.getString("token"));
                    user.setIs_root(0);
                    saveUser(user);
                    dialogo.dismiss();

                    if(status==1){
                       startActivity(new Intent(AppConn.getInstance().getApplicationContext(),MenuActivity.class));
                       finish();
                    }else{
                        BasicAlertDialog alert = new BasicAlertDialog(AppConn.getInstance().getApplicationContext(),"Error","The user "+user.getName()+" unable to use WISE",R.drawable.ic_error_outline_black_24dp);
                    }

                } catch (JSONException e1) {
                    dialogo.dismiss();
                    try {
                        jsonObjectParam = new JSONObject(response);
                        String message =jsonObjectParam.getString("message");
                        String title = "";

                        if(jsonObjectParam.getBoolean("success"))
                            title ="Success";
                        else
                            title ="Warning";

                        BasicAlertDialog alerta = new BasicAlertDialog(LoginActivity.this,title,message,R.drawable.ic_warning_black_24dp);
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
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        BasicAlertDialog basicAlert = new BasicAlertDialog(LoginActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(LoginActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(LoginActivity.this,"Info","Wifi turned off",R.drawable.ic_cloud_off_black_24dp);
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
                params.put("username",name);
                params.put("password",password);
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


    private boolean saveUser(User user){
        boolean saved =false;

        UserDataSource ds = new UserDataSource(AppConn.getInstance().getApplicationContext());
        try{
            ds.open();
            long res =0;
            User tmp = ds.getUser(user.getId());
            if(tmp==null){
                res=ds.insert(user);
            }else{
                user.setOp_buttons(tmp.getOp_buttons());
                user.setOp_confirmacion(tmp.getOp_confirmacion());
                user.setOp_lector(tmp.getOp_lector());
                user.setSin_login(tmp.isSin_login());
                user.setIs_root(tmp.getIs_root());//no es admin ni nada
                res=ds.actualizar(user);
            }
            ds.disableUsers(user.getId());
            saved = true;
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            saved= false;
        }

        return saved;
    }


}
