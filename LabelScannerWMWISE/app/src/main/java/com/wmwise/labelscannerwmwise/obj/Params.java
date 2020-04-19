package com.wmwise.labelscannerwmwise.obj;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.widget.Toast;

import com.wmwise.labelscannerwmwise.Models.LoadingGuide;
import com.wmwise.labelscannerwmwise.Models.UrlBaseConfiguration;
import com.wmwise.labelscannerwmwise.conector.AppConn;
import com.wmwise.labelscannerwmwise.db.ItemWarehouseDataSource;
import com.wmwise.labelscannerwmwise.db.LoadingGuideDataSource;
import com.wmwise.labelscannerwmwise.db.UrlBaseConfigurationDataSource;
import com.wmwise.labelscannerwmwise.dialogs.BasicAlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.WIFI_SERVICE;

public class Params {

    public static String deviceIp(Context context){
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }


    public static String appURL(){
        UrlBaseConfigurationDataSource url_ds =  new UrlBaseConfigurationDataSource(AppConn.getInstance().getApplicationContext());
        String urlApp = "";
        try{
            url_ds.open();
            UrlBaseConfiguration url = url_ds.getURL(1);
            urlApp=url.getUrl();
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(AppConn.getInstance().getApplicationContext(),"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return urlApp;
    }

    public static String currentDate(boolean withTime){
        Date todaysDate = new Date();
        String format = "yyyy-MM-dd";
        if(withTime)
            format = "yyyy-MM-dd hh:mm:ss";
        DateFormat df = new SimpleDateFormat(format);
        String sending_date = df.format(todaysDate);//fecha de hoy
        return sending_date;
    }

    public static String numberToString(int num){
        String text = (num < 10 ? "0" : "") + num;
        return text;
    }

    public static void clearApp(Context c){
        ItemWarehouseDataSource iw_ds = new ItemWarehouseDataSource(c);
        LoadingGuideDataSource lg_ds = new LoadingGuideDataSource(c);
        try{
            iw_ds.open();
            lg_ds.open();
            ArrayList<LoadingGuide> lista = lg_ds.showLoadingGuides(2);
            for(int i=0;i<lista.size();i++){
                iw_ds.DestroyWarehousesLoadingGuide(lista.get(i).getId());
            }
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(c,"error: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }


    public static boolean elemFromListExist(ArrayList<String> lista,String compare){
        for(int i=0;i<lista.size();i++){
            if(lista.get(i).equals(compare)){
                return true;
            }
        }
        return false;
    }


    public static void displayDialog(Activity activity, String title, String message, int ico){
        BasicAlertDialog basicAlert = new BasicAlertDialog(activity,title,message,ico);
        basicAlert.OkButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        basicAlert.make();
    }


    public static String textFilterFixer(String inputString){
        String ligadura = "ÆßŒʣʪʦЉЊѬЫæßœʩʥʫʧЮѾѨﷲ";
        String diacriticos = "ÃÅÄÀÁÂÇČÉÈÊËĔĞĢÏÎÍÌÑÖÔŌÒÓØŜŞÜŪÛÙÚŸ";

        String fix = inputString.replaceAll("[-\\[\\]^/,'*:.!><~"+ligadura+diacriticos+"£¢€@;&`#©§$%+°¼□√=?|\"\\\\()]+", "-");
        return fix;
    }

}
