package com.ygy.tcc.notification.generator;


public interface IdGenerator {
    String generateNotificationId(String resourceId, Object args);
}
