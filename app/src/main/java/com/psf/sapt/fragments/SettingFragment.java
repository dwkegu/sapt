package com.psf.sapt.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.psf.sapt.PreferencesString;
import com.psf.sapt.R;
import com.psf.sapt.mainActivity;

/**
 * Created by psf on 2015/8/21.
 */
public class SettingFragment extends Fragment implements View.OnClickListener{

    //Button register;
    //login control
    EditText nameInput;
    EditText passWord1;
    Button login;
    View mdialog=null;
    AlertDialog.Builder mbulder;

    //setting control
    EditText tempMax,tempMin,voltMax,voltMin;
    EditText wifiSSID,moduleAddress;
    Spinner biaodingSetting;
    Button wifiConfirm;
    Button setConfirm;
    LinearLayout mhint,settingGroup;
    ViewGroup container;

    SharedPreferences msp;
    private boolean isLogin=false;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wifi_detail_confirm:

                break;
            case R.id.confirm_param_setting:
                SharedPreferences.Editor mEditor=msp.edit();
                if(!tempMax.getText().toString().trim().equals("")){
                    mEditor.putFloat(PreferencesString.TEMPHIGH,Float.parseFloat(tempMax.getText().toString().trim()));
                    mainActivity.tempHigh=Float.parseFloat(tempMax.getText().toString().trim());
                }
                if(!tempMin.getText().toString().trim().equals("")){
                    mEditor.putFloat(PreferencesString.TEMPLOW,Float.parseFloat(tempMin.getText().toString().trim()));
                    mainActivity.tempLow=Float.parseFloat(tempMin.getText().toString().trim());
                }
                if(!voltMax.getText().toString().trim().equals("")){
                    mEditor.putFloat(PreferencesString.VOLTHIGH,Float.parseFloat(voltMax.getText().toString().trim()));
                    mainActivity.voltHigh=Float.parseFloat(voltMax.getText().toString().trim());
                }
                if(!voltMin.getText().toString().trim().equals("")){
                    mEditor.putFloat(PreferencesString.VOLTLOW,Float.parseFloat(voltMin.getText().toString().trim()));
                    mainActivity.voltLow=Float.parseFloat(voltMin.getText().toString().trim());
                }
                mEditor.putInt(PreferencesString.BIAODINGWAY,biaodingSetting.getSelectedItemPosition());
                Log.v("dataTag","params:"+mainActivity.tempHigh+"  "+mainActivity.tempLow+
                "  "+mainActivity.voltHigh+"   "+mainActivity.voltLow);
                mEditor.apply();
                Toast.makeText(getActivity(),"更改成功",Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting_login:
                mdialog=LayoutInflater.from(getActivity()).inflate(R.layout.login_dialog,container,false);
                nameInput=(EditText)mdialog.findViewById(R.id.new_user_name);
                passWord1=(EditText)mdialog.findViewById(R.id.new_user_password1);
                mbulder=new AlertDialog.Builder(getActivity());
                mbulder.setTitle("请登入！").setView(mdialog).setMessage("登入后设置")
                        .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (nameInput.getText().toString().trim().equals("123")) {
                                    if (passWord1.getText().toString().trim().equals("123456")) {
                                        Toast.makeText(getActivity(), "登入成功", Toast.LENGTH_SHORT).show();
                                        isLogin = true;
                                        settingGroup.setVisibility(View.VISIBLE);
                                        mhint.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "用户名错误", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mbulder.show();
                break;
        }
    }
/*
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        getActivity().getSharedPreferences(PreferencesString.PREFERENCES,Context.MODE_PRIVATE).edit()
                .putInt(PreferencesString.BIAODINGWAY,position).commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((mainActivity)activity).currentFragment=5;
        msp=activity.getSharedPreferences(PreferencesString.PREFERENCES, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.setting,container,false);
        mhint=(LinearLayout)rootView.findViewById(R.id.not_login);
        settingGroup=(LinearLayout)rootView.findViewById(R.id.setting_params_group);
        tempMax=(EditText)rootView.findViewById(R.id.MaxTemp);
        tempMax.setHint(String.valueOf(msp.getFloat(PreferencesString.TEMPHIGH,40f)));
        tempMin=(EditText)rootView.findViewById(R.id.MinTemp);
        tempMin.setHint(String.valueOf(msp.getFloat(PreferencesString.TEMPLOW,-10f)));
        voltMax=(EditText)rootView.findViewById(R.id.MaxVol);
        voltMax.setHint(String.valueOf(msp.getFloat(PreferencesString.VOLTHIGH,4f)));
        voltMin=(EditText)rootView.findViewById(R.id.MinVol);
        voltMin.setHint(String.valueOf(msp.getFloat(PreferencesString.VOLTLOW, 0.4f)));
        wifiSSID=(EditText)rootView.findViewById(R.id.wifi_ssid_setting);
        moduleAddress=(EditText)rootView.findViewById(R.id.module_address_setting);
        biaodingSetting=(Spinner)rootView.findViewById(R.id.biaoding_setting);
        ArrayAdapter<String> madapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,
                R.id.spinner_text1,new String[]{
                "18串标定",
                "16串标定",
                "12串标定",
                "22串标定"
        });
        biaodingSetting.setAdapter(madapter);
        //biaodingSetting.setOnItemSelectedListener(this);
        wifiConfirm=(Button)rootView.findViewById(R.id.wifi_detail_confirm);
        wifiConfirm.setOnClickListener(this);
        setConfirm=(Button)rootView.findViewById(R.id.confirm_param_setting);
        setConfirm.setOnClickListener(this);
        login=(Button)rootView.findViewById(R.id.setting_login);
        login.setOnClickListener(this);
        this.container=container;
        if(!isLogin){
            mhint.setVisibility(View.VISIBLE);
            settingGroup.setVisibility(View.GONE);
            mdialog=inflater.inflate(R.layout.login_dialog,container,false);
            nameInput=(EditText)mdialog.findViewById(R.id.new_user_name);
            passWord1=(EditText)mdialog.findViewById(R.id.new_user_password1);
            mbulder=new AlertDialog.Builder(getActivity());
            mbulder.setTitle("请登入！").setView(mdialog).setMessage("登入后设置")
                    .setPositiveButton("登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (nameInput.getText().toString().trim().equals("123")) {
                                if (passWord1.getText().toString().trim().equals("123456")) {
                                    Toast.makeText(getActivity(),"登入成功",Toast.LENGTH_SHORT).show();
                                    isLogin=true;
                                    settingGroup.setVisibility(View.VISIBLE);
                                    mhint.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(getActivity(),"密码错误",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "用户名错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mbulder.show();
        }else{
            settingGroup.setVisibility(View.VISIBLE);
            mhint.setVisibility(View.GONE);
        }

       // passWord2=(EditText)rootView.findViewById(R.id.new_user_password2);
        /*
        register=(Button)rootView.findViewById(R.id.add_user);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameInput.getText().toString().trim().length()<6){
                    nameInput.setError("用户名少于6个字节");
                }else if(passWord1.getText().toString().trim().length()<10){
                    passWord1.setError("密码长度少于10位");
                }else if(!passWord1.getText().toString().trim().equals(passWord2.getText().toString().trim())){
                    passWord2.setError("两次密码输入不一样");
                }else {
                    if(mHelper==null){
                        mHelper=new dataHelper(getActivity());
                        mdb=mHelper.getWritableDatabase();
                    }
                    Cursor cursor=mdb.query(dataHelper.table_user, null,
                            dataHelper.userProjections[1]+"=?",new String[]{nameInput.getText().toString().trim()},
                            null, null, null, null);
                    if(cursor.getCount()>0){
                        nameInput.setError("该用户已经存在");
                    }else{
                        cursor.close();
                        ContentValues mvalues=new ContentValues();
                        mvalues.put(dataHelper.userProjections[1], nameInput.getText().toString().trim());
                        mvalues.put(dataHelper.userProjections[2], passWord1.getText().toString().trim());
                        mdb.insert(dataHelper.table_user, null, mvalues);
                        Toast mtoast=Toast.makeText(getActivity(),"注册成功",Toast.LENGTH_SHORT);
                        mtoast.setGravity(Gravity.CENTER,0,0);
                        mtoast.show();
                    }
                }

            }
        });
        */
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
