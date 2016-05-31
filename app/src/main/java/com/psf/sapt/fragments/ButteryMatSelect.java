package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.psf.sapt.mainActivity;
import com.psf.sapt.R;

/**
 * Created by psf on 2015/9/7.
 */
public class ButteryMatSelect extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener{
    private ImageView bmuSelector=null;
    private GridView butteryMat=null;
    private boolean hasSelectedBmu=false;
    private mBaseAdapter mAdapter=null;
    private boolean[] moduleSelect=null;
    private Button setup=null;
    mainActivity holdActivity=null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(moduleSelect==null){
            moduleSelect=new boolean[40];
            for(int i=0;i<40;i++){
                moduleSelect[i]=false;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        holdActivity=(mainActivity)activity;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        holdActivity.fragmentOne=0;
        View rootView=inflater.inflate(R.layout.buttery_mat_select,container,false);
        bmuSelector=(ImageView)rootView.findViewById(R.id.bmu_selector);
        butteryMat=(GridView)rootView.findViewById(R.id.module_selector);
        setup=(Button)rootView.findViewById(R.id.start_detect);
        setup.setOnClickListener(this);
        mAdapter=new mBaseAdapter(getActivity());
        butteryMat.setAdapter(mAdapter);
        bmuSelector.setOnClickListener(this);
        butteryMat.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.bmu_selector:
                ImageView bmu=(ImageView)v;
                if(hasSelectedBmu){
                    bmu.setImageResource(R.drawable.bmu_selector_no);
                    hasSelectedBmu=false;
                }else {
                    bmu.setImageResource(R.drawable.bmu_selector);
                    hasSelectedBmu=true;
                }
                break;
            case R.id.start_detect:
                FragmentTransaction ft=getFragmentManager().beginTransaction();
                ft.replace(R.id.container,holdActivity.getFragment(11))
                        .setTransition(FragmentTransaction.TRANSIT_UNSET);
                ft.addToBackStack(null);
                ft.commit();

        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        moduleSelect[position]=moduleSelect[position]? false:true;
    }

    class mBaseAdapter extends BaseAdapter{
        LayoutInflater inflater;
        ViewHolder mholder=null;
        public mBaseAdapter(Context context){
            inflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 40;
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
            if(convertView==null){
                mholder=new ViewHolder();
                convertView=inflater.inflate(R.layout.buttery_mat_select_item,parent,false);
                mholder.checkBox=(CheckBox)convertView.findViewById(R.id.module_select_check);
                mholder.order=(TextView)convertView.findViewById(R.id.module_order);
                mholder.order.setText(String.valueOf(position+1));
                convertView.setTag(mholder);
            }else{
                mholder=(ViewHolder)convertView.getTag();
            }

            return convertView;
        }
    }
    class ViewHolder{
        CheckBox checkBox;
        TextView order;
    }

}
