package com.psf.sapt.wifiService;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.psf.sapt.ApplicationTime;
import com.psf.sapt.PreferencesString;
import com.psf.sapt.can;
import com.psf.sapt.mainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jxl.Cell;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 核心服务
 * Created by psf on 2015/9/9.
 */
public class CanService extends Service {
    //can总线的连接情况
    private boolean hasLcuModule=false;
    private boolean hasBmuModule=false;
    private int lcuNum=0;
    private int bmuNum=1;
    public boolean hasStartGetData=false;
    //扫描can连接的网络模块情况
    private Scanner mscan=null;
    private boolean hasStartScanner=false;
    private int detailVoltNum=0;
    private int detailTempNum=0;
    //lcu的模块情况
    private int[] lcuModulesId=null;
    private ArrayList<Integer> moduleIdsList=null;
    private int[] LcuTempNum=null;
    private final IBinder mbinder=new LocalBind();
    //wifi连接情况
    private WifiConnect wconn=null;
    private can mcan=null;
    DataConnectionThread dataThead=null;

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    //统计容量
    private double batteryCapacity=0;
    private long lastTime=0;
    private float lastCurrent=0f;

    public boolean isConnect2Module() {
        Connect2Module=wconn.isConnected2Module();
        return Connect2Module;
    }

    //是否连接上can转wifi模块
    public boolean Connect2Module=false;
    private GetDataThread mThread=null;
    //为储存数据预留
    private WritableWorkbook bookBmu=null;
    private WritableWorkbook booklcu=null;
    private WritableSheet lecuSheet=null;
    private byte[] data;
    DataProgress mprogress;
    private int[] lastDataNum=null;
    private int whichBoard=0;
    //储存数据的进度
    private float dataSavingProgress=0;



    //对于绑定服务的客户端提供数据返回接口
    private OnDataUpdateListener mlistener;

    public boolean ContainId(int position){
        if(lcuModulesId!=null){
            if(position>41||position<=0){
                return false;
            }
            for(int i=0;i<lcuModulesId.length;i++){
                if(lcuModulesId[i]==position){
                    return true;
                }
            }
        }

        return false;
    }
    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.v("dataTag",String.valueOf(msg.what));
            switch (msg.what){
                case 0://原来老的扫描方式，容易导致掉线，已经弃用
                    hasBmuModule=mscan.hasBmuModule;
                    hasLcuModule=mscan.hasLcuModule;
                    lcuNum=mscan.lcuNum;
                    bmuNum=mscan.bmuNum;
                    /*
                    if(hasLcuModule){
                        mcan=new can(lcuNum==0? 4001:4002);
                    }
                    */
                    if(mThread!=null){
                        mThread.setCanPort(hasLcuModule,hasBmuModule,lcuNum,bmuNum);
                    }

                    Log.v("dataTag","scanner detail"+hasLcuModule+" "+hasBmuModule+"  "+lcuNum+"  "+bmuNum);
                    //扫描结果
                    lcuModulesId=(int[])msg.obj;
                    detailVoltNum=msg.arg1;
                    detailTempNum=msg.arg2;
                   // Log.v("dataTag", "message: " + lcuModulesId[0]);
                    hasStartScanner=false;
                    mThread=new GetDataThread(lcuNum,bmuNum,hasLcuModule,hasBmuModule);
                    mThread.setListener(mHandler);
                    mThread.start();
                    mlistener.OnScanCompleted();
                    break;
                case 1:data=(byte[])msg.obj;

                    if(hasBmuModule){
                        mprogress.setFrames(data);
                        int [] ids=mprogress.getId();
                        for(int i=0;i<ids.length;i++){
                            if(ids[i]==0x411){
                                float currentTemp=mprogress.getFrameFloatData(i)[0];
                                long timeTemp=ApplicationTime.getTime();
                                if(lastCurrent==0){

                                    batteryCapacity+=currentTemp*(timeTemp-lastTime)/1000f;
                                }else{
                                    batteryCapacity+=(currentTemp+lastCurrent)/2*(timeTemp-lastTime)/1000f;
                                }
                                lastCurrent=mprogress.getFrameFloatData(i)[0];
                                lastTime=timeTemp;
                                break;
                            }
                        }
                    }
                    //进行LECU数据储存
                    if(hasStartSavingData&&!pauseSaveData){
                        saveDataInhandle();
                    }
                    //进行BMU数据储存
                    if(bmuSaveData){
                        saveBmuDataInHandle();
                    }

                    mlistener.OnDataUpdate(data);
                    noData=0;
                    break;
                case 2:data=(byte[])msg.obj;
                    mlistener.OnDataUpdate(data);
                    noData=0;
                    break;
                //新的模块扫描方式
                case 3:
                    data=(byte[])msg.obj;
                    mprogress.setFrames(data);
                    int[] ids=mprogress.getPureIDs();
                    for(int i=0;i<ids.length;i++){
                        if(ids[i]>=0&&ids[i]<41&&!moduleIdsList.contains(ids[i])){
                            moduleIdsList.add(ids[i]);
                        }
                    }

                    if(moduleIdsList.contains(0)){
                        if(!hasBmuModule){
                            batteryCapacity=0;
                            lastTime= ApplicationTime.getTime();
                        }
                        hasBmuModule=true;
                    }else{
                        hasBmuModule=false;
                    }
                    mprogress.setAllowSetNewData();
                    int[] lcuModulesIdT=new int[moduleIdsList.size()];
                    lcuModulesId=new int[moduleIdsList.size()];
                    for(int i=0;i<moduleIdsList.size();i++){
                        lcuModulesIdT[i]=moduleIdsList.get(i);
                    }
                    if(lcuModulesIdT.length>0) {
                        int min = 0;
                        for (int i = 0; i < lcuModulesIdT.length; i++) {
                            min = lcuModulesIdT[i];
                            for (int j = i; j < lcuModulesIdT.length; j++) {
                                if (lcuModulesIdT[j] < min) {
                                    int temp = min;
                                    min = lcuModulesIdT[j];

                                    lcuModulesIdT[j] = temp;
                                }
                            }
                            lcuModulesId[i] = min;
                        }
                    }
                    if(msg.arg1==0){
                        mlistener.OnScanCompleted();
                        isScanModule=false;
                        noData=0;
                    }
                    break;
                case 4:
                    mlistener.OnSendResult(msg.arg1);
                    break;
                case 5:
                    noData++;
                    if(noData>60){
                        //表示没连接
                        noData=0;
                        refreshCan2();
                        Toast.makeText(getApplicationContext(),"无连接",Toast.LENGTH_SHORT).show();
                    }

                    break;
                case 6:
                    if(isScanModule){
                        lcuModulesId=new int[0];
                        isScanModule=false;
                    }
                    noData=0;
                    failedConnect++;
                    mlistener.OnScanCompleted();
                    if(failedConnect>60){
                        refreshCan2();
                        failedConnect=0;
                    }
                    break;
            }
            return true;
        }
    });
public static int failedConnect=0;
    public static int noData=0;


    public int getLcuModulePosetion(int moduleID){
        for(int i=0;i<lcuModulesId.length;i++){
            if(lcuModulesId[i]==moduleID){
                if(lcuModulesId[0]==0){
                    return i-1;
                }else {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wconn=new WifiConnect(getApplicationContext());
        Connect2Module=wconn.isConnected2Module();
        mprogress=new DataProgress();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }
    public class LocalBind extends Binder{
        public CanService getLocalService() {
            return CanService.this;
        }
    }

    /**
     * 设置监听can数据获取
     * @param listener 实现监听的接口
     */
    public void setListener(OnDataUpdateListener listener,String from){
        Log.v("dataTag","listener from "+from);
        mlistener=listener;
    }
    //扫描当前的连接，获取can的连接情况。
    public void getCanConnectDetail(){
        if(hasStartSavingData){
            return;
        }
        if(hasStartGetLcuExtraData){
            stopGetLcuExtraData();
        }
        if(hasStartGetData){
            stopGetData();
        }
        if(!hasStartScanner){
            if(mcan!=null){
                mcan.close();
            }
            hasStartScanner=true;
            mscan=new Scanner(mHandler);
            mscan.start();
        }

    }
boolean isScanModule=false;
    /**
     * id标定后扫描连接情况
     * @param delay
     * @param timeLength
     */
    public boolean getCanConnectDetail(int delay,int timeLength){
        //如果在储存数据，不允许扫描模块
        if(hasStartSavingData){
            return false;
        }
        //清空moduleIdList
        if(moduleIdsList==null){
            moduleIdsList=new ArrayList<>();
        }
        moduleIdsList.clear();
        if(hasStartGetData){
            dataThead.setmHandler(mHandler);
            dataThead.setIsScan(5);
            isScanModule=true;
            lcuModulesId=null;

        }else{
            dataThead=new DataConnectionThread(4001,3);
            dataThead.setmHandler(mHandler);
            dataThead.setIsScan(5);
            dataThead.start();
            hasStartGetData=true;
            isScanModule=true;
            lcuModulesId=null;
        }
        return true;
    }

    /**
     * 扫描模块的温度路数和电压路数
     * @param delay
     * @param timeLength
     * @param module
     */
    public void getModuleDetail(int delay,int timeLength,int module){
        mscan=new Scanner(mHandler);
        mscan.setScannerTime(delay, timeLength, 1);
        mscan.setScanModule(module);
        mscan.start();
    }

    /**
     * 获取扫描路数结束的数据
     * @return
     */
    public int[] getDetail(){
        int[] detail=new int[2];
        detail[0]=detailVoltNum;
        detail[1]=detailTempNum;
        Log.v("dataTag", "lecu detail" + detail[0] + "  " + detail[1]);
        return detail;
    }
    //在服务中开启数据获取线程
    public void getData(){
        Log.v("dataTag","start get data");
        hasStartGetData=true;
        mThread=new GetDataThread(lcuNum,bmuNum,hasLcuModule,hasBmuModule);
        mThread.setListener(mHandler);
        mThread.start();
    }
    public void getData2(){
        if(dataThead==null||!hasStartGetData){
            hasStartGetData=true;
            dataThead=new DataConnectionThread(4001,3);
            dataThead.setmHandler(mHandler);
            dataThead.start();
        }
    }

    public boolean hasStartGetLcuExtraData=false;
    //获取Lcu冗余帧数据

    /**
     * 获取冗余帧，如果保存数据的服务开启了，并且服务已经启动了发送线程，
     * 则该api不做任何处理，否则启动新的线程
     */
    public void getLcuExtraData(){
        if(hasStartGetLcuExtraData){
            return;
        }
        /*
        sendThread=new SendRequestThread();
        sendThread.setRequestParams(200, SendRequestThread.REQUEST_TYPE_TWO, lcuNum == 0 ? 4001 : 4002
                , SendRequestThread.getRequstForLcuExtra(1), 0x100);
        sendThread.start();
        hasStartGetLcuExtraData=true;
        */
        dataThead.setSendRequest(true);
        hasStartGetLcuExtraData=true;
    }




    public void refreshCan(){
        if(hasStartGetLcuExtraData){
            stopGetLcuExtraData();
            getLcuExtraData();
        }
        if(hasStartGetData){
            stopGetData();
            getData();
        }



    }
    mainActivity hostActivity=null;
    public void setHostActivity(mainActivity mainActivity){
        hostActivity=mainActivity;
    }
boolean is4001=true;
    public void refreshCan2(){
        Log.v("dataTag","refrehing");
        if(!isConnect2Module()){
            stopGetLcuExtraData();
            stopGetData2();
            return;
        }
        if(hasStartGetData){
            if(is4001){
                dataThead.setFlagRefresh(4002);
                hostActivity.canPort.setText("can2");
                is4001=false;
            }else {
                dataThead.setFlagRefresh(4001);
                hostActivity.canPort.setText("can1");
                is4001=true;
            }

        }
    }
    //获取lcu模块id

    public int[] getLcuModuleIDs(){
        return lcuModulesId;

    }

    /**
     *获取当前数据温度路数,一般用于标定后的查询是否成功，所以建议开启异步任务AsyncTask，去获取更改情况。
     * @param moduleId 要查询的模块
     * @return 返回当前数据包含的温度路数，如果不是LCU数据，则返回-1，是的话返回路数
     */
    public int getLcuTempNum(int moduleId){
        int num=0;
        //add you code to analyse the LCU with the given id temp num
        mprogress.setFrames(data);
        num=mprogress.getLcuTempNum(moduleId);
        return num;
    }


    //获取bmu的冗余帧
    public void getBmuExtraData(){
        //////////////////////////////////////待实现


    }
    //停止发送lcu冗余帧请求

    /**
     *停止获取冗余帧，如果储存数据的已开启，则不做任何操作，
     * 否则停止获取冗余帧
     */
    public void stopGetLcuExtraData(){
        /*
        if(sendThread!=null){
            Log.v("dataTag","stop getExtraData");
            if(hasStartSavingData){
                return;
            }
            sendThread.stopSendRequest();
            hasStartGetLcuExtraData=false;
            sendThread=null;
        }
        */
        if(hasStartGetLcuExtraData){
            dataThead.setSendRequest(false);
        }

    }

    /**
     * 设定lcu模块的id
     * @param oldid     原来的id
     * @param newid     新的id
     * @param save      是否保存，如果保存需要写入flash重启
     */
    public void setLcuId(int oldid,int newid,boolean save){
        byte[] sendData=new byte[8];
        sendData[0]=(byte)0xff;
        sendData[1]=(save? (byte)0xf0:(byte)0x0a);
        sendData[2]=(byte)0xe0;
        sendData[3]=(byte)(save? (newid&0xff):(oldid&0xff));
        sendData[4]=(byte)0x00;
        sendData[5]=(byte)0x00;
        sendData[6]=(byte)0x00;
        sendData[7]=(save? (byte)0x00:(byte)(newid&0xff));

        if(hasStartGetData){
            dataThead.setmHandler(mHandler);
            dataThead.setSendResultKind(save ? 1 : 0);
            dataThead.setExtraRequest(0x6ff, sendData ,5);
        }

        /*
        if(hasStartGetLcuExtraData){
            Log.v("dataTag", "hasStartGetExtraData");
            sendThread.setHandler(mHandler);
            sendThread.setItem(save? 1:0);
            sendThread.setExtraRequest(10, sendData, 0x6ff);
        }else{
            Log.v("dataTag","don't hasStartGetExtraData");
            hasStartGetLcuExtraData=true;
            sendThread=new SendRequestThread();
            sendThread.setHandler(mHandler);
            sendThread.setItem(save ? 1 : 0);
            sendThread.setRequestParams(200, SendRequestThread.REQUEST_TYPE_TWO, lcuNum == 0 ? 4001 : 4002
                    , SendRequestThread.getRequstForLcuExtra(1), 0x100);
            sendThread.setExtraRequest(10, sendData, 0x6ff);
            sendThread.start();
        }
        */



    }

    /**
     *BMU id 标定
     * @param id    新的BMU id
     */
    public void setBmuId(int id){
        byte[] sendData=new byte[8];
        sendData[0]=(byte)0xAA;
        sendData[1]=(byte)0x21;
        sendData[2]=(byte)0xAA;
        sendData[3]=(byte)0xAA;
        sendData[4]=(byte)0x00;
        sendData[5]=(byte)0x00;
        sendData[6]=(byte)0x00;
        sendData[7]=(byte)(0xff&id);
        if(hasStartGetData||!bmuSaveData){
            dataThead.setmHandler(mHandler);
            dataThead.setSendResultKind(3);
            dataThead.setExtraRequest(0x005, sendData ,5);
        }
    }
    /**
     * 标定温度和电压路数
     * @param id        要标定的模块ID
     * @param temp      温度路数
     * @param volt      电压路数
     * @param save      是否保存，如果保存的话需要写入flash，重启
     */
    public void setLcuTandV(int id,int temp,int volt,boolean save,int biaodingNum){
        Log.v("dataTag","温度路数"+temp);
        byte[] sendData=new byte[8];
        sendData[0]=(byte)0xff;
        sendData[1]=(byte)(save? 0xf1:0x0c);
        sendData[2]=(byte)0xe0;
        sendData[3]=(byte)(id&0xff);
        int[] b=new int[3];
        b[0]=0;
        b[1]=0;
        b[2]=0;
        //FF 0C E0 0A 01 00 03 FF
        if(biaodingNum==0){
            if(volt<9){
                for(int i=0;i<volt;i++){
                    b[0]=(b[0]<<1)+1;
                }
            }else if(volt==9){
                for(int i=0;i<volt;i++){
                    b[0]=(b[0]<<1)+1;
                }
                b[1]=0x01;
            } else if(volt>9&&volt<14){
                b[0]=0xff;
                for(int i=0;i<volt-5;i++){
                    b[1]=(b[1]<<1)+1;
                }
                b[1]=b[1]&0xf1;
            }else if(volt>13&&volt<19) {
                b[0]=0xff;
                b[1]=0xf1;
                for(int i=0;i<volt-13;i++){
                    b[2]=(b[2]<<1)+1;
                }
            }
        }else if(biaodingNum==1){
            if(volt<9){
                for(int i=0;i<volt;i++){
                    b[0]=(b[0]<<1)+1;
                }
            }else if(volt>8&&volt<13){
                b[0]=0xff;
                for(int i=0;i<volt-4;i++){
                    b[1]=(b[1]<<1)+1;
                }
                b[1]=b[1]&0xf0;
            }else if(volt>12){
                b[0]=0xff;
                b[1]=0xf0;
                for(int i=0;i<volt-12;i++){
                    b[2]=(b[2]<<1)+1;
                }
            }
        } else if(biaodingNum==2){
            if(volt>8){
                for(int i=0;i<volt-8;i++){
                    b[1]=(b[1]<<1)+1;
                }
            }
            for(int i=0;i<volt;i++){
                b[0]=(b[0]<<1)+1;
            }
        }else if(biaodingNum==3){
            if(volt>15){
                for(int i=0;i<volt-15;i++){
                    b[2]=(b[2]<<1)+1;
                }
                b[1]=0xf7;
                b[0]=0xff;
            }else if(volt>11){
                for(int i=0;i<volt-7;i++){
                    b[1]=(b[1]<<1)+1;
                }
                b[1]=b[1]&0xf7;
                b[0]=0xff;
            }else if(volt>8){
                for(int i=0;i<volt-8;i++){
                    b[1]=(b[1]<<1)+1;
                }
                b[0]=0xff;
            }else{
                for(int i=0;i<volt;i++){
                    b[0]=(b[0]<<1)+1;
                }
            }
        }

        sendData[4]=(byte)(save? 0x00:(temp&0xff));
        sendData[5]=(byte)(save? 0x00:(b[2]&0xff));
        sendData[6]=(byte)(save? 0x00:(b[1]&0xff));
        sendData[7]=(byte)(save? 0x00:(b[0]&0xff));
        if(hasStartGetData){
            dataThead.setmHandler(mHandler);
            dataThead.setSendResultKind(save? 2:0);
            dataThead.setExtraRequest(0x6ff,sendData,10);
        }
        /*
        if(hasStartGetLcuExtraData) {
            Log.v("dataTag", "hasStartGetExtraData");
            sendThread.setHandler(mHandler);
            sendThread.setItem(save? 2:0);
            sendThread.setExtraRequest(10, sendData, 0x6ff);
        }else{
            Log.v("dataTag","don't hasStartGetExtraData");
            hasStartGetLcuExtraData=true;
            sendThread=new SendRequestThread();
            sendThread.setHandler(mHandler);
            sendThread.setItem(save ? 2 : 0);
            sendThread.setRequestParams(200, SendRequestThread.REQUEST_TYPE_TWO, lcuNum == 0 ? 4001 : 4002
                    , SendRequestThread.getRequstForLcuExtra(1), 0x100);
            sendThread.setExtraRequest(10,sendData,0x6ff);
            sendThread.start();
        }
        */
    }



    //停止数据获取线程
    public void stopGetData(){
        /*
        if(mThread!=null){
            mThread.setFlag_stop(true);
            mThread=null;
        }
        */
        if(hasStartGetData){
            dataThead.setFlagStop();
            hasStartGetData=false;
        }
    }
    public void stopGetData2(){
        if(hasStartGetData){
            dataThead.setFlagStop();
            dataThead=null;
            hasStartGetData=false;

        }
    }

    File dataDir=null;

    //save data flags and variable
    public boolean hasStartSavingData=false;
    private boolean pauseSaveData=false;
    private boolean bmuSaveData=false;
    private long saveTime=0;
    private long TimeStart=0;
    public boolean savingFinish=false;
    private float frequency=1000;//每秒储存几次数据
    String savingPath=null;
    OutputStream os=null;
    int eachModuleItem=18;

    
    /**
     *储存数据
     * @param time
     * @param frequency 每秒储存数据的次数
     * @return 是否初始化成功
     */
    public boolean saveData(int time,float frequency,int board){
        //如果还未扫描模块
        if(!isConnect2Module()){
            return false;
        }
        /*
        if(!hasStartGetLcuExtraData){
            sendThread=new SendRequestThread();
            sendThread.setRequestParams(1000, SendRequestThread.REQUEST_TYPE_TWO,lcuNum==0? 4001:4002
                    ,sendThread.getRequstForLcuExtra(1),0x100);
            sendThread.start();
        }
        */
        if(!hasStartGetLcuExtraData){
            if(!hasStartGetData){
                getData2();
                dataThead.setSendRequest(true);
            }else{
                dataThead.setSendRequest(true);
            }
        }
        if(lcuModulesId==null){
            return false;
        }
        if(dataDir==null){
            dataDir=new File(Environment.getExternalStorageDirectory()+ PreferencesString.DATASAVINGSTRING);
            if(!dataDir.exists()||!dataDir.isDirectory()){
                if(!dataDir.mkdirs()){
                    return false;
                }
            }
        }
        whichBoard=board;
        if(whichBoard==0){
            eachModuleItem=18;
        }else if(whichBoard==1){
            eachModuleItem=16;
        }else if(whichBoard==2){
            eachModuleItem=12;
        }else if(whichBoard==3){
            eachModuleItem=22;
        }
        if(booklcu==null){
            SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String today=df.format(new Date());
            savingPath=Environment.getExternalStorageDirectory().toString()
                    +PreferencesString.DATASAVINGSTRING;
            int num=0;
            if(dataDir.exists()){
                String[] existData=dataDir.list();
                //找出当前目录下的储存文件名
                for(String name:existData){
                    Log.v("dataTag",name);
                    if(name.length()==27){
                        String subName=name.substring(0,19);
                        if(subName.equals(today)){
                            if(Integer.parseInt(name.substring(20,23))>num){
                                num=Integer.parseInt(name.substring(20,23));
                            }
                        }
                    }
                }

            }else{
                return false;
            }
            try{
                savingPath+=(today+"_"+String.format("%03d",(num + 1))+".xls");
                os=new FileOutputStream(savingPath);
                booklcu=Workbook.createWorkbook(os);
                //创建n个sheet以记录所有的模块
                booklcu.createSheet("LECU电压",0);
                for(int i=0;i<lcuModulesId.length;i++){
                    if(lcuModulesId[i]!=0){
                        booklcu.createSheet("LECU"+lcuModulesId[i]+"T",i+1);
                        try{
                            booklcu.getSheet(0).mergeCells(eachModuleItem*i+1,0,eachModuleItem*(i+1),0);
                            booklcu.getSheet(0).addCell(new Label(1 + i * eachModuleItem, 0, "LECU" + lcuModulesId[i]));
                            booklcu.getSheet(i+1).mergeCells(1, 0, eachModuleItem, 0);
                            booklcu.getSheet(i+1).addCell(new Label(1, 0, "LECU" + lcuModulesId[i]+"T"));
                        }catch (RowsExceededException e){
                            e.printStackTrace();
                        }catch (WriteException e){
                            e.printStackTrace();
                        }
                    }
                }
                //booklcu.createSheet("test",0);
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }

        }
        if (this.lastDataNum == null) {
            this.lastDataNum = new int[82];
        }
        for (int i = 0; i < this.lastDataNum.length; i++) {
            this.lastDataNum[i] = 0;
        }

        TimeStart=System.currentTimeMillis();
        dataSavingProgress=0;
        this.frequency=1000/frequency;
        saveTime=time;
        pauseSaveData=false;
        savingFinish=false;
        hasStartSavingData=true;
        return true;
    }

    /**
     *
     * @return
     */
    public float getSavingProgress(){
        return dataSavingProgress;
    }

    /**
     *
     */
    public void stopSavingData(){
        if(booklcu!=null){
            try{
                booklcu.write();
                booklcu.close();
            }catch (IOException |WriteException e){
                e.printStackTrace();
            }

        }
        /*
        if(sendThread!=null){
            sendThread.stopSendRequest();
            hasStartGetLcuExtraData=false;
            sendThread=null;
        }
        */
        if(dataThead!=null){
            dataThead.setSendRequest(false);
            hasStartGetLcuExtraData=false;
        }
        dataSavingProgress=0;
        hasStartSavingData=false;
        pauseSaveData=false;
        booklcu=null;
        saveTime=0;

    }

    /**
     *
     */
    public void pauseSavingData(){
        if(hasStartSavingData){
            pauseSaveData=true;
        }


    }

    /**
     *
     */
    public void continueSavingData(){
        if(pauseSaveData){
            pauseSaveData=false;
        }
    }
    public static float HOURMILLIONS=10f*60*60*6;
    public void saveBmuDataInHandle(){
        bmuSaveProgress=(System.currentTimeMillis()-bmuSaveStartTime)/HOURMILLIONS;

        try{
            fos.write(data);
        }catch (IOException e){
            e.printStackTrace();
            bmuSaveData=false;
            try{
                fos.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }


        }
    }
    public void stopSaveBmuData(){
        bmuSaveData=false;
        try{
            fos.flush();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    String bmuDir=null;
    File bmuDataFile;
    FileOutputStream fos;
    public float bmuSaveProgress=0f;
    public long bmuSaveStartTime=0;
    public float getBmuSaveProgress(){
        return bmuSaveProgress;
    }

    /**
     * 发送指令得到BMUid
     * @return 是否发送成功
     */
    public boolean getBMUID(){
        if(hasStartGetData){
            if(hasStartSavingData||bmuSaveData){
                return false;
            }else{
                if(dataThead!=null){
                    byte[] sendData=new byte[8];
                    sendData[0]=(byte)0x00;
                    sendData[1]=(byte)0x00;
                    sendData[2]=(byte)0x00;
                    sendData[3]=(byte)0xAA;
                    sendData[4]=(byte)0x00;
                    sendData[5]=(byte)0x00;
                    sendData[6]=(byte)0x00;
                    sendData[7]=(byte)0x00;
                    dataThead.setmHandler(mHandler);
                    dataThead.setSendResultKind(3);
                    dataThead.setExtraRequest(0x005, sendData ,5);
                }
            }
        }else{
            return false;
        }
        return true;
    }
    public boolean saveBmuData(){
        if(bmuSaveData){
            return true;
        }
        if(bmuDataFile==null){
            bmuDataFile=new File(Environment.getExternalStorageDirectory()+ PreferencesString.BMUDATASAVESTRING);
            if(!bmuDataFile.exists()||!bmuDataFile.isDirectory()){
                if(!bmuDataFile.mkdirs()){
                    return false;
                }
            }
        }
        bmuDir=Environment.getExternalStorageDirectory()+ PreferencesString.BMUDATASAVESTRING;
        SimpleDateFormat df=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String today=df.format(new Date());
        bmuDataFile=new File(bmuDir,today);
        try{
            fos=new FileOutputStream(bmuDataFile);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        }
        bmuSaveProgress=0f;
        bmuSaveStartTime=System.currentTimeMillis();
        bmuSaveData=true;
        return true;
    }
    public boolean isSavingBmu(){
        return bmuSaveData;
    }
    public void saveDataInhandle(){
        int dataNums=(int)((System.currentTimeMillis()-TimeStart)/frequency)+1;//当前数据数
        int timeNow=(int)((System.currentTimeMillis()- TimeStart)/1000)+1;//当前时间
        if(timeNow<saveTime){
            dataSavingProgress=(float)timeNow/saveTime;
            mprogress.setFrames(data);
            float[] dataFloat=null;
            int[] dataIds= mprogress.getPureIDs();
            boolean[] hasInformation=new boolean[2];
            for(int i=0;i<dataIds.length;i++){
                if(ContainId(dataIds[i])){
                    hasInformation[0]=false;
                    hasInformation[1]=false;
                    dataFloat=mprogress.getFrameFloatData(i);
                    lecuSheet=booklcu.getSheet(0);
                    int lastDataNumOrder=dataIds[i]*2;
                    switch (mprogress.getId()[i]&0x00f){
                        case 0x004:
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new jxl.write.Number(0,
                                                    lastDataNum[lastDataNumOrder] + td + 1,
                                                    (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf = getLcuModulePosetion(dataIds[i])*eachModuleItem+1;
                                                 tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf,
                                                        lastDataNum[lastDataNumOrder] + td + 1,
                                                        Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }


                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+1,
                                                dataNums,dataFloat[j]));
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[0]=true;
                            break;
                        case 0x005:
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf = getLcuModulePosetion(dataIds[i])*eachModuleItem+1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j + 5,
                                                dataNums, dataFloat[j]));
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[0]=true;
                            break;
                        case 0x006:

                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf =getLcuModulePosetion(dataIds[i])*eachModuleItem+ 1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+9,
                                                dataNums,dataFloat[j]));
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[0]=true;
                            break;
                        case 0x007:
                            lecuSheet=booklcu.getSheet("LECU"+dataIds[i]+"T");
                            lastDataNumOrder++;
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf =1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        lecuSheet.addCell(new jxl.write.Number(j+1,dataNums,dataFloat[j]));
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[1]=true;
                            break;
                        case 0x008:
                            lastDataNumOrder++;
                            lecuSheet=booklcu.getSheet("LECU"+dataIds[i]+"T");
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf = 1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        lecuSheet.addCell(new jxl.write.Number(j+5,dataNums,dataFloat[j]));
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[1]=true;
                            break;
                        case 0x009:
                            break;
                        case 0x00a:
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf =getLcuModulePosetion(dataIds[i])*eachModuleItem+ 1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        if(whichBoard==0||whichBoard==2){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+13,dataNums,dataFloat[j]));
                                        }else if(whichBoard==1){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+9,dataNums,dataFloat[j]));
                                        }else if(whichBoard==3){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+12,dataNums,dataFloat[j]));
                                        }

                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[0]=true;
                            break;
                        case 0x00b:
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td = 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf =getLcuModulePosetion(dataIds[i])*eachModuleItem+ 1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        if(whichBoard==0||whichBoard==2){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+17,dataNums,dataFloat[j]));
                                        }else if(whichBoard==1){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+13,dataNums,dataFloat[j]));
                                        }else if(whichBoard==3){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+16,dataNums,dataFloat[j]));
                                        }
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[0]=true;
                            break;
                        case 0x00c:
                            if(dataNums-lastDataNum[lastDataNumOrder]>1) {
                                if(lastDataNum[lastDataNumOrder]==0){
                                    TimeStart=System.currentTimeMillis();
                                    dataNums=1;
                                }else{
                                    Cell[] temp1 = lecuSheet.getRow(lastDataNum[lastDataNumOrder]);
                                    for (int td =getLcuModulePosetion(dataIds[i])*eachModuleItem+ 0; td < dataNums - lastDataNum[lastDataNumOrder]-1; td++) {
                                        try {
                                            lecuSheet.addCell(new Number(0, lastDataNum[lastDataNumOrder] + td + 1, (lastDataNum[lastDataNumOrder] + td + 1) * frequency / 1000));
                                            for (int tf = 1; tf < temp1.length; tf++) {
                                                int kk=0;
                                                while(temp1[tf].getContents().equals("")){
                                                    temp1=lecuSheet.getRow(lastDataNum[lastDataNumOrder+(--kk)]);
                                                }
                                                lecuSheet.addCell(new jxl.write.Number(tf, lastDataNum[lastDataNumOrder] + td + 1, Float.parseFloat(temp1[tf].getContents())));
                                            }
                                        } catch (WriteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                            try{
                                lecuSheet.addCell(new jxl.write.Number(0,dataNums,(dataNums-1)*frequency/1000));
                            }catch (WriteException e){
                                e.printStackTrace();
                            }
                            for(int j=0;j<dataFloat.length;j++){
                                if(dataFloat[j]!=9999f){
                                    try{
                                        if(whichBoard==3){
                                            lecuSheet.addCell(new jxl.write.Number(getLcuModulePosetion(dataIds[i])*eachModuleItem+j+20,dataNums,dataFloat[j]));
                                        }
                                    }catch (WriteException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            hasInformation[0]=true;
                            break;
                        default:

                            break;
                    }
                    if(hasInformation[0]){
                        lastDataNum[lastDataNumOrder]=dataNums;
                    }
                    if(hasInformation[1]){
                        lastDataNum[lastDataNumOrder]=dataNums;
                    }
                }

            }
            mprogress.setAllowSetNewData();
        }else{
            try {
                if(booklcu!=null){
                    booklcu.write();
                    booklcu.close();
                }

            }catch (IOException e){
                e.printStackTrace();
                Log.v("dataTag","v保存数据失败！");
            }catch (WriteException e){
                e.printStackTrace();
            }finally {
                booklcu=null;
                savingFinish=true;
                hasStartSavingData=false;
                pauseSaveData=false;
            }

        }

    }
}
