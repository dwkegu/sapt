package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.psf.sapt.R;
import com.psf.sapt.mainActivity;
import com.psf.sapt.wifiService.CanService;
import com.psf.sapt.wifiService.OnDataUpdateListener;

/**
 * Created by psf on 2015/10/6.
 */
public class BalanceFragment extends Fragment implements OnDataUpdateListener {
    GridView balanceGrid=null;
    private mAdapter adapter=null;
    private mainActivity holdActivity=null;
    private CanService mservice=null;
    public Handler mhandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    getService();
                    break;
            }
            return true;
        }
    });
    @Override
    public void OnDataUpdate(byte[] data) {

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
        if(adapter==null){
            adapter=new mAdapter();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        holdActivity=(mainActivity)activity;
        getService();

    }
    public void getService(){
        if(mservice==null){
            if(!holdActivity.isBind){
                //pd=new ProgressDialog(getActivity());
                //connect2ServiceThread=new Connect2ServiceThread(holdActivity,mhandler);
                //connect2ServiceThread.start();
            }else{
                mservice=holdActivity.getService();
                mservice.setListener(this,"from ButteryMatDetail");
                mservice.getCanConnectDetail(200,1000);
                //moduleConn=holdActivity.modules;
            }

        }


    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.balance_lecu,container,false);
        balanceGrid=(GridView)rootView.findViewById(R.id.balance_grid);
        return rootView;
    }

    class mAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return 0;
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
            return null;
        }
    }
}
