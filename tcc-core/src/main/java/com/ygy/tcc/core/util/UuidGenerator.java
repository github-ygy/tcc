package com.ygy.tcc.core.util;

import java.util.UUID;


public class UuidGenerator {

    public static String generateTccId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String generateParticipantId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
