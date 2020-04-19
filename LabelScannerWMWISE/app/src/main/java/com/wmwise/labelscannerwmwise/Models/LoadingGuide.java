package com.wmwise.labelscannerwmwise.Models;

public class LoadingGuide {
    private String type;
    private int id;
    private String code;
    private int complete;
    private int total_pcs;

    public LoadingGuide(){
        complete = 0;//(0,1,2)vacio, en curso, completo
    }

    public int getTotal_pcs() {
        return total_pcs;
    }

    public void setTotal_pcs(int total_pcs) {
        this.total_pcs = total_pcs;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
