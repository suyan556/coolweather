package com.suyan.coolweather.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.suyan.coolweather.db.CoolWeatherOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yan on 2016/9/3.
 */
public class CoolWeatherDB {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "cool_weather";

    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;

    private SQLiteDatabase db;

    /**
     * 将构造方法私有化
     */
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 获取CoolWeatherDB
     */
    public  synchronized  static CoolWeatherDB getInstance(Context context){
        if(coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    //保存一个城市对象数据到数据库
    public void saveCity(City city){
        if(city != null){
            ContentValues values = new ContentValues();
            values.put("CITY_NAME_EN", city.getCity_name_en());
            values.put("CITY_NAME_CH", city.getCity_name_ch());
            values.put("CITY_CODE", city.getCity_code());
            db.insert("CITY", null, values);
        }
    }


    /**
     * 从数据库读取所有城市的信息
     */
    public List<City> loadCities(){
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("CITY", null, null, null,null,null, null);
        if (cursor.moveToFirst()){
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("ID")));
                city.setCity_name_ch(cursor.getString(cursor.getColumnIndex("CITY_NAME_EN")));
                city.setCity_name_en(cursor.getString(cursor.getColumnIndex("CITY_NAME_CH")));
                city.setCity_code(cursor.getString(cursor.getColumnIndex("CITY_CODE")));
                list.add(city);
            }while (cursor.moveToNext());
        }
        if(cursor != null){
            cursor.close();
        }
        return list;
    }



    //根据名称获取某一个或多个匹配的城市
    public List<City> loadCitiesByName(String name) {

        List<City> cities = new ArrayList<>();
        Cursor cursor = db.query("CITY", null, "CITY_NAME_CH like ?", new String[]{name + "%"}, null, null, "CITY_CODE");
        while (cursor.moveToNext()) {
            City city = new City();
            city.setId(cursor.getInt(cursor.getColumnIndex("ID")));
            city.setCity_name_en(cursor.getString(cursor.getColumnIndex("CITY_NAME_EN")));
            city.setCity_name_ch(cursor.getString(cursor.getColumnIndex("CITY_NAME_CH")));
            city.setCity_code(cursor.getString(cursor.getColumnIndex("CITY_CODE")));
            cities.add(city);
        }
        if (cursor != null)
            cursor.close();
        return cities;

    }



   //检查是否是第一次安装（0-是 1-否）
    public int checkDataState() {
        int data_state = -1;
        Cursor cursor = db.query("data_state", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                data_state = cursor.getInt(cursor.getColumnIndex("STATE"));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();

        return data_state;
    }

    /**
     *更新状态为已有数据
     */
    public void updateDataState(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("state", 1);
        db.update("data_state", contentValues, null, null);
    }

}
