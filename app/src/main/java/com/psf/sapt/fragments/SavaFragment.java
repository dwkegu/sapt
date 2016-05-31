package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.psf.sapt.R;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.OnDataUpdateListener;

/**
 * Created by psf on 2015/9/9.
 */
public class SavaFragment extends Fragment implements View.OnClickListener,OnDataUpdateListener {

    private ProgressBar mpb=null;
    private Spinner time,frequency,savingWays;
    private Button startAndEnd=null;
    private Button pause=null;
    private CanService mservice=null;
    private boolean hasStartSave=false;
    private boolean isPause=false;

    private int secondProgress=0;
    @Override
    public void OnDataUpdate(byte[] data) {
        //更新进度
        if((int)mservice.getSavingProgress()==100){
            hasStartSave=false;
            startAndEnd.setText("开始");
            pause.setText("暂停");
        }
        if(hasStartSave){
            mpb.setProgress((int) (100*mservice.getSavingProgress()));
            mpb.setSecondaryProgress(++secondProgress);
            if(secondProgress>=100){
                secondProgress=0;
            }
        }

        Log.v("dataTag","保存进度："+mservice.getSavingProgress());
        mpb.invalidate();
    }

    @Override
    public void OnScanCompleted() {

    }

    @Override
    public void OnSendResult(int result) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.saving_start:
                if(!hasStartSave){
                    int timeSaving=0;
                    float fr=0;
                    switch (time.getSelectedItemPosition()){
                        case 0:
                            timeSaving=(int)(0.5f*3600);
                            break;
                        case 1:
                            timeSaving=(int)(1f*3600);
                            break;
                        case 2:
                            timeSaving=(int)(2f*3600);
                            break;
                        case 3:
                            timeSaving=(int)(3f*3600);
                            break;
                        case 4:
                            timeSaving=(int)(4f*3600);
                            break;
                        case 5:
                            timeSaving=(int)(5f*3600);
                            break;
                        case 6:
                            timeSaving=(int)(6f*3600);
                            break;
                        default:
                            timeSaving=0;
                            break;
                    }
                    switch (frequency.getSelectedItemPosition()){
                        case 0:
                            fr=0.2f;break;
                        case 1:
                            fr=0.1f;break;
                        case 2:
                            fr=1/15f;break;
                        case 3:
                            fr=2f;break;
                        case 4:
                            fr=5f;break;
                        default:fr=0.1f;break;
                    }
                    int whichBoard=0;
                    switch (savingWays.getSelectedItemPosition()){
                        case 0:whichBoard=0;break;
                        case 1:whichBoard=1;break;
                        case 2:whichBoard=2;break;
                        case 3:whichBoard=3;break;
                        default:break;
                    }
                    if(mservice.saveData(timeSaving,fr,whichBoard)){
                        hasStartSave=true;
                        startAndEnd.setText("结束");
                        secondProgress=0;
                    }else{
                        Toast.makeText(getActivity(),"请重试",Toast.LENGTH_SHORT).show();
                    }

                }else{
                    mservice.stopSavingData();
                    hasStartSave=false;
                    startAndEnd.setText("开始");
                }
                break;
            case R.id.saving_pause:

                Toast.makeText(getActivity(),"暂时无法使用该功能！",Toast.LENGTH_SHORT).show();
                /*
                if(hasStartSave&&!isPause){
                    mservice.pauseSavingData();
                    pause.setText("继续");
                    isPause=true;
                }else if(hasStartSave&&isPause){
                    pause.setText("暂停");
                    mservice.continueSavingData();
                    isPause=false;
                 }
                */
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("dataTag", "SavingFragment onCreate");
        if(savedInstanceState!=null){
            hasStartSave=savedInstanceState.getBoolean("hasStartSave");
            isPause=savedInstanceState.getBoolean("isPause");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v("dataTag", "SavingFragment onAttach");
        ((mainActivity)activity).currentFragment=2;
        if(mservice==null){
            mservice=((mainActivity)activity).getService();
            //mservice.setListener(this,"SavingFragment");
        }
    }
private String[] timeSelection=new String[]{
    "0.5小时","1小时","2小时","3小时","4小时","5小时","6小时"
};
    private String[] frequencySelection=new String[]{
            "5秒","10秒","15秒"
    };
    private String[] savingWaysString=new String[]{
            "18串存储",
            "16串存储",
            "12串存储",
            "22串储存"
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("dataTag", "SavingFragment onCreateView");
        if(mservice==null){
            mservice=((mainActivity)getActivity()).getService();
            mservice.setListener(this,"SavingFragment");
        }else {
            mservice.setListener(this,"SavingFragment");
        }
        View rootView=inflater.inflate(R.layout.saving,container,false);
        time=(Spinner)rootView.findViewById(R.id.save_time_chooser);
        time.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,R.id.spinner_text1,timeSelection));
        frequency=(Spinner)rootView.findViewById(R.id.saving_frequency);
        frequency.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,R.id.spinner_text1,frequencySelection));
        savingWays=(Spinner)rootView.findViewById(R.id.saving_ways);
        savingWays.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,R.id.spinner_text1,savingWaysString));
        mpb=(ProgressBar)rootView.findViewById(R.id.saving_progress);
        startAndEnd=(Button)rootView.findViewById(R.id.saving_start);
        pause=(Button)rootView.findViewById(R.id.saving_pause);
        if(hasStartSave){
            if(mservice!=null){
                float savePro=mservice.getSavingProgress();
                mpb.setProgress((int)savePro);
            }
            startAndEnd.setText("结束");
        }
        if(isPause){
            pause.setText("继续");
        }
        startAndEnd.setOnClickListener(this);
        pause.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v("dataTag", "SavingFragment onDestroyView");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasStartSave", hasStartSave);
        outState.putBoolean("isPause",isPause);
    }

    @Override
    public void onDestroy() {
        Log.v("dataTag", "SavingFragment onDestroy");
        super.onDestroy();
    }
}
