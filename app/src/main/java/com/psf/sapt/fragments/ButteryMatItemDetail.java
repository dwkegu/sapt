package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.psf.sapt.R;
import com.psf.sapt.drawing.DataGraphView;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.Connect2ServiceThread;
import com.psf.sapt.wifiService.DataProgress;
import com.psf.sapt.wifiService.OnDataUpdateListener;

/**
 * Created by psf on 2015/8/22.
 */
public class ButteryMatItemDetail extends Fragment implements OnDataUpdateListener,View.OnClickListener{
    private byte[] data;
    private float[] data3=null;//储存单体电池组的电压温度的最大最小值
    private float[] data2=null; //
    private float[] balanceData=null;
    private float[] data4=null;
    private float[] data1=null;
    private int lcuItemNum=0;
    private int moduleId=0;
    private MediaPlayer mplayer;
    private mBaseAdapter adapter=null;
    private BalanceAdapter bAdapter=null;
    DataProgress mprogress=new DataProgress();
    mainActivity holdActivity=null;
    CanService mservice=null;
    //图像标志
    private boolean showGraph=false;
    //均衡标志
    private boolean showBalance=false;
    Button dataExcel,balanceSwitch,tempGraph,voltGraph;
    GridView ButteryItem;
    DataGraphView mdg;
    Connect2ServiceThread connect2ServiceThread=null;
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
    @Override
    public void OnDataUpdate(byte[] data) {
        this.data=data;
        OnDataChanged();
    }

    @Override
    public void OnScanCompleted() {

    }

    @Override
    public void OnSendResult(int result) {

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.data_excel:
                showGraph=false;
                showBalance=false;
                if(!ButteryItem.getAdapter().equals(adapter)){
                    ButteryItem.setAdapter(adapter);
                }
                dataExcel.setTextColor(getResources().getColor(R.color.red_color));
                balanceSwitch.setTextColor(getResources().getColor(R.color.white_line));
                tempGraph.setTextColor(getResources().getColor(R.color.white_line));
                voltGraph.setTextColor(getResources().getColor(R.color.white_line));
                mdg.setVisibility(View.GONE);
                ButteryItem.setVisibility(View.VISIBLE);
                break;
            case R.id.balance_switch_button:
                showGraph=false;
                showBalance=true;
                if(!ButteryItem.getAdapter().equals(bAdapter)){
                    ButteryItem.setAdapter(bAdapter);
                }
                dataExcel.setTextColor(getResources().getColor(R.color.white_line));
                balanceSwitch.setTextColor(getResources().getColor(R.color.red_color));
                tempGraph.setTextColor(getResources().getColor(R.color.white_line));
                voltGraph.setTextColor(getResources().getColor(R.color.white_line));
                mdg.setVisibility(View.GONE);
                ButteryItem.setVisibility(View.VISIBLE);
                break;
            case R.id.temp_graph:
                tempGraph.setTextColor(getResources().getColor(R.color.red_color));
                balanceSwitch.setTextColor(getResources().getColor(R.color.white_line));
                dataExcel.setTextColor(getResources().getColor(R.color.white_line));
                voltGraph.setTextColor(getResources().getColor(R.color.white_line));
                ButteryItem.setVisibility(View.GONE);
                mdg.setArguements(getOrderedData(data4), new int[]{moduleId}, new int[]{12}, true, true);
                showGraph=true;
                showBalance=false;
                mdg.setLimit(holdActivity.getTempHigh(), holdActivity.getTempLow());
                mdg.setVisibility(View.VISIBLE);
                break;
            case R.id.volt_graph:
                voltGraph.setTextColor(getResources().getColor(R.color.red_color));
                balanceSwitch.setTextColor(getResources().getColor(R.color.white_line));
                dataExcel.setTextColor(getResources().getColor(R.color.white_line));
                tempGraph.setTextColor(getResources().getColor(R.color.white_line));
                ButteryItem.setVisibility(View.GONE);
                mdg.setArguements(getOrderedData(data1), new int[]{moduleId}, new int[]{12}, false, true);
                mdg.setLimit(holdActivity.getVoltHigh(), holdActivity.getVoltLow());
                showGraph=true;
                showBalance=false;
                mdg.setVisibility(View.VISIBLE);
                break;
        }
    }

    public ButteryMatItemDetail setArguements(int moduleId){
    this.moduleId=moduleId;
    return this;
}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getService();
        showGraph=false;
        Log.v("dataTag","matItemDetail onCreate!");
        if(data4==null){
            data4=new float[8];
        }
        for(int i=0;i<8;i++){
            data4[i]=9999f;
        }
        if(data1==null){
            data1=new float[24];
        }
        for(int i=0;i<24;i++){
            data1[i]=9999f;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v("dataTag","attach 1");
        holdActivity=(mainActivity)activity;
        //mservice.setListener(this,"matItemDetail");
        if(adapter==null){
            adapter=new mBaseAdapter(getActivity());
        }
        if(bAdapter==null){
            bAdapter=new BalanceAdapter(getActivity());
        }
        /*
        if(mplayer==null){
            mplayer= MediaPlayer.create(getActivity(), R.raw.mat_error_warning);
            mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try{
                mplayer.prepare();
            }catch (IOException e){
                e.printStackTrace();
            }
            mplayer.setLooping(true);
        }
        */

    }
    public void getService(){
        if(mservice==null){
            Log.v("dataTag","attach 2");
            if(!holdActivity.isBind){
                Log.v("dataTag","attach 3");
                //pd=new ProgressDialog(getActivity());
                connect2ServiceThread=new Connect2ServiceThread(holdActivity,mhandler);
                connect2ServiceThread.start();
            }else{
                Log.v("dataTag","attach 4");
                mservice=holdActivity.getService();
                mservice.setListener(this,"from ButteryMatItemDetail");
                if(mservice.isConnect2Module()){
                    Log.v("dataTag","attach 5");
                    mservice.getLcuExtraData();
                }
            }

        }else {
            mservice.setListener(this,"from ButteryMatItemDetail");
            if(mservice.isConnect2Module()){
                Log.v("dataTag","attach 5");
                mservice.getLcuExtraData();
            }
        }


    }
    public void OnDataChanged(){
        mprogress.setFrames(data);
        for(int j=0;j<mprogress.getId().length;j++){
            float[] dataFloat= mprogress.getFrameFloatData(j);
            //Log.v("dataTag",data.length+"ID"+mprogress.getID(mprogress.getFrame(j)));
            if((mprogress.getId()[j]>>4)-0x10==moduleId){
                switch (mprogress.getId()[j]%16){
                    case 0x000:
                        //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        data3=dataFloat;
                        break;
                    case 0x002:
                        //.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        data2=dataFloat;
                        break;
                    case 0x004:
                       // Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat,0,data1,0,dataFloat.length);
                        break;
                    case 0x005:
                       // Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat, 0, data1, 4, dataFloat.length);
                        break;
                    case 0x006:
                       // Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat, 0, data1, 8, dataFloat.length);
                        break;
                    case 0x007:
                        //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat, 0, data4, 0, dataFloat.length);
                        break;
                    case 0x008:
                        Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j])+dataFloat[0]);
                        System.arraycopy(dataFloat, 0, data4, 4, dataFloat.length);
                        break;
                    case 0x009:
                        //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        //System.arraycopy(dataFloat, 0, data4, 4, dataFloat.length);
                        balanceData=dataFloat;
                        break;
                    case 0x00A:
                        //////////////////////////////////////////////////BUGBUGBUGBBBBBBBBBBBBBBBBB
                        //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat, 0, data1, 12, dataFloat.length);
                        break;
                    case 0x00B:
                        //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat, 0, data1, 16, dataFloat.length);
                        break;
                    case 0x00C:
                        //Log.v("dataTag","id:"+Integer.toHexString(mprogress.getId()[j]));
                        System.arraycopy(dataFloat, 0, data1, 20, dataFloat.length);
                        break;
                    default:
                        break;
                }
            }
        }
        mprogress.setAllowSetNewData();
        if(!showGraph){
            if(showBalance){
                bAdapter.notifyDataSetChanged();
            }else{
                adapter.notifyDataSetChanged();
            }
        }
        else if(mdg.isTempType()){
            mdg.setMultiData(getOrderedData(data4));
        }else{
            mdg.setMultiData(getOrderedData(data1));
        }

        //Log.v("TestTag",new String(data));
    }

    @Override
    public void onDestroyView() {
        Log.v("dataTag","matItemDetail  destroy view");
        super.onDestroyView();

        if(connect2ServiceThread!=null){
            connect2ServiceThread.flag=true;
        }

        if(mplayer!=null){
            mplayer.release();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        holdActivity.fragmentOne=2;
        if(!mservice.hasStartGetLcuExtraData){
            getService();
        }
        for(int i=0;i<8;i++){
            data4[i]=9999f;
        }
        for(int i=0;i<24;i++){
            data1[i]=9999f;
        }
        View rootView = inflater.inflate(R.layout.buttery_item_detail,container,false);
        ButteryItem=(GridView)rootView.findViewById(R.id.buttery_mat_detail_grid);
        mdg=(DataGraphView)rootView.findViewById(R.id.lcu_item_graph);
        Button back=(Button)rootView.findViewById(R.id.button_buttery_item_back);
        dataExcel=(Button)rootView.findViewById(R.id.data_excel);
        dataExcel.setOnClickListener(this);
        balanceSwitch=(Button)rootView.findViewById(R.id.balance_switch_button);
        balanceSwitch.setOnClickListener(this);
        tempGraph=(Button)rootView.findViewById(R.id.temp_graph);
        tempGraph.setOnClickListener(this);
        voltGraph=(Button)rootView.findViewById(R.id.volt_graph);
        voltGraph.setOnClickListener(this);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager manager=getFragmentManager();
                manager.popBackStack();
            }
        });
        ButteryItem.setAdapter(adapter);
        return rootView;
    }
    class BalanceAdapter extends BaseAdapter{
        LayoutInflater inflater;
        BalanceAdapter(Context context){
            inflater=LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return 24;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View childView=inflater.inflate(R.layout.balance_grid_item,parent,false);
            ImageView balanceSwitcher=(ImageView)childView.findViewById(R.id.balance_switcher);
            TextView balanceNum=(TextView)childView.findViewById(R.id.balance_num);
            balanceNum.setText(String.valueOf(String.format("%02d",position+1)));
            if(balanceData!=null){
                int byteNum=position/8;
                int and=0x1;
                switch (position%8){
                    case 0:
                        and=0x1;
                        break;
                    case 1:
                        and=0x2;
                        break;
                    case 2:
                        and=0x4;
                        break;
                    case 3:
                        and=0x8;
                        break;
                    case 4:
                        and=0x10;
                        break;
                    case 5:
                        and=0x20;
                        break;
                    case 6:
                        and=0x40;
                        break;
                    case 7:
                        and=0x80;
                        break;
                }
                if(((int)balanceData[byteNum]&and)!=0){
                    //表示该单体均衡开启
                    balanceSwitcher.setImageResource(R.drawable.balance_switcher_on);
                }else{
                    balanceSwitcher.setImageResource(R.drawable.balance_switcher_off);
                }
            }

            return childView;
        }
    }
    class mBaseAdapter extends BaseAdapter{
        LayoutInflater inflater;
        ViewHoder mholder;
        mBaseAdapter(Context context){
            inflater=LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return 36;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Log.v("TestTag","位置："+position);
            if(data1==null){
                data1=new float[24];
            }
            if(convertView!=null){
                mholder=(ViewHoder)convertView.getTag();
            }else {
                convertView = inflater.inflate(R.layout.buttery_item_cell, parent, false);
                mholder = new ViewHoder();
                mholder.cellName = (TextView) convertView.findViewById(R.id.cell_name);
                mholder.cellData = (TextView) convertView.findViewById(R.id.cell_data);
                convertView.setTag(mholder);
            }

            if((position+1)%3==0){
                if(position/3<6){
                        if(data4[position/3]==9999.0f){
                        mholder.cellName.setText("温度 "+(position/3+1));
                    }else{
                        if(data4[position/3]>holdActivity.getTempHigh()||data4[position/3]<holdActivity.getTempLow()){
                            mholder.cellName.setTextColor(getResources().getColor(R.color.red_color));
                        }else {
                            //cellName.setTextColor(getResources().getColor(R.color.text_black));
                        }
                        mholder.cellName.setText("温度 "+(position/3+1)+"   "+data4[position/3]);
                    }
                    //cellName.setText("temp "+position/3+"   "+data4[position/3]);
                }else{
                    switch (position){
                        case 26:mholder.cellName.setText("最高电压 ");
                            if(data3!=null){
                                if(data3[0]!=9999f){
                                    if(data3[0]>holdActivity.getVoltHigh()||data3[0]<holdActivity.getVoltLow()){
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.red_color));
                                        //mplayer.start();
                                    }else {
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.text_black));
                                    }
                                    mholder.cellData.setText(String.format("%.2f",data3[0])+"V");
                                }

                            }
                            break;
                        case 29:mholder.cellName.setText("最低电压 ");
                            if(data3!=null){
                                if(data3[1]!=9999f){
                                    if(data3[1]>holdActivity.getVoltHigh()||data3[1]<holdActivity.getVoltLow()){
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.red_color));
                                    }else {
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.text_black));
                                    }
                                    mholder.cellData.setText(String.format("%.2f",data3[1])+"V");
                                }

                            }
                            break;
                        case 32:mholder.cellName.setText("最高温度 ");
                            if(data3!=null){
                                if(data3[2]!=9999f){
                                    if(data3[2]>holdActivity.getTempHigh()||data3[2]<holdActivity.getTempLow()){
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.red_color));
                                    }else{
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.text_black));
                                    }
                                    mholder.cellData.setText(String.format("%.2f",data3[2])+"℃");
                                }

                            }
                            break;
                        case 35:mholder.cellName.setText("最低温度 ");
                            if(data3!=null){
                                if(data3[3]!=9999f){
                                    if(data3[3]>holdActivity.getTempHigh()||data3[3]<holdActivity.getTempLow()){
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.red_color));
                                    }else{
                                        mholder.cellData.setTextColor(getResources().getColor(R.color.text_black));
                                    }
                                    mholder.cellData.setText(String.format("%.2f",data3[3])+"℃");
                                }

                            }
                            break;
                    }

                }

            }else{
                if(position%3==0){
                    mholder.cellName.setText("单体 "+(position/3+1));
                    if(data1[position/3]!=9999.0f){
                        mholder.cellData.setText(String.valueOf(data1[position/3])+"V");
                    }
                }else if(position%3==1){
                    mholder.cellName.setText("单体 "+(position/3+13));
                    if(data1[position/3+12]!=9999.0f){
                        mholder.cellData.setText(String.valueOf(data1[position/3+12])+"V");
                    }
                }
            }
            return convertView;
        }
    }
    class ViewHoder{
        TextView cellName;
        TextView cellData;
    }

}
