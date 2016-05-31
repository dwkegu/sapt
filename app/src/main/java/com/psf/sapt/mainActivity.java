package com.psf.sapt;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.psf.sapt.fragments.BalanceFragment;
import com.psf.sapt.fragments.BmuDetailFragment;
import com.psf.sapt.fragments.ButteryMatDetail;
import com.psf.sapt.fragments.ButteryMatItemDetail;
import com.psf.sapt.fragments.ButteryMatSelect;
import com.psf.sapt.fragments.ErrorRecordsFragment;
import com.psf.sapt.fragments.HistoryFragment;
import com.psf.sapt.fragments.ParamsSetFragment;
import com.psf.sapt.fragments.SavaFragment;
import com.psf.sapt.fragments.SettingFragment;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.OnDataUpdateListener;
import com.psf.sapt.wifiService.WifiStateReceiver;

import java.io.File;

/**
 * 程序运行的主程序
 * Created by psf on 2015/8/19.
 */
public class mainActivity extends AppCompatActivity implements OnDataUpdateListener{
    ListView mlistview;
    public TextView canPort=null;
    /**
     * 当前显示的Fragment
     */
    public int currentFragment=0;

    /**
     * 用于记录第一个Fragment进入LECU的状态
     */
    public int fragmentOne=0;
    /**
     * 当前连接的BMU和LECU的ID
     */
    public int[] modules=null;
    /**
     * 后台主服务的实例化对象
     */
    private CanService mservice=null;
    /**
     * 记录是否与服务绑定成功
     */
    public boolean isBind=false;

    public static float tempHigh=0;
    public static float tempLow=0;
    WifiStateReceiver receiver=null;
    //View container=null;

    public float getTempHigh() {
        return tempHigh;
    }

    public float getTempLow() {
        return tempLow;
    }

    public float getVoltHigh() {
        return voltHigh;
    }

    public float getVoltLow() {
        return voltLow;
    }

    public static float voltHigh=0;
    public static float voltLow=0;
    private ServiceConnection mconn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CanService.LocalBind mbind=(CanService.LocalBind)service;
            mservice=mbind.getLocalService();
            mservice.setHostActivity(mainActivity.this);
            isBind=true;
            if(fragment1_1!=null){
                if(fragment1_1.pd!=null){
                    fragment1_1.pd.dismiss();
                }
                mservice.setListener(mainActivity.this,"mainActivity");
            }
            receiver=new WifiStateReceiver(getApplicationContext(),mservice);
            receiver.registerAction(new String[]{
                    WifiManager.RSSI_CHANGED_ACTION,
                    WifiManager.NETWORK_STATE_CHANGED_ACTION});
            if(!mservice.Connect2Module){
                Toast.makeText(getApplication(),"未连接can转wifi模块",Toast.LENGTH_SHORT).show();
            }else{
                mservice.getCanConnectDetail(200,1000);
                //pd=ProgressDialog.show(getApplication(),"初始化","正在扫描连接...",true,false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mservice.stopGetData2();
            if(mservice.hasStartSavingData){
                mservice.stopSavingData();
            }
            isBind=false;
        }
    };
    //本程序所用到的所有fragment
    ButteryMatDetail fragment1_1=null;
    BmuDetailFragment fragment1_3=null;
    ButteryMatItemDetail fragment1_2=null;
    ButteryMatSelect fragment1_0=null;
    HistoryFragment fragment4=null;
    ErrorRecordsFragment fragment5=null;
    ParamsSetFragment fragment2=null;
    SettingFragment fragment6=null;
    SavaFragment fragment3=null;
    BalanceFragment bFragment=null;

    @Override
    public void OnDataUpdate(byte[] data) {

    }

    @Override
    public void OnScanCompleted() {
        Log.v("dataTag","recieved info");
        modules=mservice.getLcuModuleIDs();
        mservice.getData2();
    }

    @Override
    public void OnSendResult(int result) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(currentFragment==1){
            FragmentTransaction ft=getFragmentManager().beginTransaction();
            ft.detach(fragment2);
            ft.attach(fragment2);
            ft.commitAllowingStateLoss();
        }else if(currentFragment==0){
            FragmentTransaction ft=getFragmentManager().beginTransaction();
            ft.detach(fragment1_1);
            ft.attach(fragment1_1);
            ft.commitAllowingStateLoss();
        }

    }
    //状态栏高度
    int statusBarHeight=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("dataTag", "mainActivity onCreate!");
        fragment1_1=new ButteryMatDetail();
        fragment1_0=new ButteryMatSelect();
        fragment1_2=new ButteryMatItemDetail();
        fragment1_3=new BmuDetailFragment();
        fragment2=new ParamsSetFragment();
        fragment3=new SavaFragment();
        fragment4=new HistoryFragment();
        fragment5=new ErrorRecordsFragment();
        fragment6=new SettingFragment();
        bFragment=new BalanceFragment();
        SharedPreferences msp=getSharedPreferences(PreferencesString.PREFERENCES, MODE_PRIVATE);
        tempHigh=msp.getFloat(PreferencesString.TEMPHIGH, 40f);
        tempLow=msp.getFloat(PreferencesString.TEMPLOW,-10f);
        voltHigh=msp.getFloat(PreferencesString.VOLTHIGH,3.9f);
        voltLow=msp.getFloat(PreferencesString.VOLTLOW,2.1f);
        File mainDir=new File(Environment.getExternalStorageDirectory().toString()+PreferencesString.DATASAVINGSTRING);
        //Log.v("dataTag",mainDir.getPath());
        if(!mainDir.exists()&&!mainDir.isDirectory()){
            if(mainDir.mkdirs()||mainDir.isDirectory()){
                Log.v("dataTag","文件夹已经创建");
            }else {
                Log.v("dataTag","文件夹未能成功创建");
            }
        }else{
            Log.v("dataTag","文件夹已经存在");
        }

        //绑定服务
        bindService(new Intent(this, CanService.class), mconn, BIND_AUTO_CREATE);
        setContentView(R.layout.main_layout);
        mlistview=(ListView)findViewById(R.id.item_menu);
        mlistview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        canPort=(TextView)findViewById(R.id.can_port);
        canPort.setText("can1");
        TextView statusBar=(TextView)findViewById(R.id.status_bar_bg);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        if(statusBarHeight>0){
            statusBar.setHeight(statusBarHeight);
        }else{
            statusBar.setHeight(48);
        }
        ApplicationTime.start();
    }

    public CanService getService(){
        return mservice;
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public Fragment getFragment(int position){
        Fragment re=null;
        switch (position){
            case 11: re=fragment1_1;break;
            case 10:re=fragment1_0;break;
            case 12:re=fragment1_2;break;
            case 13:re=fragment1_3;break;
            //case 1:re=bFragment;break;
            case 2: re=fragment2;break;
            case 3: re=fragment3;break;
            case 4: re=fragment4;break;
            case 5: re=fragment5;break;
            case 6: re=fragment6;break;

        }
        return re;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("dataTag", "mainActivity onDestroy");
        if(mservice!=null){
            if(mservice.hasStartGetData) {
                mservice.stopGetLcuExtraData();
                mservice.stopGetData2();
            }
            if(mservice.hasStartSavingData){
                mservice.stopSavingData();
            }
            if(mservice.isSavingBmu()){
                mservice.stopSaveBmuData();
            }
        }

        unbindService(mconn);

        Log.v("dataTag",can.port1Num+"   "+can.port2Num);
    }
}
