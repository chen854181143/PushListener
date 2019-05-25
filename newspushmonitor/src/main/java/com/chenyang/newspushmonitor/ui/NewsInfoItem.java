package com.chenyang.newspushmonitor.ui;

/**
 * Created by wangmingxing on 18-1-31.
 */

public class NewsInfoItem {
    public String packageName;
    public String newsInfo;
    public String msg;
    public String time;

    public NewsInfoItem(String packageName, String newsInfo,String msg,String time) {
        this.packageName = packageName;
        this.newsInfo = newsInfo;
        this.msg = msg;
        this.time = time;
    }
}
