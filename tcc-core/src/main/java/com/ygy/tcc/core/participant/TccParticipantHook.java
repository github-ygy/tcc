package com.ygy.tcc.core.participant;


public interface TccParticipantHook {

    void beforeExecuteTry(TccPropagationContext propagationContext);

    void beforeExecuteConfirm(TccPropagationContext propagationContext);

    void beforeExecuteRollback(TccPropagationContext propagationContext);


}
