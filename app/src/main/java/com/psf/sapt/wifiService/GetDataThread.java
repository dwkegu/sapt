package com.psf.sapt.wifiService;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.psf.sapt.can;

/**
 * Created by psf on 2015/8/28.
 */
public class GetDataThread extends Thread {
    Handler listener;
    private boolean flag_stop=false;
    private boolean flagPort=true;
    private can[] can=new can[2];
    private int bmuNum=1;

    public void setCanPort(boolean hasLcuModule,boolean hasBmuModule,int lcuNum,int bmuNum) {
        if(this.hasBmuModule==hasBmuModule&&this.hasLcuModule==hasLcuModule&&this.lcuNum==lcuNum&&this.bmuNum==bmuNum){
            return;
        }
        this.hasLcuModule = hasLcuModule;
        this.lcuNum = lcuNum;
        this.hasBmuModule = hasBmuModule;
        this.bmuNum = bmuNum;
        flagPort=true;
    }
public void setFlagPort(){
    flagPort=true;
}

    private int lcuNum=0;
    private boolean hasBmuModule=false;
    private boolean hasLcuModule=false;
    public byte[] data=null;
    private DataProgress mpro=null;
    public GetDataThread(int lcuNum, int bmuNum, boolean hasLcu, boolean hasbmu){
        //Log.v("dataTag","connect detail is:"+lcuNum+ "   "+bmuNum+"  "+hasbmu+"  "+hasLcu);
        this.lcuNum=lcuNum;
        this.bmuNum=bmuNum;
        this.hasBmuModule=hasbmu;
        this.hasLcuModule=hasLcu;
        //如果can0和can1已经连接上，则分别打开连接。默认can[0]是lcu can[1]是bmu
        mpro=new DataProgress();
        data=new byte[2000];
    }
    public void setListener(Handler listener){
        this.listener=listener;
    }




//前期程序，此功能已经转移到sendRequestThread中
    public byte[] getBcding(){
        byte[] req=new byte[8];
        req[0]=(byte)0xAA;
        req[1]=(byte)0xAA;
        req[2]=(byte)0xAA;
        req[3]=(byte)0x00;
        req[4]=(byte)0x00;
        req[5]=(byte)0x00;
        req[6]=(byte)0x00;
        req[7]=(byte)0x00;//此处修改标定ID
        return req;
    }

    @Override
    public void run() {
        super.run();
        //Log.v("dataTag","lcu data"+" is getting");
        while (!flag_stop){
            if(flagPort){
                if(can[0]!=null){
                    can[0].close();
                }
                if(can[1]!=null){
                    can[1].close();
                }
                if(hasLcuModule){
                    can[0]=new can(lcuNum==0? 4001:4002);
                }else{
                    can[0]=null;
                }
                if(hasBmuModule){
                    can[1]=new can(bmuNum==1? 4002:4001);
                }else{
                    can[1]=null;
                }
                flagPort=false;
            }
            //Log.v("dataTag","getDataThread is running!");
            if(hasLcuModule) {
               // Log.v("dataTag", "getDataThread has lcu");
                if(can[0]==null||!can[0].isConn){
                   // Log.v("dataTag","GetDataThread can not get can0");
                }else{
                   // Log.v("dataTag", "getDataThread is getting lcu data");
                    data=new byte[2000];
                    can[0].read(data);
                    //this part is used to print the frame that received;
                    //Log.v("dataTag", "lcu:"+sb.toString());
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = data;
                    if(!listener.sendMessage(msg)){
                       // Log.v("dataTag","message don't send");
                    }
                }

            }
            if(hasBmuModule){
               // Log.v("dataTag","getDataThread has bmu");
                can[1].read(data);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 20; i++) {
                    sb.append((data[i] & 0xff) + "  ");
                }
                Log.v("dataTag", "bmu:"+sb.toString());
                Message msg=Message.obtain();
                msg.what=2;
                msg.obj=data;
                listener.sendMessage(msg);
            }
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }
        if(flag_stop){
            if(can[0]!=null){
                can[0].close();
            }
            if(can[1]!=null){
                can[1].close();
            }

        }
    }
    //停止线程的运行
    public void setFlag_stop(boolean flag_stop){
        this.flag_stop=flag_stop;
    }

}
