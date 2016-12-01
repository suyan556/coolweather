package com.suyan.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suyan.coolweather.R;
import com.suyan.coolweather.model.City;
import com.suyan.coolweather.service.AutoUpdateService;
import com.suyan.coolweather.util.HttpCallbackListener;
import com.suyan.coolweather.util.HttpUtil;
import com.suyan.coolweather.util.Utility;

import net.youmi.android.AdManager;
import net.youmi.android.normal.banner.BannerViewListener;

public class WeatherActivity extends Activity {

    private LinearLayout weatherInfoLayout;
    private ProgressDialog mProgressDialog;//进度条
    private SharedPreferences mSharedPreferences;//数据存储对象
    private SharedPreferences.Editor mEditor;
    private static final int REQUEST_CODE = 1;

    private Button mChangeCityButton;//小房子按钮
    private TextView mTextView_cityName;//标题栏城市名称
    private Button mRefreshButton;//刷新按钮
    private TextView mTextView_updateTiem;//更新时间
    private TextView mTextView_current_date;//当前时间
    private TextView mTextView_weather_desp;//具体的天气情况
    private TextView mTextView_textView_temp1;//最低温度
    private TextView mTextView_textView_temp2;//最高天气

    private City mCity_current = new City();//当前显示的城市对象

    private BannerViewListener bannerViewListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化有米广告
        AdManager.getInstance(this).init("cbf16aad95c58d2a", "f933aff675c4761e", false, true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);


        //实例化本地存储
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        //更新城市
        mChangeCityButton = (Button) findViewById(R.id.button_changeCity);
        mChangeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //实例化各个控件
        mTextView_cityName = (TextView) findViewById(R.id.textView_city_name);
        mTextView_current_date = (TextView) findViewById(R.id.textView_current_date);
        mTextView_updateTiem = (TextView) findViewById(R.id.textView_publishTime);
        mTextView_weather_desp = (TextView) findViewById(R.id.textView_weather_desp);
        mTextView_textView_temp1 = (TextView) findViewById(R.id.textView_temp1);
        mTextView_textView_temp2 = (TextView) findViewById(R.id.textView_temp2);


        //刷新按钮
        mRefreshButton = (Button) findViewById(R.id.button_refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从服务器更新
                updateWeatherFromServier();

            }
        });

        //为了在第一次安装时候，因为本地存储没有数据，所以设置默认的城市为广州
        if (mSharedPreferences.getString("city_code", null) == null) {
            mCity_current.setCity_code("CN101280101");
            updateWeatherFromServier();
        } else {
            //如果有数据，则从本地取出来， 也就是上次访问的城市，先确定这个
            loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null),
                    mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null),
                    mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
            //然后再从服务器更新一次
            updateWeatherFromServier();
        }

        //启动自动更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

        //有米广告

        /*View bannerView = BannerManager.getInstance(this).getBannerView(bannerViewListener);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.adLayout);
        linearLayout.addView(bannerView);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
            }
        }
    }

    //刷新各个组件数据的封装
    private void loadWeatherData(String city_code, String city_name, String update_time, String current_data, String txt_d, String txt_n, String tmp_min, String tmp_max) {
        mTextView_cityName.setText(city_name);
        mTextView_updateTiem.setText(update_time);
        mTextView_current_date.setText(current_data);

        if (txt_d.equals(txt_n)){
            mTextView_weather_desp.setText(txt_d);
        } else {
            mTextView_weather_desp.setText(txt_d + "转" + txt_n);
        }

        mTextView_textView_temp1.setText(tmp_min + "℃");
        mTextView_textView_temp2.setText(tmp_max + "℃");

        mCity_current.setCity_name_ch(city_name);
        mCity_current.setCity_code(city_code);

    }


    //从服务器更新数据
    private void updateWeatherFromServier() {
        String address = "https://api.heweather.com/x3/weather?cityid=" + mCity_current.getCity_code() + "&key=" + ChooseAreaActivity.WEATHER_KEY;
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Utility.handleWeatherResponse(mEditor, response)) {
                            loadWeatherData(mSharedPreferences.getString("city_code", null), mSharedPreferences.getString("city_name_ch", null), mSharedPreferences.getString("update_time", null), mSharedPreferences.getString("data_now", null), mSharedPreferences.getString("txt_d", null), mSharedPreferences.getString("txt_n", null), mSharedPreferences.getString("tmp_min", null), mSharedPreferences.getString("tmp_max", null));
                            closeProgressDialog();
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(WeatherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    //关闭进度条
    private void closeProgressDialog() {
        if (mProgressDialog != null)
        mProgressDialog.dismiss();
    }


    //显示 进度条
    private void showProgressDialog() {
        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("正在同步数据...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
}
