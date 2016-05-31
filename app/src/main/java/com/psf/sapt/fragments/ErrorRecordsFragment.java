package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.psf.sapt.mainActivity;
import com.psf.sapt.R;
import com.psf.sapt.dataHelper;

/**
 * Created by psf on 2015/8/21.
 */
public class ErrorRecordsFragment extends Fragment {
    SQLiteDatabase mdb;
    dataHelper mhelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((mainActivity)activity).currentFragment=4;
        mhelper=new dataHelper(getActivity(),"mdb",1);
        mdb=mhelper.getReadableDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.error_records,container,false);
        GridView errorGrid=(GridView)rootView.findViewById(R.id.error_grid);
        mBaseAdapter adapter=new mBaseAdapter(getActivity(),mdb);
        errorGrid.setAdapter(adapter);
        return rootView;
    }
    class mBaseAdapter extends BaseAdapter{
        LayoutInflater inflater;
        SQLiteDatabase mdb;
        ViewHolder mHolder;
        Cursor cursor;
        mBaseAdapter(Context context,SQLiteDatabase db){
            inflater=LayoutInflater.from(context);
            mdb=db;
            cursor=mdb.query(dataHelper.table_errorRecords,null,null,null,null,null,null);
        }
        @Override
        public int getCount() {
            return mdb.query(dataHelper.table_errorRecords,null,null,null,null,null,null).getCount()*4+4;
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
            if(mHolder==null){
                mHolder=new ViewHolder();
            }
            if(convertView==null){
                convertView=inflater.inflate(R.layout.grid_item_history,parent,false);
                mHolder.mtext=(TextView)convertView.findViewById(R.id.history_item_text);
                if(position<4){
                    mHolder.mtext.setText(dataHelper.errorRecordsProjections[position+1]);
                }else{
                    switch (position/4){
                        case 0:mHolder.mtext.setText(cursor.getString(1)); break;
                        case 1:mHolder.mtext.setText(cursor.getString(2)); break;
                        case 2:mHolder.mtext.setText(cursor.getString(3)); break;
                        case 3:mHolder.mtext.setText(cursor.getString(4)); break;
                        default:break;
                    }
                }
                convertView.setTag(mHolder);

            }else{
                mHolder=(ViewHolder)convertView.getTag();
            }
            return convertView;
        }
    }
    class ViewHolder{
        TextView mtext;
    }
}
