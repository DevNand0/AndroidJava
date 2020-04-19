package com.wmwise.labelscannerwmwise.Models;

import java.util.ArrayList;

/**
 * Created by Pedro Avellaneda on 11/04/2018.
 */

public class Warehouse {
    private String code;
    private int loading_guide_id;
    private int receipt_id;
    private String shipper_name;
    private int shipper_id;
    private String consignee_name;
    private int consignee_id;
    private char op;

    public ArrayList<Item> items;

    public Warehouse(){
        this.items=new ArrayList<Item>();
    }

    public int getLoading_guide_id() {
        return loading_guide_id;
    }

    public void setLoading_guide_id(int loading_guide_id) {
        this.loading_guide_id = loading_guide_id;
    }

    public char getOp() {
        return op;
    }

    public void setOp(char op) {
        this.op = op;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getReceipt_id() {
        return receipt_id;
    }

    public void setReceipt_id(int receipt_id) {
        this.receipt_id = receipt_id;
    }

    public String getShipper_name() {
        return shipper_name;
    }

    public void setShipper_name(String shipper_name) {
        this.shipper_name = shipper_name;
    }

    public int getShipper_id() {
        return shipper_id;
    }

    public void setShipper_id(int shipper_id) {
        this.shipper_id = shipper_id;
    }

    public String getConsignee_name() {
        return consignee_name;
    }

    public void setConsignee_name(String consignee_name) {
        this.consignee_name = consignee_name;
    }

    public int getConsignee_id() {
        return consignee_id;
    }

    public void setConsignee_id(int consignee_id) {
        this.consignee_id = consignee_id;
    }
}
