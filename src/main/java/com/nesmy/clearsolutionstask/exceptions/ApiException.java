package com.nesmy.clearsolutionstask.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

public class ApiException extends ResponseStatusException {

    private List<ApiError> apiErrors = new ArrayList<>();

    public ApiException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

    public ApiException(HttpStatusCode status, List<ApiError> apiErrors) {
        super(status);
        this.apiErrors = apiErrors;
    }

    public List<ApiError> getApiErrors() {
        return apiErrors;
    }

    public void setApiErrors(List<ApiError> apiErrors) {
        this.apiErrors = apiErrors;
    }

}
