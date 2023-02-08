package com.lee.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class Message<T extends Object> {
    private String type;
    private T data;

    public Message(String type,T data) {
        this.type = type;
        this.data = data;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }

    public Object getData(){
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
