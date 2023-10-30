package com.ygy.tcc.core.participant;


import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TccParticipantHookManager {

    private static final List<TccParticipantHook> BASE_HOOKS = Lists.newArrayList();


    private static final ReentrantReadWriteLock HOOK_READ_WRITE_LOCK = new ReentrantReadWriteLock();



    private TccParticipantHookManager() {
    }

    public static  void registerParticipantHook(TccParticipantHook hook) {
        HOOK_READ_WRITE_LOCK.readLock().lock();
        try {
            BASE_HOOKS.add(hook);
        } finally {
            HOOK_READ_WRITE_LOCK.readLock().unlock();
        }
    }

    public static List<TccParticipantHook> getParticipantHooks() {
        HOOK_READ_WRITE_LOCK.readLock().lock();
        try {
            return new ArrayList<>(BASE_HOOKS);
        } finally {
            HOOK_READ_WRITE_LOCK.readLock().unlock();
        }
    }

    public static void triggerBeforeExecuteTry() {
        List<TccParticipantHook> hooks = getParticipantHooks();
        if (CollectionUtils.isNotEmpty(hooks)) {
            for (TccParticipantHook hook : hooks) {
                hook.beforeExecuteTry();
            }
        }
    }

    public static void triggerBeforeExecuteConfirm() {
        List<TccParticipantHook> hooks = getParticipantHooks();
        if (CollectionUtils.isNotEmpty(hooks)) {
            for (TccParticipantHook hook : hooks) {
                hook.beforeExecuteConfirm();
            }
        }

    }

    public static void triggerBeforeExecuteRollback() {
        List<TccParticipantHook> hooks = getParticipantHooks();
        if (CollectionUtils.isNotEmpty(hooks)) {
            for (TccParticipantHook hook : hooks) {
                hook.beforeExecuteRollback();
            }
        }
    }



}
