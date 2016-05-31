package com.psf.sapt.wifiService;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.psf.sapt.can;

import java.util.ArrayList;

/**
 * Created by psf on 2015/9/10.
 */
public class Scanner extends Thread {
    private can[] can=null;
    private DataProgress mpro=new DataProgress();
    public int lcuNum=0;
    public int bmuNum=1;
    public boolean hasLcuModule=false;
    public boolean hasBmuModule=false;
    public int[] modules=null;
    private int timeLength=500;
    private int delay=0;
    private int type=0;
    private int scannedModule=1;
    private int detailVoltNum=0;
    private int detailTempNum=0;
    //用于通知扫描完成
    private Handler mhandle;
    public Scanner(Handler handler){
        mhandle=handler;
    }

    /**
     * 设置扫描选项
     * @param delay 扫描开始的延迟时间
     * @param timeLength    扫描持续的时间，包括延迟的时间
     * @param type      扫描的类型，0,默认类型，1扫描scannedModule的单体电压和温度路数
     */
    public void setScannerTime(int delay,int timeLength,int type){
        this.timeLength=timeLength;
        this.delay=delay;
        this.type=type;
    }
    public void setScanModule(int module){
        this.scannedModule=module;
        detailTempNum=0;
        detailVoltNum=0;
    }
    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(delay);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        Log.v("dataTag", "scanner");
        if(can==null){
            can=new can[2];
           // Log.v("dataTag","is new can0");
            can[0]=new can(4001);
           // Log.v("dataTag","new can0");
            can[1]=new can(4002);
        }
        //对模块连接的can总线数量和类型进行数据截获分析
        ArrayList<Integer> idsList=new ArrayList<>();
        ArrayList<Integer> detailList=new ArrayList<>();
        long time1=System.currentTimeMillis();
        byte[] data=new byte[4000];
        while(System.currentTimeMillis()-time1<timeLength){
            if(can[0]!=null&&can[0].isConn){
                if(!can[0].read(data)){
                    hasLcuModule=false;
                }else{
                    mpro.setFrames(data);
                    for(int i=0;i<200;i++){
                        if(mpro.getID(mpro.getFrame(i))>=0x110){
                            lcuNum=0;
                            hasLcuModule=true;
                            int id=mpro.getID(mpro.getFrame(i));
                            id=(id&0xff0)-0x100>>4;
                            Log.v("dataTag","lcu id："+id);
                            if(!idsList.contains(id)){
                                idsList.add(id);
                            }
                        }else{
                            Log.v("dataTag","lcu not connected");
                            /*
                            bmuNum=0;
                            hasBmuModule=true;
                            if(!idsList.contains(0)){
                                idsList.add(0);
                            }
                            */
                        }
                    }
                    mpro.setAllowSetNewData();
                }
            }else{
             //   Log.v("dataTag","no can0");
                hasLcuModule=false;
            }
            if(can[1]!=null&&can[1].isConn){
                if(!can[1].read(data)){
                    hasBmuModule=false;
                }else{
                    mpro.setFrames(data);
                    if(mpro.getID(data)>=0x110){
                        lcuNum=0;
                        hasLcuModule=true;
                        int id=mpro.getID(data);
                        id=(id&0xff0)-0x100>>4;
                        if(!idsList.contains(id)){
                            idsList.add(id);
                        }
                    }else{
                        hasBmuModule=true;
                        bmuNum=1;
                        hasBmuModule=true;
                        if(!idsList.contains(0)){
                            idsList.add(0);
                        }
                    }
                    mpro.setAllowSetNewData();
                }
            }else{
              //  Log.v("dataTag","no bmu");
                hasBmuModule=false;
            }
        }
        if(can[0]!=null){
            can[0].close();
        }
        if(can[1]!=null){
            can[1].close();
        }
        modules=new int[idsList.size()];
        for(int i=0;i<idsList.size();i++){
            modules[i]=idsList.get(i);
        }
        Message msg=Message.obtain();
        msg.what=0;
        msg.obj=modules;
        msg.arg1=detailVoltNum;
        msg.arg2=detailTempNum;
        Log.v("dataTag","scanner send message");
        mhandle.sendMessage(msg);
    }
}
