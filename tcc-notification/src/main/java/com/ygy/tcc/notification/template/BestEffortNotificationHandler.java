package com.ygy.tcc.notification.template;


public interface BestEffortNotificationHandler {


    public void doLocal();

    public void doRemote();

    public void checkLocal();

    public void checkRemote();




}
