package com.km.appblocker;

public class AppInfo {
    public String appName;
    public boolean isBlock;
    public String packageName;

    public AppInfo(String appName, boolean isBlock, String packageName) {
        this.appName = appName;
        this.isBlock = isBlock;
        this.packageName = packageName;
    }
}
