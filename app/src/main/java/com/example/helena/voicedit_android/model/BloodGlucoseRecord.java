package com.example.helena.voicedit_android.model;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by helena on 2017/8/20.
 * 一条血糖记录
 */

public class BloodGlucoseRecord {
    private double insulin;
    private double blood_glucose;
    private Date date;
    private String state;

    public BloodGlucoseRecord() {}
    public BloodGlucoseRecord(double in, double bg, Date d, String s) {
        insulin = in;
        blood_glucose = bg;
        date = d;
        state = s;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getInsulin() {
        return insulin;
    }

    public void setInsulin(double insulin) {
        this.insulin = insulin;
    }

    public double getBlood_glucose() {
        return blood_glucose;
    }

    public void setBlood_glucose(double blood_glucose) {
        this.blood_glucose = blood_glucose;
    }
}
