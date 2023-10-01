package com.nesmy.clearsolutionstask.dto;


public class DataDTO<T> {

    T data;

    public DataDTO(T data) {
        this.data = data;
    }

    public DataDTO() {
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
