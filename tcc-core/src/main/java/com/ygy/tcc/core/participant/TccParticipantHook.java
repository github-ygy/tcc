package com.ygy.tcc.core.participant;


public interface TccParticipantHook {

    void beforeExecuteTry();

    void beforeExecuteConfirm();

    void beforeExecuteRollback();


}
