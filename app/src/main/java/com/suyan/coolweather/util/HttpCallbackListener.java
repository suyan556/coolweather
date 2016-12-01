package com.suyan.coolweather.util;

/**
 * Created by Yan on 2016/9/3.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
