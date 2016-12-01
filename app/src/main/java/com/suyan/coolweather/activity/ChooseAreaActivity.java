package com.suyan.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.suyan.coolweather.R;
import com.suyan.coolweather.model.City;
import com.suyan.coolweather.model.CoolWeatherDB;
import com.suyan.coolweather.util.HttpCallbackListener;
import com.suyan.coolweather.util.HttpUtil;
import com.suyan.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    private static final String TAG = "ChooseAreaActivity";

    private ProgressDialog progressDialog;//进度条对话框
    private EditText editText;//搜索编辑框
    private ListView listView;//城市ListView
    private ArrayAdapter<String> mAdapter;//ListView适配器
    private CoolWeatherDB coolWeatherDB;//数据库操作对象
    private City mCity_selected;//选中的城市

    private List<String> cityNames = new ArrayList<>();//用于存放与输入的内容相匹配的城市名称字符串
    private List<City> mCities;//用于存放与输入的内容相匹配的城市名称对象

    private static final int NONE_DATA = 0;//标识是否有初始化城市数据

    private SharedPreferences mSharedPreferences;//本地存储
    private SharedPreferences.Editor mEditor;//本地存储

    public static final String WEATHER_KEY = "d39fb761310147919e4e6fb9027b63b0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        coolWeatherDB = CoolWeatherDB.getInstance(this);//获取数据库处理对象
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);//获取本地存储对象
        mEditor = mSharedPreferences.edit();//获取本地存储对象

        //先检查本地是否已同步过城市数据，如果没有，则从服务器同步
        if (coolWeatherDB.checkDataState() == NONE_DATA) {
            queryCitiesFromServer();
        }

        mCities = queryCitiesFromLocal("");//获取本地存储的所有的城市

        editText = (EditText) findViewById(R.id.edit_city);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCities = queryCitiesFromLocal(s.toString());//每次文本变化就去本地数据库查询匹配的城市
                mAdapter.notifyDataSetChanged();//通知更新
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cityNames);//适配器初始化
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCity_selected = mCities.get(position);//根据点击的位置获取对应的City对象
                queryWeatherFromServer();//根据点击的城市从服务器获取天气数据
            }
        });

    }

    //从服务器获取天气数据
    private void queryWeatherFromServer() {
        String address = "https://api.heweather.com/x3/weather?cityid=" + mCity_selected.getCity_code() + "&key=" + WEATHER_KEY;
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                //将服务器获取的Json数据进行解析
                if (Utility.handleWeatherResponse(mEditor, response)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "数据同步失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //从服务器获取所有城市的信息
    private void queryCitiesFromServer() {
        String address = " https://api.heweather.com/x3/citylist?search=allchina&key=" + WEATHER_KEY;
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (Utility.handleProvincesResponse(coolWeatherDB, response)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            coolWeatherDB.updateDataState();
                        }
                    });
                }

            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    //从本地数据库取出相似的城市名称
    private List<City> queryCitiesFromLocal(String name) {
        List<City> cities = coolWeatherDB.loadCitiesByName(name);
        cityNames.clear();
        for (City city : cities) {
            cityNames.add(city.getCity_name_ch());
        }
        return cities;
    }



    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("正在同步数据...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }


}
