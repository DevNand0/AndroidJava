package com.wmwise.labelscannerwmwise.conector;

import android.app.Application;
import android.content.Context;

public class AppConn extends Application{
    private static Context c;
    private static AppConn instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        this.setAppContext(getAppContext());
    }

    public void setAppContext(Context contexto){
        c=contexto;
    }

    public static AppConn getInstance(){
        return instance;
    }

    public static Context getAppContext(){
        return c;
    }

}
