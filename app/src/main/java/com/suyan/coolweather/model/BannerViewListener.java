package com.suyan.coolweather.model;

/**
 * Created by Yan on 2016/9/10.
 */
public interface BannerViewListener {

    /**
     * 请求广告成功
     */
    void onRequestSuccess();

    /**
     * 切换广告条
     */
    void onSwitchBanner();

    /**
     * 请求广告失败
     */
    void onRequestFailed();
}
