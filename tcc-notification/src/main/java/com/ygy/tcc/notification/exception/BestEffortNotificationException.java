package com.ygy.tcc.notification.exception;


public class BestEffortNotificationException extends RuntimeException {


    public BestEffortNotificationException(String message) {
        super(message);
    }

    public BestEffortNotificationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
