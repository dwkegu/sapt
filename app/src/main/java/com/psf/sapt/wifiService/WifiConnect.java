package com.psf.sapt.wifiService;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.psf.sapt.can;
import com.psf.sapt.R;


/**
 * @author 彭顺风
 * @version 1.0
 * Created by psf on 2015/8/22.
 */

/**
 * 此类可以对当前wifi连接状况进行一些管理和查询，判断模块的连接情况，
 * 如果连接模块可以获取can端口实例，进行一些读写数据操作。
 */
public class WifiConnect {
    WifiManager manager;
    boolean wifiOpen;
    Context context;
    //当连接wifi模块后可以将CAN总线返回给用户
    public WifiConnect(Context context){
        init(context);
    }

    void init(Context context){
        this.context=context;
        manager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if(manager.getWifiState()==WifiManager.WIFI_STATE_DISABLED){
            wifiOpen=false;
        }else if(manager.getWifiState()==WifiManager.WIFI_STATE_DISABLING){
            wifiOpen=false;
        }else if(manager.getWifiState()==WifiManager.WIFI_STATE_UNKNOWN){
            wifiOpen=false;
        }else{
            wifiOpen=true;
        }

    }

    /**
     * 当前的wifi是否可用
     * @return 若可用返回true,否则返回false
     */
    public boolean isWifiAvailable(){
        if(manager.getWifiState()==WifiManager.WIFI_STATE_ENABLED){
            return true;
        }
        return false;
    }

    /**
     * 查询当前是否连接到wifi模块
     * @return 如果连接到返回true，否则返回false
     */
    public boolean isConnected2Module(){
        if(isWifiAvailable()){
            Log.v("TestTag",manager.getConnectionInfo().getSSID()+"  "+context.getResources().getString(R.string.module_id));
            WifiInfo wifiInfo=manager.getConnectionInfo();
            if(wifiInfo.getSSID().length()>13){
                if(wifiInfo.getSSID().substring(1,13).equals(context.getResources().getString(R.string.module_id)))
                    return true;
            }

        }
        Log.v("TestTag","wifi无法使用");
        return false;
    }







}
