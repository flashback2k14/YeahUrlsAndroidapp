package com.yeahdev.yeahurls.model;

public class NoteItem {
    private int id;
    private String title;
    private String value;
    private String keywords;
    private long timestamp;
    private String objId;

    public NoteItem() { }

    public NoteItem(int id, String title, String value, String keywords, long timestamp, String objId) {
        this.id = id;
        this.title = title;
        this.value = value;
        this.keywords = keywords;
        this.timestamp = timestamp;
        this.objId = objId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }
}
