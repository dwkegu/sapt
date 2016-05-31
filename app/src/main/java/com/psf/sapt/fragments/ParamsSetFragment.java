package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.psf.sapt.PreferencesString;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.DataProgress;
import com.psf.sapt.wifiService.OnDataUpdateListener;
import com.psf.sapt.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by psf on 2015/8/21.
 */
public class ParamsSetFragment extends Fragment implements View.OnClickListener,OnDataUpdateListener,
        AdapterView.OnItemSelectedListener,CompoundButton.OnCheckedChangeListener{
    Spinner spinnerOldID,spinnerNewID,spinner_ID,spinnerVmax;
    CheckBox[] cb1=null;
    boolean[] cbCheck=null;
    Button biaoding1=null;
    Button biaoding2=null;
    Button saving1=null;
    Button saving2=null;
    ImageView idState,tempState,voltState;
    TextView biaodingWay;
    CanService mservece=null;
    int oldid,newid,id,temp,volt;
    int[] moduleIds;
    int tempNum=0;
    int voltNum=0;
    private int WAITFOR=0;
    DataProgress mpro=null;
    private boolean scanModuleId=false;
    private boolean scanModuleDetail=false;
    private int scannedModule=0;
    private boolean scannedOldId=false;
    private boolean scannedNewId=false;
    private int scanTimes=4;
    int[] IDS=null;
    private Handler mhandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    @Override
    public void OnDataUpdate(byte[] data) {
        //Log.v("dataTag", "params data update");
        if(scanModuleId){
            mpro.setFrames(data);
            IDS=mpro.getPureIDs();
            scanTimes--;
            if(containID(oldid)){
                scannedOldId=true;
                Log.v("dataTag","has old id");
            }
            if(containID(newid)){
                scannedNewId=true;
                Log.v("data","has new id");
            }
            if(scanTimes==0){
                scanModuleId=false;
                if(!scannedOldId&&scannedNewId){
                    Toast.makeText(getActivity(),"标定成功",Toast.LENGTH_SHORT).show();
                    idState.setImageResource(R.drawable.state_ok);
                }else{
                    Toast.makeText(getActivity(),"标定失败",Toast.LENGTH_SHORT).show();
                }
            }
            mpro.setAllowSetNewData();
        }
        if(scanModuleDetail){
            mpro.setFrames(data);
            IDS=mpro.getPureIDs();
            scanTimes--;
            int ways=getActivity().getSharedPreferences(PreferencesString.PREFERENCES,Context.MODE_PRIVATE)
                    .getInt(PreferencesString.BIAODINGWAY,0);
            for(int i=0;i<IDS.length;i++){
                if(IDS[i]==id){
                    int num=0;
                    float[] datatemp=null;
                    switch (mpro.getID(mpro.getFrame(i))&0x00f){
                        case 0x4:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            voltNum=num;
                            break;
                        case 0x5:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            voltNum=num+4;
                            break;
                        case 0x6:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            voltNum=num+8;
                            break;
                        case 0x7:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            Log.v("dataTag","7datatemp"+num);
                            tempNum=num;
                            break;
                        case 0x8:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            Log.v("dataTag","8datatemp"+num);
                            tempNum=num+tempNum;
                            break;
                        case 0xa:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            if(ways==0|ways==2){
                                voltNum=num+12;
                            }else if(ways==1){
                                voltNum=num+8;
                            }

                            break;
                        case 0xb:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            if(ways==0|ways==2){
                                voltNum=num+16;
                            }else if(ways==1){
                                voltNum=num+12;
                            }
                            break;
                        case 0xc:
                            datatemp=mpro.getFrameFloatData(i);
                            for(int j=0;j<datatemp.length;j++){
                                if(datatemp[j]!=9999f){
                                    num++;
                                }
                            }
                            voltNum=num+20;
                            break;

                    }
                }
            }
            if(scanTimes==0){
                scanModuleDetail=false;
                //mservece.stopGetLcuExtraData();
                Log.v("dataTag","温度路数："+tempNum+"电压路数："+voltNum);
                int temp1=0;
                for(int i=0;i<6;i++){
                    if(cbCheck[i]){
                        temp1++;
                    }
                }
                if(tempNum==temp1&&voltNum==volt){
                    Toast.makeText(getActivity(),"标定成功",Toast.LENGTH_SHORT).show();
                    tempState.setImageResource(R.drawable.state_ok);
                    voltState.setImageResource(R.drawable.state_ok);
                }else{
                    Toast.makeText(getActivity(),"标定失败",Toast.LENGTH_SHORT).show();
                }
            }
            mpro.setAllowSetNewData();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("dataTag","paramsFragment is onSaveInstanceState");
    }
boolean showScan=false;
    @Override
    public void OnScanCompleted() {
        if(showScan){
            switch (WAITFOR){
                case 0:
                    moduleIds=mservece.getLcuModuleIDs();
                    if(getActivity()!=null){
                        Toast.makeText(getActivity(),"标定后ID个数"+moduleIds.length,Toast.LENGTH_SHORT).show();
                        if(!containID(oldid)&&containID(newid)){
                            Toast.makeText(getActivity(),"标定成功",Toast.LENGTH_SHORT).show();
                            idState.setImageResource(R.drawable.state_ok);
                        }else{
                            Toast.makeText(getActivity(),"标定失败",Toast.LENGTH_SHORT).show();
                        }
                    }


                    break;
                case 1:
                    Log.v("dataTag","lecu temp volt"+mservece.getDetail()[0]+"  "+mservece.getDetail()[1]);
                    if(mservece.getDetail()[0]==volt&&mservece.getDetail()[1]==temp){
                        Toast.makeText(getActivity(),"标定成功",Toast.LENGTH_SHORT).show();
                        tempState.setImageResource(R.drawable.state_ok);
                        voltState.setImageResource(R.drawable.state_ok);
                    }else {
                        Log.v("dataTag","标定失败");
                    }
                    break;
                case 2:
                    break;
                case 3:
                    break;
                default:break;
            }
            showScan=false;
        }


    }

    @Override
    public void OnSendResult(int result) {
        switch (result){
            case 1:
                idState.post(new Runnable() {
                    @Override
                    public void run() {
                        idState.setImageResource(R.drawable.state_ok);
                    }
                });break;
            case 2:
                tempState.post(new Runnable() {
                    @Override
                    public void run() {
                        tempState.setImageResource(R.drawable.state_ok);
                    }
                });
                voltState.post(new Runnable() {
                    @Override
                    public void run() {
                        voltState.setImageResource(R.drawable.state_ok);
                    }
                });
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mpro==null){
            mpro=new DataProgress();
        }
        if(cb1==null){
            cb1=new CheckBox[6];
        }
        if(cbCheck==null){
            cbCheck=new boolean[6];
            for(int i=0;i<6;i++){
                cbCheck[i]=false;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((mainActivity)activity).currentFragment=1;
        if(mservece==null){
            mservece=((mainActivity)activity).getService();
        }
        mservece.setListener(this,"from params SetFragment");
    }

    @Override
    public void onClick(View v) {
        if(mservece==null){
            Log.v("dataTag","服务未连接，无法标定");
        }else{
            oldid=(int)spinnerOldID.getSelectedItem();
            newid=(int)spinnerNewID.getSelectedItem();
            id=(int)spinner_ID.getSelectedItem();
            //temp=spinnerTmax.getSelectedItemPosition()+1;
            temp=0x0;
            for(int i=0;i<6;i++){
                if(cbCheck[i]){
                    int j=1;
                    j=j<<i;
                    temp=temp|j;
                }
            }
            volt=spinnerVmax.getSelectedItemPosition()+1;
            int biaodingNum=getActivity().getSharedPreferences(PreferencesString.PREFERENCES, Context.MODE_PRIVATE)
                    .getInt(PreferencesString.BIAODINGWAY,0);
            Log.v("dataTag", "标定参数：" + oldid + "   " + newid + "  " + id + "   " + temp + "  " + volt);
            switch (v.getId()){
                case R.id.params_biaoding1:
                    mservece.setLcuId(oldid, newid, false);
                    //mservece.getCanConnectDetail(500, 1000);
                    WAITFOR=0;
                    Timer mtimer=new Timer();
                    TimerTask mtask=new TimerTask() {
                        @Override
                        public void run() {
                            Log.v("dataTag","start analysis");
                            scanModuleId=true;
                            scannedNewId=false;
                            scannedOldId=false;
                            scanTimes=5;
                        }
                    };
                    mtimer.schedule(mtask,500);
                    Toast.makeText(getActivity(),"已发送",Toast.LENGTH_SHORT).show();
                    showScan=true;
                    break;
                case R.id.params_biaoding2:
                    mservece.setLcuTandV(id, temp, volt, false,biaodingNum);
                    //mservece.getModuleDetail(500, 1200, id);
                    Toast.makeText(getActivity(),"已发送",Toast.LENGTH_SHORT).show();
                    WAITFOR=1;
                    mservece.getLcuExtraData();
                    Timer mtimer1=new Timer();
                    TimerTask mtask1=new TimerTask() {
                        @Override
                        public void run() {
                            Log.v("dataTag","start analysis");
                            scanModuleDetail=true;
                            tempNum=0;
                            voltNum=0;
                            scanTimes=5;
                        }
                    };
                    mtimer1.schedule(mtask1,500);
                    showScan=true;
                    break;
                case R.id.params_save1:
                    idState.setImageResource(R.drawable.state_not);
                    mservece.setLcuId(oldid, newid, true);
                    Toast.makeText(getActivity(),"已保存",Toast.LENGTH_SHORT).show();
                    showScan=true;
                    break;
                case R.id.params_save2:
                    mservece.setLcuTandV(id, temp, volt, true, biaodingNum);
                    Toast.makeText(getActivity(),"已保存",Toast.LENGTH_SHORT).show();
                    showScan=true;
                    break;
                default:
                    break;

            }



        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        voltState.setImageResource(R.drawable.state_not);
        switch (buttonView.getId()){
            case R.id.temp_select_1:
                cbCheck[0]=isChecked;
                break;
            case R.id.temp_select_2:
                cbCheck[1]=isChecked;
                break;
            case R.id.temp_select_3:
                cbCheck[2]=isChecked;
                break;
            case R.id.temp_select_4:
                cbCheck[3]=isChecked;
                break;
            case R.id.temp_select_5:
                cbCheck[4]=isChecked;
                break;
            case R.id.temp_select_6:
                cbCheck[5]=isChecked;
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.old_id1:
                if(position!=(oldid-1)){
                    idState.setImageResource(R.drawable.state_not);
                }
                break;
            case R.id.new_id1:
                if(position!=(newid-1)){
                    idState.setImageResource(R.drawable.state_not);
                }
                break;
            case R.id.biaoding_id1:
                tempState.setImageResource(R.drawable.state_not);
                voltState.setImageResource(R.drawable.state_not);
                break;
            case R.id.voltage_gates:
                voltState.setImageResource(R.drawable.state_not);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.params_set_layout,container,false);
        spinnerOldID=(Spinner)rootView.findViewById(R.id.old_id1);
        spinnerNewID=(Spinner)rootView.findViewById(R.id.new_id1);
        spinnerOldID.setOnItemSelectedListener(this);
        spinnerNewID.setOnItemSelectedListener(this);
        spinner_ID=(Spinner)rootView.findViewById(R.id.biaoding_id1);
        spinner_ID.setOnItemSelectedListener(this);
        spinnerVmax=(Spinner)rootView.findViewById(R.id.voltage_gates);
        spinnerVmax.setOnItemSelectedListener(this);
        /*
        spinnerTmax=(Spinner)rootView.findViewById(R.id.temprature_gates);
        spinnerTmax.setOnItemSelectedListener(this);
        */
        cb1[0]=(CheckBox)rootView.findViewById(R.id.temp_select_1);
        cb1[1]=(CheckBox)rootView.findViewById(R.id.temp_select_2);
        cb1[2]=(CheckBox)rootView.findViewById(R.id.temp_select_3);
        cb1[3]=(CheckBox)rootView.findViewById(R.id.temp_select_4);
        cb1[4]=(CheckBox)rootView.findViewById(R.id.temp_select_5);
        cb1[5]=(CheckBox)rootView.findViewById(R.id.temp_select_6);
        for(int i=0;i<6;i++){
            cb1[i].setOnCheckedChangeListener(this);
        }
        biaoding1=(Button)rootView.findViewById(R.id.params_biaoding1);
        biaoding2=(Button)rootView.findViewById(R.id.params_biaoding2);
        biaodingWay=(TextView)rootView.findViewById(R.id.biaoding_way);
        switch (getActivity().getSharedPreferences(PreferencesString.PREFERENCES,Context.MODE_PRIVATE)
                .getInt(PreferencesString.BIAODINGWAY,0)){
            case 0:biaodingWay.setText("18串");
                break;
            case 1:biaodingWay.setText("16串");
                break;
            case 2:biaodingWay.setText("12串");
                break;
            case 3:biaodingWay.setText("22串");
                break;
            default:biaodingWay.setText("18串");
                break;
        }
        //biaodingWay.setText(? "18串":"12串");
        saving1=(Button)rootView.findViewById(R.id.params_save1);
        saving2=(Button)rootView.findViewById(R.id.params_save2);
        idState=(ImageView)rootView.findViewById(R.id.idbiaoding_state);
        tempState=(ImageView)rootView.findViewById(R.id.tempbiaoding_state);
        voltState=(ImageView)rootView.findViewById(R.id.voltbiaoding_state);
        biaoding1.setOnClickListener(this);
        biaoding2.setOnClickListener(this);
        saving1.setOnClickListener(this);
        saving2.setOnClickListener(this);
        ArrayAdapter<Integer> mAdapter=new ArrayAdapter<Integer>(getActivity(),R.layout.spinner_item,R.id.spinner_text1,
                new Integer[]{
                        1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
                        21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40
                });
        ArrayAdapter<String> vAdapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,R.id.spinner_text1,
                new String[]{
                        "V1","V2","V3","V4","V5","V6","V7","V8","V9","V10","V11","V12",
                        "V13","V14","V15","V16","V17","V18"});
        ArrayAdapter<String> tAdapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,R.id.spinner_text1,
                new String[]{
                        "t1","T2","T3","T4","T5","T6"});
        spinnerOldID.setAdapter(mAdapter);
        spinnerNewID.setAdapter(mAdapter);
        spinner_ID.setAdapter(mAdapter);
        spinnerVmax.setAdapter(vAdapter);

        //spinnerTmax.setAdapter(tAdapter);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //检查是否连接点击模块
    public boolean containID(int position){
        // if(position==0) return true;
        if(IDS!=null){
            for(int i=0;i<IDS.length;i++){
                if(IDS[i]==position){
                    return true;
                }
            }
        }

        return false;
    }
}
