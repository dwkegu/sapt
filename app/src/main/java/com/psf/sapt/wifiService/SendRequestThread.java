package com.psf.sapt.wifiService;

import android.os.Handler;
import android.os.Message;

import com.psf.sapt.can;

/**
 * 已经弃用，原来用来发送请求
 * Created by psf on 2015/9/10.
 */
public class SendRequestThread extends Thread{
    public static Integer REQUEST_TYPE_ONE=1;
    public static Integer REQUEST_TYPE_TWO=2;
    private int times;
    private Integer type;
    private can canSend=null;
    private int port;
    private boolean stopFlag=false;
    private byte[] sendData=null;
    private int id;
    public static int sendRequestNum=0;
    public Handler mHandler=null;
    private boolean sendExtra=false;
    private int extraTimes=0;
    private byte[] extraData=null;
    private int extraId=0x100;
    private boolean refresh=false;

    public void setRequestParams(int times,Integer type, int port,byte[] send,int frameId){
        this.times=times;
        this.type=type;
        this.port=port;
        sendData=send;
        id=frameId;

    }

    public void setRefresh(){
        refresh=true;
    }
    /**
     *
     * @param times
     * @param send
     * @param frameId
     */
    public void setExtraRequest(int times,byte[] send,int frameId){
        this.extraTimes=times;
        this.extraData=send;
        this.extraId=frameId;
        sendExtra=true;
    }
    public void setCan(can can){
        canSend=can;
    }
    private int item=0;
    private final Integer itemSy=Integer.valueOf(1);
    public void setItem(Integer item){
        synchronized (this.itemSy){
            this.item=item;
        }
    }

    public void setHandler(Handler handler){
        mHandler=handler;
    }

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


    public byte[] setModuleId(int oldID,int newID,boolean eFlash){
        byte[] req=new byte[8];
        req[0]=(byte)0xAA;
        req[1]=(byte)0xF0;
        if(eFlash){
            req[2]=(byte)0xAA;
        }else{
            req[2]=(byte)0x00;
        }
        req[3]=(byte)0xAA;
        req[4]=(byte)0x00;
        req[5]=(byte)0x00;
        req[6]=(byte)oldID;
        req[7]=(byte)newID;

        return req;

    }
    public void stopSendRequest(){
        stopFlag=true;
    }

    @Override
    public void run() {
        super.run();
        sendRequestNum++;
        if(canSend==null){
            canSend=new can(port);
        }
        while(!stopFlag){
            if(refresh){
                canSend.close();
                try{
                    Thread.sleep(20);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                canSend=new can(port);
                refresh=false;
            }
            synchronized (type){
                if(sendExtra){
                    //Log.v("dataTag","send once data");
                    if(canSend.isConn){
                      //  Log.v("dataTag","send data once get port");
                        for(int i=0;i<extraTimes;i++){
                            canSend.write(extraId,extraData.length,extraData);
                        }
                        Message msg=Message.obtain();
                        msg.what=4;
                        msg.arg1=item;
                        mHandler.sendMessage(msg);
                        sendExtra=false;
                        extraTimes=0;
                    }
                }
                if(canSend.isConn){
                    canSend.write(id,sendData.length,sendData);
                    try{
                        Thread.sleep(90);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }

        }

        canSend.close();
        sendRequestNum--;
    }
}
