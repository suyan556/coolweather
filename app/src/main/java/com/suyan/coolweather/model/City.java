package com.suyan.coolweather.model;

/**
 * Created by Yan on 2016/9/3.
 */
public class City {
    private int id;
    private String city_name_en;
    private String city_name_ch;
    private String city_code;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity_name_en() {
        return city_name_en;
    }

    public void setCity_name_en(String city_name_en) {
        this.city_name_en = city_name_en;
    }

    public String getCity_name_ch() {
        return city_name_ch;
    }

    public void setCity_name_ch(String city_name_ch) {
        this.city_name_ch = city_name_ch;
    }

    public String getCity_code() {
        return city_code;
    }

    public void setCity_code(String city_code) {
        this.city_code = city_code;
    }
}
