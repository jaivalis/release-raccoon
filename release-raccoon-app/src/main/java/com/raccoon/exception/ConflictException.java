package com.raccoon.exception;

public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 7718828512143293558L;

    public ConflictException(String message) {
        super(message);
    }

}