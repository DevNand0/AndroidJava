package com.wmwise.labelscannerwmwise;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jgabrielfreitas.core.BlurImageView;
import com.wmwise.labelscannerwmwise.Models.User;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.conector.BlurConnectorBuilder;
import com.wmwise.labelscannerwmwise.db.UserDataSource;

public class SplashActivity extends AppCompatActivity {

    private RelativeLayout splash_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash_layout = (RelativeLayout)findViewById(R.id.splash_layout);
        /*
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.warehouse);
        Bitmap blurredBitmap = BlurConnectorBuilder.blur(AppConn.getInstance().getApplicationContext(), originalBitmap );
        splash_layout.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
        */

        Thread hilo = new Thread(){
            @Override
            public void run() {
                Intent i = null;
                try{
                    sleep(3000);
                    User user = loadUser(getApplicationContext());
                    if(user==null){
                        i =  new Intent(getApplicationContext(),LoginActivity.class);
                    }else if(user.getActive_user()==1){
                        if(user.isSin_login()){
                            i =  new Intent(getApplicationContext(),MenuActivity.class);
                        }else{
                            i =  new Intent(getApplicationContext(),LoginActivity.class);
                        }
                    }else{
                        i = new Intent(getApplicationContext(),LoginActivity.class);
                    }
                    //startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    startActivity(i);
                    finish();
                }catch(Exception ex){

                }
            }
        };
        hilo.start();
    }

    private User loadUser(Context c){
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

}
