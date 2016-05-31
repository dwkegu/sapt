package com.psf.sapt.wifiService;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.psf.sapt.PreferencesString;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by psf on 2016/1/15.
 */
public class bmuHistorySendMessage extends Thread {
    String path;
    Handler handler;
    private boolean stopFlag=false;
    FileInputStream fis;
    public bmuHistorySendMessage(String path, Handler handler){
        this.path=path;
        this.handler=handler;
    }
    public void stopThread(){
        stopFlag=true;
    }
    private int aviable=0;
    @Override
    public void run() {
        super.run();
        try{
            fis=new FileInputStream(Environment.getExternalStorageDirectory()+ PreferencesString.BMUDATASAVESTRING
                    +path);
            aviable=fis.available();
        }catch (IOException e){
            e.printStackTrace();
        }
        byte[] data=new byte[400];
        while(!stopFlag){
            try{
                if(aviable>400){
                    fis.read(data);
                    Message msg=Message.obtain();
                    msg.obj=data;
                    handler.sendMessage(msg);
                    aviable-=400;
                }else if(aviable>20){
                    byte[] data1=new byte[aviable];
                    fis.read(data1);
                    Message msg=Message.obtain();
                    msg.what=0;
                    msg.obj=data1;
                    handler.sendMessage(msg);
                    aviable=0;
                }else{
                    stopFlag=true;
                }
                Thread.sleep(200);
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }

        }
        try {
            fis.close();
        }catch (IOException e){
            e.printStackTrace();
        }


    }
}
