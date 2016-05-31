package com.psf.sapt.wifiService;

import android.os.Handler;
import android.os.Message;

import com.psf.sapt.mainActivity;

/**
 * Created by psf on 2015/9/26.
 */
public class Connect2ServiceThread extends Thread{
    Handler handler;
    mainActivity activity;
    public boolean flag=false;
    public Connect2ServiceThread(mainActivity activity,Handler handler){
        this.handler=handler;
        this.activity=activity;
    }

    @Override
    public void run() {
        super.run();
        while(!activity.isBind&&!flag){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if(!flag){
                Message msg=Message.obtain();
                msg.what=0;
                handler.sendMessage(msg);
            }

        }
    }
}
