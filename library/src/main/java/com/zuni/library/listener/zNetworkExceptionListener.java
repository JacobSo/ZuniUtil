package com.zuni.library.listener;

/**
 * Created by Administrator on 2015/12/4.
 */

public interface zNetworkExceptionListener {
    void connectionTimeOut();

    void networkException();

    void resultIsNull();

    void networkDisable();
}

