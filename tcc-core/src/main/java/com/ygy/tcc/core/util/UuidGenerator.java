package com.ygy.tcc.core.util;

import java.util.UUID;


public class UuidGenerator {

    public static String generateTccId() {
        return UUID.randomUUID().toString();
    }

    public static String generateParticipantId() {
        return UUID.randomUUID().toString();
    }
}
