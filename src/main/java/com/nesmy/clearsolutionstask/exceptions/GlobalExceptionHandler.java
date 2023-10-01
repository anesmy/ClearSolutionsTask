package com.nesmy.clearsolutionstask.exceptions;

import com.nesmy.clearsolutionstask.dto.ApiErrorDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorDTO> handleApiException(ApiException apie) {
        ApiErrorDTO apiErrorDTO = new ApiErrorDTO();
        apiErrorDTO.setErrors(apie.getApiErrors());
        return new ResponseEntity<>(apiErrorDTO, apie.getStatusCode());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO>
    handleValidationExceptions(ConstraintViolationException ex) {
        ApiErrorDTO apiErrorDTO = new ApiErrorDTO();
        ex.getConstraintViolations().forEach(cve -> {
            ApiError apiError = new ApiError();
            String[] path = cve.getPropertyPath().toString().split("\\.");
            apiError.setFieldName(path[path.length-1]);
            apiError.setMessage(cve.getMessageTemplate());
            apiErrorDTO.getErrors().add(apiError);
        });
        return new ResponseEntity<>(apiErrorDTO, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}