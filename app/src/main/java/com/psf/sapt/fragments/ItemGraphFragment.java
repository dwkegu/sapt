package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.psf.sapt.R;
import com.psf.sapt.drawing.DataGraphView;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.Connect2ServiceThread;
import com.psf.sapt.wifiService.DataProgress;
import com.psf.sapt.wifiService.OnDataUpdateListener;

/**
 * Created by psf on 2015/9/15.
 */
public class ItemGraphFragment extends Fragment implements OnDataUpdateListener,View.OnClickListener{
    private com.psf.sapt.drawing.DataGraphView mgraph;
    TextView maxVoltView,minVoltView;
    private boolean[] items=null;
    private int[] selected=null;
    private int num=0;
    private float[] data=null;
    private int[] IDs=null;
    private boolean isTempType=false;
    DataProgress mprogress=null;
    private float[][] data1=null;
    private float[] data2=null;
    private float[] data3=null;
    private float[][] data4=null;
    Button allTempGraph,allVoltGraph;
    private CanService mservice=null;
    private Connect2ServiceThread connect2ServiceThread=null;
    public Handler mhandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    getService();
                    break;
            }
            return false;
        }
    });
    public boolean containID(int position){
        // if(position==0) return true;
        if(selected!=null){
            for(int i=0;i<selected.length;i++){
                if(selected[i]==position){
                    return true;
                }
            }
        }

        return false;
    }

    private mainActivity holdActivity=null;
    @Override
    public void OnDataUpdate(byte[] data) {
        mprogress.setFrames(data);
        IDs=mprogress.getId();
        int length0fData=mprogress.getId().length;
        for(int i=0;i<length0fData;i++){
            if(containID((IDs[i]>>4)-0x10)){
                float[] dataFloat=mprogress.getFrameFloatData(i);
                if(IDs[i]>0x100){
                    switch (IDs[i]&0x00f){
                        case 0x000:
                           // Log.v("dataTag", "id:" + Integer.toHexString(mprogress.getId()[i]));
                            data3=dataFloat;
                            break;
                        case 0x002:
                           // Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            data2=dataFloat;
                            break;
                        case 0x004:
                            //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat,0,data1[(IDs[i]-0x100)/16],0,dataFloat.length);
                            break;
                        case 0x005:
                            //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data1[(IDs[i]-0x100)/16], 4, dataFloat.length);
                            break;
                        case 0x006:
                            //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data1[(IDs[i]-0x100)/16], 8, dataFloat.length);
                            break;
                        case 0x007:
                            //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data4[(IDs[i]-0x100)/16], 0, dataFloat.length);
                            break;
                        case 0x008:
                            //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data4[(IDs[i]-0x100)/16], 4, dataFloat.length);
                            break;
                        case 0x00A:
                            //////////////////////////////////////////////////BUGBUGBUGBBBBBBBBBBBBBBBBB
                           // Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data1[(IDs[i]-0x100)/16], 12, dataFloat.length);
                            break;
                        case 0x00B:
                           // Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data1[(IDs[i]-0x100)/16], 16, dataFloat.length);
                            break;
                        case 0x00C:
                            //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[i]));
                            System.arraycopy(dataFloat, 0, data1[(IDs[i]-0x100)/16], 20, dataFloat.length);
                            break;
                    }
                }
                if(isTempType){
                    mgraph.setData(data4[(IDs[i]-0x100)/16],(IDs[i]-0x100)/16);
                }else{
                    mgraph.setData(data1[(IDs[i]-0x100)/16],(IDs[i]-0x100)/16);
                    maxVoltView.setText("M"+ String.format("%d", mgraph.maxVoltModule)+"-"
                            + String.format("%d",(mgraph.maxVoltItem+1))
                            +"    " + String.format("%.2f", mgraph.maxVolt) + "V");
                    minVoltView.setText("M"+String.format("%d",mgraph.minVoltModule)+"-"
                            +String.format("%d",(mgraph.minVoltItem+1))+
                            "    "+String.format("%.2f",mgraph.minVolt)+"V");
                }
                /*
                if(mgraph.isHasProblem()){
                    if(mplayer==null){
                        mplayer= MediaPlayer.create(getActivity(), R.raw.mat_error_warning);
                        mplayer.setLooping(true);
                        mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try{
                            mplayer.prepare();
                        }catch (IOException e){
                            e.printStackTrace();
                        }catch (IllegalStateException e){
                            e.printStackTrace();
                        }
                    }
                    if(!mplayer.isPlaying()){
                        mplayer.start();
                    }
                }else{
                    if(mplayer!=null&&mplayer.isPlaying()){
                        mplayer.pause();
                    }
                }
                */
            }

        }
        mprogress.setAllowSetNewData();
    }
//MediaPlayer mplayer=null;
    @Override
    public void OnScanCompleted() {

    }

    @Override
    public void OnSendResult(int result) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.all_temp_graph:
                allTempGraph.setTextColor(getResources().getColor(R.color.red_color));
                //dataExcel.setTextColor(getResources().getColor(R.color.white_line));
                allVoltGraph.setTextColor(getResources().getColor(R.color.white_line));
                //ButteryItem.setVisibility(View.GONE);
                mgraph.setArguements(null, selected, null, true, false);
                //showGraph=true;
                mgraph.setLimit(holdActivity.getTempHigh(), holdActivity.getTempLow());
                maxVoltView.setVisibility(View.GONE);
                minVoltView.setVisibility(View.GONE);
                isTempType=true;
                //mgraph.setVisibility(View.VISIBLE);
                break;
            case R.id.all_volt_graph:
                allVoltGraph.setTextColor(getResources().getColor(R.color.red_color));
                //dataExcel.setTextColor(getResources().getColor(R.color.white_line));
                allTempGraph.setTextColor(getResources().getColor(R.color.white_line));
                //ButteryItem.setVisibility(View.GONE);
                mgraph.setArguements(getOrderedData(data1[0]),selected,null,false,false);
                mgraph.setLimit(holdActivity.getVoltHigh(), holdActivity.getVoltLow());
                //showGraph=true;
                maxVoltView.setVisibility(View.VISIBLE);
                minVoltView.setVisibility(View.VISIBLE);
                isTempType=false;
                mgraph.setVisibility(View.VISIBLE);
                break;
        }
    }

    public float[] getOrderedData(float[] data){
        int num=0;
        for(int i=0;i<data.length;i++){
            if(data[i]==9999.0f){
                num=i+1;
                break;
            }
        }
        float[] result =new float[num];
        System.arraycopy(data,0,result,0,num);
        return result;
    }
    public ItemGraphFragment setArguements(boolean[] selectedItems,boolean isTempType){
        items=selectedItems;
        this.isTempType=isTempType;
        for(int i=0;i<items.length;i++){
            if(items[i]){
                num++;
            }
        }
        selected=new int[num];
        for (int i=0,j=0;i<items.length;i++){
            if(items[i]){
                selected[j]=i;
                j++;
            }
        }
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mprogress=new DataProgress();
        getService();
        if(data4==null){
            data4=new float[40][8];
        }
        for(int i=0;i<40;i++){
            for(int j=0;j<8;j++){
                data4[i][j]=9999f;
            }
        }
        if(data1==null){
            data1=new float[40][24];
        }
        for(int i=0;i<40;i++){
            for(int j=0;j<24;j++){
                data1[i][j]=9999f;
            }
        }
    }
    public void getService() {
        if (mservice == null) {
            Log.v("dataTag", "attach 2");
            if (!holdActivity.isBind) {
                Log.v("dataTag", "attach 3");
                //pd=new ProgressDialog(getActivity());
                connect2ServiceThread = new Connect2ServiceThread(holdActivity, mhandler);
                connect2ServiceThread.start();
            } else {
                Log.v("dataTag", "attach 4");
                mservice = holdActivity.getService();
                mservice.setListener(this, "from ButteryMatItemDetail");
                if (mservice.isConnect2Module()) {
                    Log.v("dataTag", "attach 5");
                    mservice.getLcuExtraData();
                }
            }

        } else {
            mservice.setListener(this, "from ItemGraphView");
            if (mservice.isConnect2Module()) {
                Log.v("dataTag", "attach 5");
                mservice.getLcuExtraData();
            }
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        holdActivity=(mainActivity)activity;
    }


    public void initDataGraphView(){
        mgraph.setArguements(null,selected,null,isTempType,false);
        mgraph.setLimit(holdActivity.getVoltHigh(), holdActivity.getVoltLow());
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.item_data_graph,container,false);
        mgraph=(DataGraphView)rootView.findViewById(R.id.item_data_graph_view);
        allTempGraph=(Button)rootView.findViewById(R.id.all_temp_graph);
        allVoltGraph=(Button)rootView.findViewById(R.id.all_volt_graph);
        allTempGraph.setOnClickListener(this);
        allVoltGraph.setOnClickListener(this);
        maxVoltView=(TextView)rootView.findViewById(R.id.itemGraph_maxVolt);
        minVoltView=(TextView)rootView.findViewById(R.id.itemGraph_minVolt);
        initDataGraphView();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(connect2ServiceThread!=null){
            connect2ServiceThread.flag=true;
        }
        /*
        if(mplayer!=null){
            mplayer.release();
            mplayer=null;
        }
        */

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
