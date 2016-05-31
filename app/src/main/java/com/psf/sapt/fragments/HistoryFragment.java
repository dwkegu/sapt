package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.psf.sapt.PreferencesString;
import com.psf.sapt.R;
import com.psf.sapt.drawing.HistoryDrawView;
import com.psf.sapt.mainActivity;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by psf on 2015/8/19.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener,View.OnLongClickListener{

    HistoryDrawView historySurfaceView;
    Button switcherButton;
    ListView dataSelectView;
    private boolean showSelect=true;
    String[] fileStrings=null;
    mainActivity holdActivity=null;

    @Override
    public void onClick(View v) {
        //视图切换
        switch (v.getId()){
            case R.id.data_select_switch:
                if(!showSelect&&(dataSelectView.getVisibility()==View.GONE)){
                    showSelect=true;
                    switcherButton.setVisibility(View.INVISIBLE);
                    dataSelectView.setVisibility(View.VISIBLE);
                    historySurfaceView.setVisibility(View.GONE);
                }
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(historySurfaceView.init(fileStrings[position],getActivity())){
            historySurfaceView.setLimits(holdActivity.getVoltHigh(),holdActivity.getVoltLow(),
                    holdActivity.getTempHigh(),holdActivity.getTempLow());
            showSelect=false;
            switcherButton.setVisibility(View.VISIBLE);
            dataSelectView.setVisibility(View.GONE);
            historySurfaceView.setVisibility(View.VISIBLE);
        }else{
            Toast.makeText(getActivity(),"打开文件失败",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((mainActivity)activity).currentFragment=3;
        holdActivity=(mainActivity)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View detailHistory=inflater.inflate(R.layout.history_fragment, container, false);
        historySurfaceView=(HistoryDrawView)detailHistory.findViewById(R.id.history_surf);
        switcherButton=(Button)detailHistory.findViewById(R.id.data_select_switch);
        dataSelectView=(ListView)detailHistory.findViewById(R.id.data_select_list);
        switcherButton.setOnClickListener(this);
        dataSelectView.setOnItemClickListener(this);
        dataSelectView.setOnLongClickListener(this);
        dataSelectView.setAdapter(new historyAdapter(getActivity()));
        if(showSelect){
            switcherButton.setVisibility(View.INVISIBLE);
            dataSelectView.setVisibility(View.VISIBLE);
            historySurfaceView.setVisibility(View.GONE);
        }else {
            dataSelectView.setVisibility(View.GONE);
            historySurfaceView.setVisibility(View.VISIBLE);
        }
        return detailHistory;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showSelect=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class historyAdapter extends BaseAdapter{

        LayoutInflater inflater;
        String path;
        ViewHolder mholder=null;
        public historyAdapter(Context context){
            inflater=LayoutInflater.from(context);
            path= Environment.getExternalStorageDirectory().toString()+ PreferencesString.DATASAVINGSTRING;
            File maindir=new File(path);
            if(maindir.exists()&&maindir.isDirectory()){
                fileStrings=maindir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if(filename.substring(filename.length()-3,filename.length()).equals("xls")){
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
        @Override
        public int getCount() {
            if(fileStrings==null){
                return 0;
            }
            return fileStrings.length;
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
            if(convertView!=null){
                mholder=(ViewHolder)convertView.getTag();
                if(mholder.position!=position){
                    mholder.fileNameText.setText(fileStrings[position]);
                    mholder.position=position;
                }
            }else{
                mholder=new ViewHolder();
                convertView=inflater.inflate(R.layout.grid_item_history,parent,false);
                mholder.fileNameText=(TextView)convertView.findViewById(R.id.history_item_text);
                mholder.fileNameText.setText(fileStrings[position]);
                mholder.position=position;
                convertView.setTag(mholder);
            }

            return convertView;
        }
    }
class ViewHolder{
    TextView fileNameText;
    int position;
}

}
