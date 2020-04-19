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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.wmwise.labelscannerwmwise.adapters.ItemUrlAdapter;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.ConnectionLinkWebService;
import com.wmwise.labelscannerwmwise.db.UrlBaseConfigurationDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;
import com.wmwise.labelscannerwmwise.dialogs.SimpleConfirmDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UrlConfigurationActivity extends AppCompatActivity {

    private TextView tv_title;
    private CardView cv_save;
    private Button btn_back;
    private Spinner spn_option;
    private ListView lv_url;
    private EditText et_app_name,et_url;
    private String arr_http[] = {"http","https"};
    private RequestQueue request;
    private ProgressDialog dialogo;
    private ItemUrlAdapter adapter;
    private ArrayList<UrlBaseConfiguration> lista;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_configuration);

        request = ConnectionLinkWebService.getInstance().getQueue();

        cv_save = (CardView)findViewById(R.id.cv_save);
        btn_back = (Button)findViewById(R.id.btn_back);
        spn_option = (Spinner)findViewById(R.id.spn_option);
        lv_url = (ListView)findViewById(R.id.lv_url);
        lista=fillURLArrayList();
        adapter=new ItemUrlAdapter(AppConn.getInstance().getApplicationContext(),lista);
        lv_url.setAdapter(adapter);

        et_app_name = (EditText)findViewById(R.id.et_app_name);
        et_url = (EditText)findViewById(R.id.et_url);

        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("Secret Option");

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(UrlConfigurationActivity.this,R.layout.support_simple_spinner_dropdown_item,arr_http);
        spn_option.setAdapter(adapterSpinner);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UrlConfigurationActivity.this,MenuActivity.class);
                startActivity(i);
                finish();
            }
        });

        cv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputNotEmpty()){
                    dialogo = ProgressDialog.show(UrlConfigurationActivity.this, "Connecting to Service...", "Please stand by...");
                    String URL = spn_option.getSelectedItem().toString()+"://"+et_url.getText().toString().toLowerCase().trim()+"/api/v1/connect";
                    setConnectionToService(request,URL);

                }
            }
        });

        lv_url.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setApplicationURL(lista.get(position).getName().trim(),lista.get(position).getUrl().trim());
            }
        });

    }

    private void setApplicationURL(String name, final String url){

        SimpleConfirmDialog dialog = new SimpleConfirmDialog(this,name, "Do you want use this URL?",R.drawable.ic_modal);
        dialog.setPositive("Yes",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                UrlBaseConfigurationDataSource ds = new UrlBaseConfigurationDataSource(UrlConfigurationActivity.this);
                try{
                    ds.open();
                    ds.setDefaultURL(url);
                    Toast.makeText(UrlConfigurationActivity.this,"SUCCESS",Toast.LENGTH_LONG).show();
                }catch(SQLException e){
                    e.printStackTrace();
                }

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
    }


    private void setConnectionToService(final RequestQueue request, String url){
        StringRequest SR =  new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObjectParam= null;
                try {
                    dialogo.dismiss();
                    jsonObjectParam = new JSONObject(response);
                    boolean success = jsonObjectParam.getBoolean("success");
                    String message =jsonObjectParam.getString("message");
                    String title = "";
                    int icon = R.drawable.ic_warning_black_24dp;
                    if(success){
                        title = "SUCCESS";
                        icon = R.drawable.ic_modal;

                    }
                    else
                        title ="WARNING";

                    final BasicAlertDialog basicAlert = new BasicAlertDialog(UrlConfigurationActivity.this,title,message,icon);
                    basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveToDataBase();
                        }
                    });
                    basicAlert.make();

                } catch (JSONException e1) {
                    e1.printStackTrace();
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(UrlConfigurationActivity.this,"Error Network","Unable to Connect",R.drawable.ic_cloud_off_black_24dp);
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
                        BasicAlertDialog basicAlert = new BasicAlertDialog(UrlConfigurationActivity.this,"Error Encoding",e1.getMessage(),R.drawable.ic_warning_black_24dp);
                        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        basicAlert.make();
                    }
                }else{
                    BasicAlertDialog basicAlert = new BasicAlertDialog(UrlConfigurationActivity.this,"Info","Wifi turned off",R.drawable.ic_cloud_off_black_24dp);
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

    private ArrayList<UrlBaseConfiguration> fillURLArrayList(){
        UrlBaseConfigurationDataSource ds = new UrlBaseConfigurationDataSource(AppConn.getInstance().getApplicationContext());
        try{
            ds.open();
            return ds.all();
        }catch(SQLException ex){
            ex.printStackTrace();
            return new ArrayList<UrlBaseConfiguration>();
        }
    }

    private void saveToDataBase(){
        String URL = spn_option.getSelectedItem().toString()+"://"+et_url.getText().toString().trim()+"/api/v1/";
        UrlBaseConfiguration url = new UrlBaseConfiguration();
        url.setStatus(1);
        url.setName(et_app_name.getText().toString().toLowerCase().trim());
        url.setUrl(URL);
        url.setApp_url(et_url.getText().toString().toLowerCase().trim());
        int code = storeToDB(UrlConfigurationActivity.this,url);
        String title="";
        String message="";
        int ico =R.drawable.ic_modal;
        boolean showModal = false;
        if(code == 2){
            ico=R.drawable.ic_warning_black_24dp;
            title="Warning";
            message="Cant create URL, previuosly created";
            showModal=true;
        }else if(code == 3){
            title="Error";
            message="Connection error to DataBase";
            ico=R.drawable.ic_error_outline_black_24dp;
            showModal=true;
        }else if(code == 1){
            //add to list
            lista.add(getUrlBaseConfigurationInserted(UrlConfigurationActivity.this,url.getUrl()));
            adapter.notifyDataSetChanged();
            et_app_name.setText("");
            et_url.setText("");
        }

        if(showModal){
            BasicAlertDialog alerta = new BasicAlertDialog(UrlConfigurationActivity.this,title,message,ico);
            alerta.OkButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alerta.make();
        }

    }


    private int storeToDB(Context c,UrlBaseConfiguration url){
        int save_code =0;
        UrlBaseConfigurationDataSource ds = new UrlBaseConfigurationDataSource(c);
        try{
            ds.open();
            UrlBaseConfiguration tmp =ds.loadStoredUrlApplication(url.getUrl().trim());
            if(tmp==null){
                ds.insert(url);
                save_code=1;//saved
            }else{
                save_code=2;//cant create application previously created
            }
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(c,"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            save_code=3;//error DB
        }
        return save_code;
    }

    private UrlBaseConfiguration getUrlBaseConfigurationInserted(Context c,String url){
        UrlBaseConfiguration obj = null;
        UrlBaseConfigurationDataSource ds = new UrlBaseConfigurationDataSource(c);
        try{
            ds.open();
            obj = ds.loadStoredUrlApplication(url.trim());
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        return obj;
    }


    private boolean inputNotEmpty(){
        return (et_app_name.getText().length()>0&&et_url.getText().length()>0);
    }
}
