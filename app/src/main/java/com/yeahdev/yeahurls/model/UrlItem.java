package com.yeahdev.yeahurls.model;

import java.sql.Time;
import java.util.Date;

public class UrlItem {
    private int id;
    private String value;
    private String keywords;
    private String date;
    private String time;
    private String timestamp;
    private String objId;

    public UrlItem() {}

    public UrlItem(int id, String value, String keywords, String date, String time, String timestamp, String objId) {
        this.id = id;
        this.value = value;
        this.keywords = keywords;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.objId = objId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }
}
