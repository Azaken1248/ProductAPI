package com.seveneleven.storeapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.CONFLICT)
public class InactiveUserException extends RuntimeException {
    public InactiveUserException(String message) {
        super(message);
    }
}