package com.psf.sapt.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.psf.sapt.R;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.Connect2ServiceThread;
import com.psf.sapt.wifiService.DataProgress;
import com.psf.sapt.wifiService.OnDataUpdateListener;

import java.io.IOException;


/**
 * Created by psf on 2015/8/20.
 */
public class ButteryMatDetail extends Fragment implements AdapterView.OnItemClickListener,OnDataUpdateListener,
        View.OnClickListener,PopupMenu.OnMenuItemClickListener{
    public static String matFragmentTag="ButteryMat";
    /**
     * 储存接收到的数据
     */
    public byte[] data;
    /**
     * 连接到的模块
     */
    private int[] moduleConn=null;
    /**
     * 防止连接service时间过长，显示ProgressDialog
     */
    public ProgressDialog pd;
    /**
     * 是否在选择模式下
     */
    private boolean isSelectMode=false;
    /**
     * 各模块的选择情况 0号为BMUs
     */
    private boolean[] selectedItems=null;
    /**
     * 每次从接收到的数据里面分析当前是否有报警模块
     */
    private boolean[] problems=null;
    /**
     * 用于对每个模块的报警动画进行记录
     */
    private boolean[] animationStart=null;
    /**
     * 报警声音是否暂停，true表示暂停，false表示没有暂停
     */
    private boolean ispause=false;
    /**
     * lecu最高温度值
     */
    private float tempMax=0;
    /**
     * lecu最低温度值
     */
    private float tempMin=0;
    /**
     * lecu最大单体电压值
     */
    private float voltMax=0;
    /**
     * lecu最小单体电压值
     */
    private float voltMin=0;
    /**
     * 多功能选择界面
     */
    private View selectOption;
    /**
     * MediaPlayer实例，用于播放报警声
     */
    private MediaPlayer mplayer=null;
    /**
     * 是否停止播放报警声，true表示已经停止，false表示没有停止
     */
    private boolean isStopWarningMusic=false;
    /**
     * 连接CanService的线程，在mainActivity没有连接上服务的时候用于后台连接服务
     */
    Connect2ServiceThread connect2ServiceThread;
    /**
     * 是否显示扫描模块结果
     */
    boolean showResult=true;
    /**
     * 用于后台服务数据交换
     */
    public Handler mhandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    if(pd!=null){
                        if(pd.isShowing()){
                            pd.dismiss();
                        }
                    }
                    getService();
                    break;
            }
            return true;
        }
    });
    Button seeMore;
    PopupMenu selectMenu;
    Button selectAll;
    Button selectInv;
    Button cancel;
    Button confirm;

    DataAdapter madapter;
    DataProgress mprogress; //处理帧数据的类
    mainActivity holderActivity=null;
    CanService mservice=null;

    //通知接收到新的数据
    public void OnDataChanged() {
        mprogress.setFrames(data);
        for(int i=0;i<mprogress.getId().length;i++){
            float[] dataFloat= mprogress.getFrameFloatData(i);
            //Log.v("dataTag",(mprogress.getID() / 16-0x10) + " 模块");
            //moduleConn[mprogress.getID()/256-1]=true;//设置连接模块
            //isModuleConnChange[mprogress.getID()/256-1]=true;
            if(mprogress.getId()[i]>0x100&&mprogress.getId()[i]<0x2a0){
                switch (mprogress.getId()[i]&0x00f){
                    case 0x000:
                        //检测到电压或者温度超出范围
                        boolean hasFalse=false;
                        if(dataFloat[0]!=9999f){
                            if(dataFloat[0]>voltMax){
                                hasFalse=true;
                            }
                        }
                        if(dataFloat[0]!=9999f){
                            if(dataFloat[1]<voltMin){
                                hasFalse=true;
                            }
                        }
                        if(dataFloat[2]!=9999f){
                            if(dataFloat[2]>tempMax){
                                hasFalse=true;
                            }
                        }
                        if(dataFloat[3]!=9999f){
                            if(dataFloat[3]<tempMin){
                                hasFalse=true;
                            }
                        }
                        Log.v("dataTag",mprogress.getId()[i]+"id");
                        problems[((mprogress.getId()[i]-0x100)>>4)]=hasFalse;

                        break;
                }
            }
        }
        mprogress.setAllowSetNewData();
        madapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //getFragmentManager().beginTransaction().replace(R.id.container,new ButteryMatItemDetail()).commit();
        Log.v("dataTag", "onItemOnclick");
        if(isSelectMode){
            if(selectedItems==null){
                selectedItems=new boolean[41];
                for(int i=0;i<41;i++){
                    selectedItems[i]=false;
                }
            }
            if(position==0){
                return;
            }
            if(selectedItems[position]){
                selectedItems[position]=false;
            }else {
                selectedItems[position]=true;
            }
            madapter.notifyDataSetChanged();

        }else{
            if(containID(position)){
                FragmentTransaction ft=getFragmentManager().beginTransaction();
                if(position==0){
                    ft.replace(R.id.container, ((BmuDetailFragment)holderActivity.getFragment(13)));
                }else{
                    ft.replace(R.id.container, ((ButteryMatItemDetail)holderActivity.getFragment(12)).setArguements(position));
                }

                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                if(ft.isAddToBackStackAllowed()){
                    ft.addToBackStack(null);
                }
                ft.commit();
            }
        }


    }

    @Override
    public void OnDataUpdate(byte[] data) {
        this.data=data;
        if(ispause) return;
        OnDataChanged();
    }

    @Override
    public void OnScanCompleted() {
        //pd.dismiss();
        if(!mservice.hasStartGetData){
            mservice.getData2();
        }
        moduleConn=mservice.getLcuModuleIDs();
        if(moduleConn!=null){
            if(showResult){
                if(containID(0)){
                    Toast.makeText(holderActivity,"bmu:"+"1个   "+"lecu：" + (moduleConn.length-1), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holderActivity, "lecu个数为：" + moduleConn.length, Toast.LENGTH_SHORT).show();
                }

                showResult=false;
            }

        }else{
            if(showResult){
                Toast.makeText(holderActivity, "lecu个数为：" + 0+"扫描失败", Toast.LENGTH_SHORT).show();
                showResult=false;
            }
        }

        if(moduleConn==null||moduleConn.length==0){
            if(mplayer!=null&&mplayer.isPlaying()){
                mplayer.pause();
            }
        }
        madapter.notifyDataSetChanged();
    }

    @Override
    public void OnSendResult(int result) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.more_operation:
                selectMenu=new PopupMenu(getActivity(),v);
                selectMenu.inflate(R.menu.mat_select_menu);
                selectMenu.setOnMenuItemClickListener(ButteryMatDetail.this);
                selectMenu.show();
                break;
            case R.id.mat_select_all:
                if(selectedItems==null){
                    selectedItems=new boolean[41];
                    for(int i=0;i<41;i++){
                        selectedItems[i]=false;
                    }
                }
                for(int i=0;i<41;i++){
                    if(containID(i)) {
                        selectedItems[i] = true;
                    }
                }
                madapter.notifyDataSetChanged();
                break;
            case R.id.mat_select_inv:
                if(selectedItems==null){
                    selectedItems=new boolean[41];
                    for(int i=0;i<41;i++){
                        selectedItems[i]=false;
                    }
                }
                for(int i=0;i<41;i++){
                    if(containID(i)){
                        if(selectedItems[i]){
                            selectedItems[i]=false;
                        }else{
                            selectedItems[i]=true;
                        }
                    }
                }

                madapter.notifyDataSetChanged();
                break;
            case R.id.mat_select_cancel:
                isSelectMode=false;
                selectOption.setVisibility(View.GONE);
                madapter.notifyDataSetChanged();
                break;
            case R.id.mat_select_confirm:
                isSelectMode=false;
                FragmentTransaction ft=getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new ItemGraphFragment().setArguements(selectedItems,false));
                ft.commit();
                break;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_mat_scan:
                if(mservice.getCanConnectDetail(200,1000)){
                    moduleConn=null;
                    for(int i=0;i<41;i++){
                        problems[i]=false;
                    }
                    if(mplayer!=null){
                        if(mplayer.isPlaying()){
                            mplayer.pause();
                        }
                    }
                    madapter.notifyDataSetChanged();
                    showResult=true;
                }

                break;
            case R.id.menu_mat_select:
                Toast.makeText(getActivity(),"you click sencond choose",Toast.LENGTH_SHORT).show();
                isSelectMode=true;
                madapter.notifyDataSetChanged();
                selectOption.setVisibility(View.VISIBLE);
                break;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mprogress=new DataProgress();
        //对选择模块进行标识
        if(selectedItems==null){
            selectedItems=new boolean[41];
            for(int i=0;i<41;i++){
                selectedItems[i]=false;
            }
        }
        //对问题模块进行标识
        if(problems==null){
            problems=new boolean[41];
            for(int i=0;i<41;i++){
                problems[i]=false;
            }
        }
        if(animationStart==null){
            animationStart=new boolean[41];
            for(int i=0;i<41;i++){
                animationStart[i]=false;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v("dataTag", "ButteryMatDetail onAttach");
        if(holderActivity==null){
            holderActivity=(mainActivity)activity;
            holderActivity.currentFragment=0;
            tempMax=holderActivity.getTempHigh();
            tempMin=holderActivity.getTempLow();
            voltMax=holderActivity.getVoltHigh();
            voltMin=holderActivity.getVoltLow();
            mservice=holderActivity.getService();
            getService();
        }else{
            if(mservice!=null){
                mservice.setListener(this, "from ButteryMatDetail");
                //mservice.getCanConnectDetail();
            }else{
                getService();
            }
        }
        if(mplayer==null){
            mplayer=MediaPlayer.create(getActivity(),R.raw.mat_error_warning);
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


    }

    public void getService(){
        if(mservice==null){
            if(!holderActivity.isBind){
                //pd=new ProgressDialog(getActivity());
                connect2ServiceThread=new Connect2ServiceThread(holderActivity,mhandler);
                connect2ServiceThread.start();
            }else{
                mservice=holderActivity.getService();
                mservice.setListener(this,"from ButteryMatDetail");
                mservice.getCanConnectDetail(200,1000);
                moduleConn=holderActivity.modules;
            }

        }


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("dataTag", "ButteryMatDetail is paused");
        ispause=true;
        if(mplayer.isPlaying()){
            mplayer.pause();
            isStopWarningMusic=true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ispause=false;
        Log.v("dataTag", "ButteryMatDetail is resume");
        if(isStopWarningMusic){
            mplayer.start();
            isStopWarningMusic=false;
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v("dataTag","mat detail destroy view");
        if(connect2ServiceThread!=null){
            connect2ServiceThread.flag=true;
        }
        if(mplayer.isPlaying()){
            mplayer.pause();
            isStopWarningMusic=true;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tempMax=holderActivity.getTempHigh();
        tempMin=holderActivity.getTempLow();
        voltMax=holderActivity.getVoltHigh();
        voltMin=holderActivity.getVoltLow();
        if(mservice!=null){
            moduleConn=mservice.getLcuModuleIDs();
            mservice.setListener(this,"matDetail");
        }
        holderActivity.fragmentOne=1;
        View rootView=inflater.inflate(R.layout.battery_mat,container,false);
        selectOption=rootView.findViewById(R.id.mat_select_mode_option);
        seeMore=(Button)rootView.findViewById(R.id.more_operation);
        seeMore.setOnClickListener(this);
        selectAll=(Button)rootView.findViewById(R.id.mat_select_all);
        selectAll.setOnClickListener(this);
        selectInv=(Button)rootView.findViewById(R.id.mat_select_inv);
        selectInv.setOnClickListener(this);
        cancel=(Button)rootView.findViewById(R.id.mat_select_cancel);
        cancel.setOnClickListener(this);
        confirm=(Button)rootView.findViewById(R.id.mat_select_confirm);
        confirm.setOnClickListener(this);
        //searchModule=(Button)rootView.findViewById(R.id.search_module);
        //searchModule.setOnClickListener(this);
        //seeGraph=(Button)rootView.findViewById(R.id.see_graph);
        //seeGraph.setOnClickListener(this);
        GridView mgview=(GridView)rootView.findViewById(R.id.buttery_group);
        madapter=new DataAdapter(getActivity());
        mgview.setAdapter(madapter);
        mgview.setOnItemClickListener(this);
        return rootView;
    }

    //检查是否连接点击模块
    public boolean containID(int position){
       // if(position==0) return true;
        if(moduleConn!=null){
            for(int i=0;i<moduleConn.length;i++){
                if(moduleConn[i]==position){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mplayer!=null){
            mplayer.release();
            mplayer=null;
        }
    }

    //数据适配器
    public class DataAdapter extends BaseAdapter{
        Context context;
        LayoutInflater minflater;
        int position;
        adapterViewHolder mholder;
        DataAdapter(Context context){
            this.context=context;
            minflater=LayoutInflater.from(context);

        }
        @Override
        public int getCount() {
            return 41;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            this.position=position;
            if(convertView!=null){
                mholder=(adapterViewHolder)convertView.getTag();
                if(containID(position)){
                    if(isSelectMode&&selectedItems[position]){
                        mholder.mbox.setVisibility(View.VISIBLE);
                    }else{
                        mholder.mbox.setVisibility(View.GONE);
                    }
                    mholder.butteryOrder.setTextColor(getResources().getColor(R.color.green));
                    if(position==0){
                        if(isSelectMode&&selectedItems[position]){
                            mholder.mbox.setVisibility(View.VISIBLE);
                        }
                        if(problems[position]){
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item_error);
                            if(!mplayer.isPlaying()){
                                //Log.v("dataTag","playing not null");
                                isStopWarningMusic=false;
                                mplayer.start();
                            }
                            if(mholder.animator!=null){
                                if(mholder.animator.isRunning()){
                                    if(mholder.animator.isPaused()){
                                        mholder.animator.resume();
                                    }
                                }else{
                                    mholder.animator.setTarget(mholder.butteryBg);
                                    mholder.animator.start();
                                }
                            }else{
                                mholder.animator=AnimatorInflater.loadAnimator(getActivity(),R.animator.mat_error_show);
                                mholder.animator.setTarget(mholder.butteryBg);
                                mholder.animator.start();
                            }
                        }else{
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item_bmu);
                            if(mholder.animator!=null&&mholder.animator.isRunning()){
                                mholder.animator.cancel();
                            }
                        }
                    }else{
                        if(problems[position]){
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item_error);
                            if(!mplayer.isPlaying()){
                                isStopWarningMusic=false;
                                mplayer.start();
                            }
                            if(mholder.animator!=null){
                                if(mholder.animator.isRunning()){
                                    if(mholder.animator.isPaused()){
                                        mholder.animator.resume();
                                    }
                                }else{
                                    mholder.animator.setTarget(mholder.butteryBg);
                                    mholder.animator.start();
                                }
                            }else{
                                mholder.animator=AnimatorInflater.loadAnimator(getActivity(),R.animator.mat_error_show);
                                mholder.animator.setTarget(mholder.butteryBg);
                                mholder.animator.start();
                            }

                        }else{
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item);
                            if(mholder.animator!=null&&mholder.animator.isRunning()){
                                mholder.animator.cancel();
                            }
                        }
                    }
                }else{
                    mholder.butteryOrder.setTextColor(getResources().getColor(R.color.blue_color));
                    if(position==0){
                        mholder.butteryBg.setImageResource(R.drawable.buttery_item_bmu_gray);
                    }else{
                        mholder.butteryBg.setImageResource(R.drawable.buttery_item_gray);
                    }
                    if(mholder.animator!=null){
                        if(mholder.animator.isRunning()){
                            mholder.animator.cancel();
                        }
                    }



                }
            }else{
                mholder=new adapterViewHolder();
                convertView=minflater.inflate(R.layout.buttery_grid_item,parent,false);
                mholder.butteryBg=(ImageView)convertView.findViewById(R.id.buttery_mat_item_bg);
                mholder.butteryOrder=(TextView)convertView.findViewById(R.id.buttery_mat_order);
                mholder.mbox=(TextView)convertView.findViewById(R.id.select_item_graph);
                if(containID(position)){
                    //Log.v("dataTag", position + "模块 高亮1");
                    if(position==0){
                        mholder.butteryBg.setImageResource(R.drawable.buttery_item_bmu);
                        mholder.butteryOrder.setTextColor(getResources().getColor(R.color.green));
                        //超出范围的模块设置动画
                        if(problems[position]){
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item_error);
                            if(!mplayer.isPlaying()){
                                //Log.v("dataTag","playing null");
                                isStopWarningMusic=false;
                                mplayer.start();
                            }
                            //Log.v("dataTag","position"+animationStart[position]);
                            mholder.animator=AnimatorInflater.loadAnimator(getActivity(),R.animator.mat_error_show);
                            mholder.animator.setTarget(mholder.butteryBg);
                            mholder.animator.start();

                        }else {
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item_bmu);
                            if(mholder.animator!=null&&mholder.animator.isRunning()){
                                mholder.animator.cancel();
                            }
                        }
                    }else{
                        mholder.butteryBg.setImageResource(R.drawable.buttery_item);
                        mholder.butteryOrder.setTextColor(getResources().getColor(R.color.green));
                        //超出范围的模块设置动画
                        if(problems[position]){
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item_error);
                            if(!mplayer.isPlaying()){
                                isStopWarningMusic=false;
                                mplayer.start();
                            }
                            //Log.v("dataTag", "position" + animationStart[position]);
                            mholder.animator=AnimatorInflater.loadAnimator(getActivity(),R.animator.mat_error_show);
                            mholder.animator.setTarget(mholder.butteryBg);
                            mholder.animator.start();
                        }else{
                            mholder.butteryBg.setImageResource(R.drawable.buttery_item);
                            if(mholder.animator!=null&&mholder.animator.isRunning()){
                                mholder.animator.cancel();
                            }
                        }
                    }
                    if(isSelectMode&&selectedItems[position]){
                       // Log.v("dataTag", "setgou" + position);
                        mholder.mbox.setVisibility(View.VISIBLE);

                    }else{
                        mholder.mbox.setVisibility(View.GONE);
                    }
                 //   Log.v("dataTag","   "+selectedItems[position]);
                } else {
                    //Log.v("dataTag", "  null" + position);
                    if(position==0){
                        mholder.butteryBg.setImageResource(R.drawable.buttery_item_bmu_gray);
                    }else {
                        mholder.butteryBg.setImageResource(R.drawable.buttery_item_gray);
                    }
                    mholder.mbox.setVisibility(View.GONE);
                }
                mholder.butteryOrder.setText(String.valueOf(String.format("%02d",position)));
                convertView.setTag(mholder);
            }

            /*
            }
            */
            boolean hasProblems=false;
            for(int i=0;i<41;i++){
                if(problems[i]){
                    hasProblems=true;
                    break;
                }
            }
            if(!hasProblems){
                if(mplayer!=null&&mplayer.isPlaying()){
                    mplayer.pause();
                }
            }
            return convertView;
        }
    }
    public class adapterViewHolder {
        TextView butteryOrder;
        ImageView butteryBg;
        TextView mbox;
        Animator animator;
    }

}
