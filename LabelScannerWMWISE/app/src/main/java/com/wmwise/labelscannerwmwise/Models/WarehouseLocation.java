package com.wmwise.labelscannerwmwise.Models;

public class WarehouseLocation {
    private int id;
    private String wr_code;
    private String location_bin;
    private int user_id;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWr_code() {
        return wr_code;
    }

    public void setWr_code(String wr_code) {
        this.wr_code = wr_code;
    }

    public String getLocation_bin() {
        return location_bin;
    }

    public void setLocation_bin(String location_bin) {
        this.location_bin = location_bin;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
