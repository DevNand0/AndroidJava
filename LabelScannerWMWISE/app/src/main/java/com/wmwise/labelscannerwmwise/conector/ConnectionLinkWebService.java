package com.wmwise.labelscannerwmwise.conector;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Pedro Avellaneda on 09/04/2018.
 */

public class ConnectionLinkWebService {

    private RequestQueue queue;
    private static ConnectionLinkWebService singleton=null;

    private ConnectionLinkWebService(){
        queue = Volley.newRequestQueue(AppConn.getInstance().getApplicationContext());
    }

    public static ConnectionLinkWebService getInstance(){
        if(singleton==null)
            singleton= new ConnectionLinkWebService();
        return singleton;
    }

    public RequestQueue getQueue(){
        return this.queue;
    }
}
