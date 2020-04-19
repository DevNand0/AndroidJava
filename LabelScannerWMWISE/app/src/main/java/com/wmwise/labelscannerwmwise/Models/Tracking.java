package com.wmwise.labelscannerwmwise.Models;

public class Tracking {

    private int id;
    private int id_carrier;
    private String carrier_name;
    private String code;
    private int status;

    public int getId_carrier() {
        return id_carrier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId_carrier(int id_carrier) {
        this.id_carrier = id_carrier;
    }

    public String getCarrier_name() {
        return carrier_name;
    }

    public void setCarrier_name(String carrier_name) {
        this.carrier_name = carrier_name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
