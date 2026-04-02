package com.zorvyn.fintech.exception;

import org.springframework.http.HttpStatus;

public class TransactionLimitException extends ApiException {

    public TransactionLimitException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
