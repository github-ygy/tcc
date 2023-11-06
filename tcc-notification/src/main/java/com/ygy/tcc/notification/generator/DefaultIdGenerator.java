package com.ygy.tcc.notification.generator;


import java.util.UUID;

public class DefaultIdGenerator implements IdGenerator {

    @Override
    public String generateNotificationId(String resourceId, Object args) {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
