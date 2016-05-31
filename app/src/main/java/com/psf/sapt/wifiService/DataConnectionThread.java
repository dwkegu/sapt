package com.psf.sapt.wifiService;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.psf.sapt.can;

/**
 * Created by psf on 2015/11/16.
 */
public class DataConnectionThread extends Thread {
    boolean flagStop=false;
    boolean flagRefresh=false;
    can lecuCan=null;
    Handler mHandler=null;
    boolean isReadable=false;
    int sendTimes=5;
    int port=4001;
    int stdID=0x100;
    byte[] send1=null;
    boolean isScan=false;
    int scanTimes=0;

    public void setSendRequest(boolean sendRequest) {
        this.sendRequest = sendRequest;
    }

    boolean sendRequest=false;


    int sendExtra=0;
    int ExtraID=0x6ff;
    byte[] ExtraData=null;


    public DataConnectionThread(int port,int sendTimes){
        this.port=port;
        this.sendTimes=sendTimes;
        stdID=0x100;
        send1=getRequstForLcuExtra(1);
        sendExtra=0;
        isScan=false;
        scanTimes=0;
        sendRequest=false;
    }
    public void setmHandler(Handler handler){
        mHandler=handler;
    }
    public void setIsScan(int scanTimes){
        isScan=true;
        this.scanTimes=scanTimes;
    }

    /**
     * 请求LECU的冗余帧
     * @param type 默认为1
     * @return 请求LECU需要发送的数据
     */
    public static byte[] getRequstForLcuExtra(int type){
        byte[] req=null;
        switch (type){
            case 1:
                req=new byte[3];
                req[0]=(byte)0x00;
                req[1]=(byte)0x00;
                req[2]=(byte)0x20;
                break;
        }

        return req;
    }

    /**
     * 用于向指定LECU模块发送数据，主要用于标定
     * @param id    模块ID
     * @param data  要发送的数据
     * @param times 发送多少次
     */
    public void setExtraRequest(int id,byte[] data,int times){
        this.ExtraID=id;
        this.ExtraData=data;
        this.sendExtra=times;
    }
    int biaodingKind=0;

    /**
     * 由于模块有很多种类型，每种的标定对不一样，用于返回标定结果的辅助标识
     * 需要进行设置
     * @param kind
     */
    public void setSendResultKind(int kind){
        biaodingKind=kind;
    }
    public void setFlagStop(){
        flagStop=true;
    }
    public void setFlagRefresh(int canport){
        flagRefresh=true;
        this.canport=canport;
    }
    int canport;
    @Override
    public void run() {
        super.run();
        int leftSendTimes=sendTimes;
        lecuCan=new can(4001);
        while(!flagStop){
            if(flagRefresh){
                if(lecuCan!=null){
                    lecuCan.close();
                    lecuCan=null;
                }
                Log.v("dataTag","new Can");
                lecuCan=new can(canport);

                flagRefresh=false;
            }
            if(lecuCan==null||!lecuCan.isConn){
                //连接失败
                Log.v("dataTag","disconnected");
                Message msg=Message.obtain();
                msg.what=6;
                mHandler.sendMessage(msg);
            }else{
                //用于发送标定指令
                CanService.failedConnect=0;
                if(sendExtra>0){
                    lecuCan.write(ExtraID,ExtraData.length,ExtraData);
                    sendExtra--;
                    Message msg=Message.obtain();
                    msg.what=4;
                    msg.arg1=biaodingKind;
                    mHandler.sendMessage(msg);
                }
                //读取数据
                if(isReadable){
                    int aviable=lecuCan.getAviable();
                    aviable=aviable-(aviable%20);
                    //Log.v("dataTag", "可读数据："+aviable);
                    if(aviable>0){
                        byte[] data=new byte[aviable];
                        lecuCan.read(data);
                        Message msg=Message.obtain();
                        msg.obj=data;
                        if(isScan&&scanTimes>0){
                            msg.what=3;
                            scanTimes--;
                            msg.arg1=scanTimes;
                        }else{
                            if(scanTimes==0){
                                isScan=false;
                            }
                            msg.what=1;
                        }
                        mHandler.sendMessage(msg);
                    }else{
                        //无数据报告
                        Message msg=Message.obtain();
                        msg.what=5;
                        mHandler.sendMessage(msg);
                    }
                    isReadable=false;
                    leftSendTimes=sendTimes;

                }else{//发送冗余帧请求
                    if(leftSendTimes>0&&sendRequest){
                        lecuCan.write(stdID,send1.length,send1);
                        leftSendTimes--;
                    }else{
                        isReadable=true;
                    }
                }
            }
            try {
                Thread.sleep(90);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        if(flagStop){
            lecuCan.close();
        }
    }
}
