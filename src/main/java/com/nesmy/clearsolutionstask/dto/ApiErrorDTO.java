package com.nesmy.clearsolutionstask.dto;

import com.nesmy.clearsolutionstask.exceptions.ApiError;

import java.util.ArrayList;
import java.util.List;

public class ApiErrorDTO {
    private List<ApiError> errors = new ArrayList<>();

    public List<ApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }
}
