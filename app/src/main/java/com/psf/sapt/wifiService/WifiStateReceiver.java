package com.psf.sapt.wifiService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

/**
 * Created by psf on 2015/8/31.
 */
public class WifiStateReceiver extends BroadcastReceiver {
    private static int wifiDisconnectedNum=0;
    //private WifiManager manager;
    Context context;
    WifiStateReceiver receiver;
    CanService service;
    public WifiStateReceiver(Context context,CanService service){
        this.context=context;
        receiver=this;
        this.service=service;
    }
    public void registerAction(String[] action){
        IntentFilter filter=new IntentFilter();
        for(int i=0;i<action.length;i++){
            filter.addAction(action[i]);
        }
        context.registerReceiver(receiver, filter);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
            NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info==null){
                Toast.makeText(this.context,"未连接wifi",Toast.LENGTH_SHORT).show();
            }
            /*else{
                //Toast.makeText(this.context,String.valueOf(info.isConnected()),Toast.LENGTH_SHORT).show();
                if(info.isConnected()){
                    service.refreshCan2();
                }
            }*/
        }
    }
}
