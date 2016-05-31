package com.psf.sapt;

/**
 * 用于全局域获取时间
 * Created by psf on 2016/2/17.
 */
public class ApplicationTime {
    public static long appDate=0;
    public static void start(){
        appDate=System.currentTimeMillis();
    }
    public static long getTime(){
        return System.currentTimeMillis()-appDate;
    }
}
