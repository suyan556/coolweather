package com.suyan.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.suyan.coolweather.activity.ChooseAreaActivity;
import com.suyan.coolweather.util.HttpCallbackListener;
import com.suyan.coolweather.util.HttpUtil;
import com.suyan.coolweather.util.Utility;

/**
 * Created by Yan on 2016/9/9.
 */
public class AutoUpdateService extends Service {

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updaWeather();
            }
        }).start();


        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour = 60*60*1000;
        long triggerTime = SystemClock.currentThreadTimeMillis()+hour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onCreate() {
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

    }

    //更新天气
    private void updaWeather() {
        String city_code = mSharedPreferences.getString("city_code", null);
        String address = "https://api.heweather.com/x3/weather?cityid=" + city_code + "&key=" + ChooseAreaActivity.WEATHER_KEY;
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                Utility.handleWeatherResponse(mEditor, response);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }



    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}
