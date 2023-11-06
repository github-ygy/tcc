package com.ygy.tcc.notification.generator;


import java.util.UUID;

public class DefaultIdGenerator implements IdGenerator {

    @Override
    public String generateNotificationId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
