package com.ygy.tcc.core.exception;


public class TccException extends RuntimeException {


    public TccException(String message) {
        super(message);
    }

    public TccException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
