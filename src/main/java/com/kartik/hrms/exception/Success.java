package com.kartik.hrms.exception;

import org.springframework.http.HttpStatus;

public class Success extends ApiException {
    public Success(String message) {
        super(message, HttpStatus.OK);
    }
}