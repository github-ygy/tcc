package com.ygy.tcc.core.generator;


import java.util.UUID;

public class DefaultUuidGenerator implements UuidGenerator {

    @Override
    public String generateTccId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Override
    public String generateParticipantId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
