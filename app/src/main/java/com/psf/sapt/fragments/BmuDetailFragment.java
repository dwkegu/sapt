package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.psf.sapt.PreferencesString;
import com.psf.sapt.R;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.DataProgress;
import com.psf.sapt.wifiService.OnDataUpdateListener;
import com.psf.sapt.wifiService.bmuHistorySendMessage;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**显示BMU相关信息的
 * Created by psf on 2015/12/7.
 */
public class BmuDetailFragment extends Fragment implements OnDataUpdateListener,
        AdapterView.OnItemClickListener,View.OnClickListener,TabHost.OnTabChangeListener{
    ListView dataList;
    TabHost mtabHost;
    TabWidget mTW;
    ListView historyList;
    Button  histotyBack,bmuBiaoding;
    EditText bmuNewId;
    Switch saveStatus;
    ProgressBar mbar;
    ListView historyDataList;
    LinearLayout historyShow;
    TextView bmuIDText;
    private int bmuSecondeProgress=0;
    private int bmuProgress=0;
    /**
     *储存充放电电流值
     */
    public float chargeCurrent=0;
    public float chargeCurrent1=0;

    /**
     *绝缘阻抗值
     */
    public float insulationResistance=0;
    public float insulationResistance1=0;
    /**
     * 电池端电压
     */
    public float butteryGroupVoltage=0;
    public float butteryGroupVoltage1=0;
    /**
     * 负载端电压
     */
    public float butteryOutputVoltage=0;
    public float butteryOutputVoltage1=0;

    /**
     * 电池剩余容量
     */
    public float butteryRemainCharge=0;
    public float butteryRemainCharge1=0;
    /**
     * 单体输出平均电压
     */
    public float itemOutputVoltageAverage=0;
    public float itemOutputVoltageAverage1=0;
    /**
     *单体输出电压最小值
     */
    public float itemOutputVoltageMin=0;
    public float itemOutputVoltageMin1=0;
    public int vMinModule=0;
    public int vMinModule1=0;
    public int vMinItem=0;
    public int vMinItem1=0;

    /**
     * 单体输出电压最大值
     */
    public float itemOutputVoltageMax=0;
    public float itemOutputVoltageMax1=0;
    public int vMaxModule=0;
    public int vMaxModule1=0;
    public int vMaxItem=0;
    public int vMaxItem1=0;
    /**
     * 单体温度最大值
     */
    public float itemTempMax=0;
    public float itemTempMax1=0;
    public int tMaxModule=0;
    public int tMaxModule1=0;
    public int tMaxItem=0;
    public int tMaxItem1=0;

    /**
     * 单体温度最小值
     */
    public float itemTempMin=0;
    public float itemTempMin1=0;
    public int tMinModule=0;
    public int tMinModule1=0;
    public int tMinItem=0;
    public int tMinItem1=0;
    /**
     * 单体输出平均电压
     */
    public float itemTempAverage=0;
    public float itemTempAverage1=0;
    /**
     *正继电器状态
     */
    public boolean jidianqiState=false;
    public boolean jidianqiState1=false;
    /**
     *负继电器状态
     */
    public boolean negJidianqiState=false;
    public boolean negJidianqiState1=false;

    dataAdapter mAdapter,mHistoryAdapter;
    DataProgress mpro1,mpro2;
    boolean scanedBMUid=false;
    boolean isScanBmuId=false;
    int BMUID=0;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bmu_history_list_back:
                if(bmuHS!=null){
                     bmuHS.stopThread();
                }
                historyShow.setVisibility(View.GONE);
                historyList.setVisibility(View.VISIBLE);
                break;
            case R.id.bmu_biaoding:
                if(!bmuNewId.getText().toString().trim().equals("")){
                    mservice.setBmuId(Integer.parseInt(bmuNewId.getText().toString().trim()));
                    isScanBmuId=true;
                    Timer mtimer=new Timer();
                    TimerTask mtask=new TimerTask() {
                        @Override
                        public void run() {
                            Log.v("dataTag","start analysis");
                            isScanBmuId=false;
                            if(scanedBMUid){
                                Message msg=Message.obtain();
                                msg.what=1;
                                mHandle.sendMessage(msg);
                                scanedBMUid=false;
                            }else{
                                Message msg=Message.obtain();
                                msg.what=2;
                                mHandle.sendMessage(msg);
                            }
                        }
                    };
                    mtimer.schedule(mtask,600);

                }else{
                    Toast.makeText(getActivity(),"错误ID",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    int currentTab=1;
    @Override
    public void onTabChanged(String tabId) {
        switch (tabId){
            case "tab1":
                if(currentTab==3){
                    if(bmuHS!=null){
                        bmuHS.stopThread();
                    }
                }
                currentTab=1;
                break;
            case "tab2":
                if(currentTab==3){
                    if(bmuHS!=null){
                        bmuHS.stopThread();
                    }
                }
                currentTab=2;
                break;
            case "tab3":
                currentTab=3;
                listData();
                break;
            default:
                break;
        }
    }

    private  Handler mHandle=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    historyDataUpdate((byte[])msg.obj);
                    break;
                case 1:
                    Toast.makeText(getActivity(),"未知1",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getActivity(),"未知2",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            return true;
        }
    });

    public bmuHistorySendMessage bmuHS;
    /**
     *
     * @param data
     */
    public void historyDataUpdate(byte[] data){
        mpro2.setFrames(data);
        int[] ids=mpro2.getId();
        float[] dataFloat=null;
        for(int i=0;i<mpro2.getId().length;i++){
            switch (ids[i]){
                case 0x51:
                    break;
                case 0x52:
                    break;
                case 0x53:
                    break;
                case 0x54:
                    dataFloat=mpro2.getFrameFloatData(i);
                    itemOutputVoltageMax1=dataFloat[0];
                    itemOutputVoltageMin1=dataFloat[1];
                    itemOutputVoltageAverage1=dataFloat[2];
                    break;
                case 0x55:
                    dataFloat=mpro2.getFrameFloatData(i);
                    itemTempMax1=dataFloat[0];
                    itemTempMin1=dataFloat[1];
                    itemTempAverage1=dataFloat[2];
                    break;
                case 0x56:
                    break;
                case 0x57:
                    break;
                case 0x411:
                    dataFloat=mpro2.getFrameFloatData(i);
                    chargeCurrent1=dataFloat[0];
                    insulationResistance1=dataFloat[1];
                    butteryGroupVoltage1=dataFloat[2];
                    butteryOutputVoltage1=dataFloat[3];
                    butteryRemainCharge1=dataFloat[4];
                    break;
                case 0x413:
                    dataFloat=mpro2.getFrameFloatData(i);
                    if(dataFloat[0]==0f){
                        jidianqiState1=false;
                    }else if(dataFloat[0]==1f){
                        jidianqiState1=true;
                    }
                    if(dataFloat[1]==0f){
                        negJidianqiState1=false;
                    }else if(dataFloat[1]==1f){
                        negJidianqiState1=true;
                    }
                    break;
                case 0x416:
                    dataFloat=mpro2.getFrameFloatData(i);
                    tMaxModule1=(int)dataFloat[0];
                    tMaxItem1=(int)dataFloat[1];
                    tMinModule1=(int)dataFloat[2];
                    tMinItem1=(int)dataFloat[3];
                    vMaxModule1=(int)dataFloat[4];
                    vMaxItem1=(int)dataFloat[5];
                    vMinModule1=(int)dataFloat[6];
                    vMinItem1=(int)dataFloat[7];
                    break;
                default:
                    break;
            }
        }
        mHistoryAdapter.notifyDataSetChanged();
        mpro2.setAllowSetNewData();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        historyList.setVisibility(View.GONE);
        historyShow.setVisibility(View.VISIBLE);
        bmuHS=new bmuHistorySendMessage(datalist[position],mHandle);
        bmuHS.start();
        historyDataList.setAdapter(mHistoryAdapter);
    }


    @Override
    public void OnDataUpdate(byte[] data) {
        if(saveStatus.isChecked()){
            bmuProgress=(int)mservice.getBmuSaveProgress();
            mbar.setProgress(bmuProgress);
            mbar.setSecondaryProgress(bmuSecondeProgress);
            bmuSecondeProgress++;
            bmuSecondeProgress%=101;
        }
        mpro1.setFrames(data);
        int[] ids=mpro1.getId();
        float[] dataFloat=null;
        for(int i=0;i<mpro1.getId().length;i++){
            //Log.v("dataTag","֡ID"+ids[i]);
            switch (ids[i]){
                case 0x51:
                    break;
                case 0x52:
                    break;
                case 0x53:
                    break;
                case 0x54:
                    dataFloat=mpro1.getFrameFloatData(i);
                    itemOutputVoltageMax=dataFloat[0];
                    itemOutputVoltageMin=dataFloat[1];
                    itemOutputVoltageAverage=dataFloat[2];
                    break;
                case 0x55:
                    dataFloat=mpro1.getFrameFloatData(i);
                    itemTempMax=dataFloat[0];
                    itemTempMin=dataFloat[1];
                    itemTempAverage=dataFloat[2];
                    break;
                case 0x56:
                    break;
                case 0x57:
                    break;
                case 0x411:
                    dataFloat=mpro1.getFrameFloatData(i);
                    chargeCurrent=dataFloat[0];
                    insulationResistance=dataFloat[1];
                    butteryGroupVoltage=dataFloat[2];
                    butteryOutputVoltage=dataFloat[3];
                    butteryRemainCharge=dataFloat[4];
                    break;
                case 0x413:
                    dataFloat=mpro1.getFrameFloatData(i);
                    if(dataFloat[0]==0f){
                        jidianqiState=false;
                    }else if(dataFloat[0]==1f){
                        jidianqiState=true;
                    }
                    if(dataFloat[1]==0f){
                        negJidianqiState=false;
                    }else if(dataFloat[1]==1f){
                        negJidianqiState=true;
                    }
                    break;
                case 0x416:
                    dataFloat=mpro1.getFrameFloatData(i);
                    tMaxModule=(int)dataFloat[0];
                    tMaxItem=(int)dataFloat[1];
                    tMinModule=(int)dataFloat[2];
                    tMinItem=(int)dataFloat[3];
                    vMaxModule=(int)dataFloat[4];
                    vMaxItem=(int)dataFloat[5];
                    vMinModule=(int)dataFloat[6];
                    vMinItem=(int)dataFloat[7];
                    break;
                case 0x417:
                    if(isScanBmuId){
                        scanedBMUid=true;
                    }
                    BMUID=(int)mpro1.getFrameFloatData(i)[0];
                    bmuIDText.setText("BMU-"+BMUID+"      ");
                    break;
                default:
                    break;
            }
        }
        mAdapter.notifyDataSetChanged();
        mpro1.setAllowSetNewData();
    }

    @Override
    public void OnScanCompleted() {

    }

    @Override
    public void OnSendResult(int result) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mAdapter==null){
            mAdapter=new dataAdapter(getActivity());
        }
        if(mHistoryAdapter==null){
            mHistoryAdapter=new dataAdapter(getActivity());
            mHistoryAdapter.setType(1);
        }
        mpro1=new DataProgress();
        mpro2=new DataProgress();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.bmu_detail,container,false);
        dataList=(ListView)rootView.findViewById(R.id.bmu_list);
        historyShow=(LinearLayout)rootView.findViewById(R.id.bmu_show_data);
        historyShow.setVisibility(View.GONE);
        bmuIDText=(TextView)rootView.findViewById(R.id.bmu_id_text);
        bmuIDText.setText("BMU-"+BMUID+"     ");
        historyList=(ListView)rootView.findViewById(R.id.bmu_history_select);
        historyDataList=(ListView)rootView.findViewById(R.id.bmu_history_list);
        histotyBack=(Button)rootView.findViewById(R.id.bmu_history_list_back);
        bmuBiaoding=(Button)rootView.findViewById(R.id.bmu_biaoding);
        bmuBiaoding.setOnClickListener(this);
        bmuNewId=(EditText)rootView.findViewById(R.id.bmu_new_id);
        saveStatus=(Switch)rootView.findViewById(R.id.switch1);
        saveStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    buttonView.setChecked(mservice.saveBmuData());
                }else{
                    Toast.makeText(getActivity(),"停止储存",Toast.LENGTH_SHORT).show();
                    bmuProgress=0;
                    bmuSecondeProgress=0;
                    mservice.stopSaveBmuData();
                    listData();
                }
            }
        });
        mbar=(ProgressBar)rootView.findViewById(R.id.bmu_save_bar);
        mbar.setProgress(bmuProgress);
        if(mservice!=null){
            saveStatus.setChecked(mservice.isSavingBmu());
        }else{
            saveStatus.setChecked(false);
        }
        historyList.setOnItemClickListener(this);
        histotyBack.setOnClickListener(this);
        mtabHost=(TabHost)rootView.findViewById(R.id.bmu_tab_host);
        mtabHost.setup();
        mtabHost.addTab(mtabHost.newTabSpec("tab1").setIndicator("BMU巡检").setContent(R.id.tab1));
        mtabHost.addTab(mtabHost.newTabSpec("tab2").setIndicator("BMU ID标定").setContent(R.id.tab2));
        mtabHost.addTab(mtabHost.newTabSpec("tab3").setIndicator("BMU数据储存").setContent(R.id.tab3));
        mtabHost.setOnTabChangedListener(this);
        /*
        saveButton=(Button)rootView.findViewById(R.id.bmu_start_saving);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */
        dataList.setAdapter(mAdapter);
        listData();
        return rootView;
    }
    String[] datalist;

    /**
     *
     */
    public void saveData(){
        if(mservice!=null){
            mservice.saveBmuData();
        }
    }
    ArrayAdapter<String> hlistAdapter;
    /**
     *
     */
    public void listData(){
        String bmudir= Environment.getExternalStorageDirectory().getPath()+ PreferencesString.BMUDATASAVESTRING;
        File bmuFile=new File(bmudir);
        if(!bmuFile.exists()||!bmuFile.isDirectory()){
           return;
        }
        datalist=bmuFile.list();
        hlistAdapter=new ArrayAdapter<String>(getActivity(),R.layout.simple_list_item,R.id.simple_list_item_text1,datalist);
        historyList.setAdapter(hlistAdapter);
        hlistAdapter.notifyDataSetChanged();
    }

    CanService mservice=null;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(mservice==null){
            mservice=((mainActivity)activity).getService();
        }
        mservice.setListener(this,"bmuFragment");
        mservice.getBMUID();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    public class dataAdapter extends BaseAdapter{
        ViewHolder mholder;
        LayoutInflater inflater;
        dataAdapter(Context context){
            inflater=LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return (type==0)? 14:13;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
        int type=0;
        public void setType(int type){
            this.type=type;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView!=null){
                mholder=(ViewHolder)convertView.getTag();
                if(mholder.postion==position){
                    setValues(position,mholder,type);
                }else{
                    convertView=inflater.inflate(R.layout.bmu_detail_item,parent,false);
                    mholder.postion=position;
                    mholder.name=(TextView)convertView.findViewById(R.id.bmu_item_name);
                    mholder.value=(TextView)convertView.findViewById(R.id.bmu_item_value);
                    setValues(position,mholder,type);
                    convertView.setTag(mholder);
                }
            }else{
                mholder=new ViewHolder();
                convertView=inflater.inflate(R.layout.bmu_detail_item,parent,false);
                mholder.postion=position;
                mholder.name=(TextView)convertView.findViewById(R.id.bmu_item_name);
                mholder.value=(TextView)convertView.findViewById(R.id.bmu_item_value);
                setValues(position,mholder,type);
                convertView.setTag(mholder);
            }
            return convertView;
        }

        public void setValues(int position,ViewHolder holder,int which){
            if(which==0){
                switch (position){
                    case 0:
                        holder.name.setText("单体输出电压最大值");
                        holder.value.setText(String.format("M%d-%d   %.3fV",vMaxModule,vMaxItem,itemOutputVoltageMax));

                        break;
                    case 1:
                        holder.name.setText("单体输出电压最小值");
                        holder.value.setText(String.format("M%d-%d   %.3fV",vMinModule,vMinItem,itemOutputVoltageMin));

                        break;
                    case 2:
                        holder.name.setText("单体输出电压平均值");
                        holder.value.setText(String.format("%.3fV",itemOutputVoltageAverage));
                        break;
                    case 3:
                        holder.name.setText("单体温度最大值");
                        holder.value.setText(String.format("M%d-%d   %.3f��C",tMaxModule,tMinItem,itemTempMax));
                        break;
                    case 4:
                        holder.name.setText("单体温度最小值");
                        holder.value.setText(String.format("M%d-%d   %.3f��C",tMinModule,tMinItem,itemTempMin));
                        break;
                    case 5:
                        holder.name.setText("单体温度平均值");
                        holder.value.setText(String.format("%.3f��C",itemTempAverage));
                        break;
                    case 6:
                        holder.name.setText("充放电电流");
                        holder.value.setText(String.format("%.3fA",chargeCurrent));
                        break;
                    case 7:
                        holder.name.setText("绝缘阻抗");
                        holder.value.setText(String.format("%.3fK��",insulationResistance));
                        break;
                    case 8:
                        holder.name.setText("电池端电压");
                        holder.value.setText(String.format("%.3fV",butteryGroupVoltage));
                        break;
                    case 9:
                        holder.name.setText("负载端电压");
                        holder.value.setText(String.format("%.3fV",butteryOutputVoltage));
                        break;
                    case 10:
                        holder.name.setText("电池剩余容量");
                        holder.value.setText(String.format("%.3f%%",butteryRemainCharge));
                        break;
                    case 11:
                        holder.name.setText("正继电器状态");
                        if(!jidianqiState){
                            holder.value.setText("关");
                        }else{
                            holder.value.setText("开");
                        }
                        //holder.value.setText(String.format("%.3fT",itemTempMin));
                        break;
                    case 12:
                        holder.name.setText("负继电器状态");
                        if(!negJidianqiState){
                            holder.value.setText("关");
                        }else{
                            holder.value.setText("开");
                        }
                        break;
                    case 13:
                        holder.name.setText("电池容量");
                        holder.value.setText(String.valueOf(mservice.getBatteryCapacity()/3600f)+"Ah");
                    default:
                        break;
                }
            }else if(which==1){
                switch (position){
                    case 0:
                        holder.name.setText("单体输出电压最大值");
                        holder.value.setText(String.format("M%d-%d   %.3fV",vMaxModule,vMaxItem,itemOutputVoltageMax));

                        break;
                    case 1:
                        holder.name.setText("单体输出电压最小值");
                        holder.value.setText(String.format("M%d-%d   %.3fV",vMinModule,vMinItem,itemOutputVoltageMin));

                        break;
                    case 2:
                        holder.name.setText("单体输出电压平均值");
                        holder.value.setText(String.format("%.3fV",itemOutputVoltageAverage));
                        break;
                    case 3:
                        holder.name.setText("单体温度最大值");
                        holder.value.setText(String.format("M%d-%d   %.3f��C",tMaxModule,tMinItem,itemTempMax));
                        break;
                    case 4:
                        holder.name.setText("单体温度最小值");
                        holder.value.setText(String.format("M%d-%d   %.3f��C",tMinModule,tMinItem,itemTempMin));
                        break;
                    case 5:
                        holder.name.setText("单体温度平均值");
                        holder.value.setText(String.format("%.3f��C",itemTempAverage));
                        break;
                    case 6:
                        holder.name.setText("充放电电流");
                        holder.value.setText(String.format("%.3fA",chargeCurrent));
                        break;
                    case 7:
                        holder.name.setText("绝缘阻抗");
                        holder.value.setText(String.format("%.3fK��",insulationResistance));
                        break;
                    case 8:
                        holder.name.setText("电池端电压");
                        holder.value.setText(String.format("%.3fV",butteryGroupVoltage));
                        break;
                    case 9:
                        holder.name.setText("负载端电压");
                        holder.value.setText(String.format("%.3fV",butteryOutputVoltage));
                        break;
                    case 10:
                        holder.name.setText("电池剩余容量");
                        holder.value.setText(String.format("%.3f%%",butteryRemainCharge));
                        break;
                    case 11:
                        holder.name.setText("正继电器状态");
                        if(!jidianqiState){
                            holder.value.setText("关");
                        }else{
                            holder.value.setText("开");
                        }
                        //holder.value.setText(String.format("%.3fT",itemTempMin));
                        break;
                    case 12:
                        holder.name.setText("负继电器状态");
                        if(!negJidianqiState){
                            holder.value.setText("关");
                        }else{
                            holder.value.setText("开");
                        }
                        break;
                    case 13:
                        holder.name.setText("电池容量");
                        holder.value.setText(String.valueOf(mservice.getBatteryCapacity()/3600f)+"Ah");
                    default:
                        break;
                }
            }

        }
    }
    class ViewHolder{
        int postion;
        TextView name,value;
        ViewHolder(){
            postion=0;
        }
    }
}
