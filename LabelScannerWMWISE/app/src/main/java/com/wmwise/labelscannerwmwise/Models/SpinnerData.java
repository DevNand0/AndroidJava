package com.wmwise.labelscannerwmwise.Models;


/**
 * Created by Pedro Avellaneda on 12/04/2018.
 */

public class SpinnerData {
    private String types;
    private String keys;
    private int icons;

    private int pos;

    public SpinnerData(String keys,String types,int icons){
        this.keys=keys;
        this.types=types;
        this.icons=icons;
    }

    public SpinnerData(){

    }

    public String getTypeNames(){
        return types;
    }

    public int getIcons(){
        return icons;
    }

    public String getKeys(){
        return keys;
    }

    public void setSelected(int pos){
        this.pos=pos;
    }

    public void setTypes(String types){
        this.types=types;
    }

    public void setIcons(int icons){
        this.icons=icons;
    }

    public void setKeys(String keys){
        this.keys=keys;
    }

    public String typeSelected(){
        return types;
    }

    public int iconSelected(){
        return icons;
    }

    public String keySelected(){
        return keys;
    }


}
