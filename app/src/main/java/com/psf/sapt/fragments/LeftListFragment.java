package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.psf.sapt.mainActivity;
import com.psf.sapt.R;

/**
 * Created by psf on 2015/8/19.
 */
public class LeftListFragment extends Fragment implements AdapterView.OnItemClickListener{

    final String[] listItem={
        "电池巡检",
            "模块ID标定",
            "数据存储",
            "历史回放",
            "故障记录和分析",
            "设置"
    };
    int listPosition=0;
    ListView mlist;
    mainActivity holderActivity;
    public LeftListFragment getInstance(int index){
        return new LeftListFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        holderActivity=(mainActivity)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listmain=inflater.inflate(R.layout.list_main,container,false);
        mlist=(ListView)listmain.findViewById(R.id.item_menu);
        mBaseAdapter listAdapter=new mBaseAdapter(getActivity());
        if(savedInstanceState!=null){
            listPosition=savedInstanceState.getInt("clickItem");
        }
        listAdapter.setSelectedItem(listPosition, true);
        //ArrayAdapter<String> mlAdapter=new ArrayAdapter<String>(getActivity(),R.layout.simple_list_item,R.id.simple_list_item_text1,listItem);
        mlist.setAdapter(listAdapter);
        mlist.setOnItemClickListener(this);
        replaceFragmentAll(listPosition);
        return listmain;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBaseAdapter adapter=(mBaseAdapter)mlist.getAdapter();
        adapter.setSelectedItem(position,true);
        adapter.notifyDataSetChanged();
        listPosition=position;
        holderActivity.currentFragment=listPosition;
        if(listPosition==0){

        }
        replaceFragmentAll(listPosition);

    }


    public void replaceFragmentAll(int position){
        FragmentManager manager=getFragmentManager();
        for(int i=0;i<manager.getBackStackEntryCount();i++){
            manager.popBackStack();
        }
            switch (position){
            case 0:manager.beginTransaction().replace(R.id.container,holderActivity.getFragment(11))
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                break;
            //case 1:manager.beginTransaction().replace(R.id.container,holderActivity.getFragment(1))
              //      .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                //break;
            case 1:manager.beginTransaction().replace(R.id.container,holderActivity.getFragment(2))
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                break;
            case 2:manager.beginTransaction().replace(R.id.container,holderActivity.getFragment(3))
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                break;
            case 3:manager.beginTransaction().replace(R.id.container,holderActivity.getFragment(4))
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                break;
            case 4:manager.beginTransaction().replace(R.id.container, holderActivity.getFragment(5))
                    .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                break;
            case 5:
                manager.beginTransaction().replace(R.id.container, holderActivity.getFragment(6))
                        .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();
                break;
            default:
                break;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("clickItem",listPosition);
    }

    class mBaseAdapter extends BaseAdapter{
        LayoutInflater minflator;
        boolean selectfirst=true;
        int selectedItem;
        mBaseAdapter(Context context){
            selectfirst=true;
            minflator=LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return listItem.length;
        }

        @Override
        public Object getItem(int position) {
            return listItem[position];
        }
        public void setSelectedItem(int position,boolean isfirst){
            selectedItem=position;
            selectfirst=isfirst;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView=minflator.inflate(R.layout.simple_list_item,parent,false);
            TextView itemview=(TextView)rootView.findViewById(R.id.simple_list_item_text1);
            itemview.setText(listItem[position]);
            if(position==selectedItem&&selectfirst){
                Log.v("TAG","set selected 0 successful");
                rootView.setBackgroundColor(getResources().getColor(R.color.list_item_click_bg));
            }
            return rootView;
        }
    }
}
