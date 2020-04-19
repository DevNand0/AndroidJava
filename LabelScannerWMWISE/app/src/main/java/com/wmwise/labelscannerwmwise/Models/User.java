package com.wmwise.labelscannerwmwise.Models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User {

    //campos relevantes de la aplicacion
    private boolean sin_login;
    private int op_lector;
    private int op_confirmacion;
    private int active_user;
    private int op_buttons;

    private int is_root;

    //traido desde el servicio
    private String username;

    private String name;
    private int id;
    private String login_date;
    private String ip;
    private int status;
    private String token;
    private String profile;


    public User(){
        sin_login=false;
        op_lector=1;//Lector por defecto
        //0(Camara),1(Adaptador Laser)
        op_confirmacion =0;//si, por defecto
        //0(Yes),1(No)
        active_user=1;//usuario tiene sesion activa
        //1(habilitado),0(deshabilitado)
        op_buttons = 1;//uso de el boton disparador desde la aplicacion o desde el handler(no por defecto)
        //0(Yes),1(No)
        token="";


        Date todaysDate = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        login_date = df.format(todaysDate);//fecha de hoy
    }

    public User(String login_date){
        sin_login=false;
        op_lector=1;//Lector por defecto
        //0(Camara),1(Adaptador Laser)
        op_confirmacion =0;//si, por defecto
        //0(Yes),1(No)
        active_user=1;//usuario tiene sesion activa
        //1(habilitado),0(deshabilitado)
        op_buttons = 1;//uso de el boton disparador desde la aplicacion o desde el handler(no por defecto)
        //0(Yes),1(No)
        token="";

        this.login_date = login_date;
    }

    public int getIs_root() {
        return is_root;
    }

    public void setIs_root(int is_root) {
        this.is_root = is_root;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getOp_buttons() {
        return op_buttons;
    }

    public void setOp_buttons(int op_buttons) {
        this.op_buttons = op_buttons;
    }

    public int getActive_user() {
        return active_user;
    }

    public void setActive_user(int active_user) {
        this.active_user = active_user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin_date() {
        return login_date;
    }

    public void setLogin_date(String login_date) {
        this.login_date = login_date;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public boolean isSin_login() {
        return sin_login;
    }

    public void setSin_login(boolean sin_login) {
        this.sin_login = sin_login;
    }

    public int getOp_lector() {
        return op_lector;
    }

    public void setOp_lector(int op_lector) {
        this.op_lector = op_lector;
    }

    public int getOp_confirmacion() {
        return op_confirmacion;
    }

    public void setOp_confirmacion(int op_confirmacion) {
        this.op_confirmacion = op_confirmacion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
