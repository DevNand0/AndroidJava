package com.wmwise.labelscannerwmwise.Models;

/**
 * Created by Pedro Avellaneda on 11/04/2018.
 */

public class Item {
    private int id;
    private int lg_id;
    private int receipt_id;
    private String cargo_type_code;
    private int cargo_type_id;
    private boolean picked;
    private boolean loaded;
    private String width;
    private String height;
    private String length;
    private int pcs;
    private int pcs_picked;
    private int pcs_loaded;
    private int line;

    public Item(){
        this.line=0;
        this.height="0";
        this.width="0";
        this.length="0";
    }

    public String getDims(){
        float h =Float.parseFloat(this.height);
        float w =Float.parseFloat(this.width);
        float l =Float.parseFloat(this.length);
        int int_h =(int)h;
        int int_w =(int)w;
        int int_l =(int)l;
        String height =String.valueOf(int_h);
        String width =String.valueOf(int_w);
        String length =String.valueOf(int_l);

        return height+" X "+width+" X "+length;
    }

    public int getLg_id() {
        return lg_id;
    }

    public void setLg_id(int lg_id) {
        this.lg_id = lg_id;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReceipt_id() {
        return receipt_id;
    }

    public void setReceipt_id(int receipt_id) {
        this.receipt_id = receipt_id;
    }

    public String getCargo_type_code() {
        return cargo_type_code;
    }

    public void setCargo_type_code(String cargo_type_code) {
        this.cargo_type_code = cargo_type_code;
    }

    public int getCargo_type_id() {
        return cargo_type_id;
    }

    public void setCargo_type_id(int cargo_type_id) {
        this.cargo_type_id = cargo_type_id;
    }

    public boolean isPicked() {
        picked = (this.pcs_picked==pcs)?true:false;
        return picked;
    }

    public boolean isLoaded() {
        loaded = (this.pcs_loaded==pcs)?true:false;
        return loaded;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public int getPcs() {
        return pcs;
    }

    public void setPcs(int pcs) {
        this.pcs = pcs;
    }

    public int getPcs_picked() {
        return pcs_picked;
    }

    public void setPcs_picked(int pcs_picked) {
        this.pcs_picked = pcs_picked;
    }

    public int getPcs_loaded() {
        return pcs_loaded;
    }

    public void setPcs_loaded(int pcs_loaded) {
        this.pcs_loaded = pcs_loaded;
    }
}
