package com.psf.sapt;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * 用于生成数据库保存数据，已经弃用
 * Created by psf on 2015/8/20.
 */
public class dataHelper extends SQLiteOpenHelper implements BaseColumns{
    private static String DataBaseName="mdb";
    private static int versionOfDatabase=1;
    public final String create="create table ";
    public static String table_user="userTable";
    public static String table_history="history";
    public static String table_errorRecords="Records";
    /*
    projections of user table
     */
    public static String[] userProjections={
        _ID,"username","password"
    };
    /*
    projections for history data
     */
    public static String[] historyProjections={
        _ID,"系统总压","系统SOC","系统电流",
            "报警等级","最高温度","最低温度","最大电压",
            "最低电压","故障事件","故障时间"
    };
    /*
    projections for records of error and analyse
     */
    public static String[] errorRecordsProjections={
        _ID,"故障事件","故障时间","故障分析","接收时间"
    };
    public dataHelper(Context context){
        super(context,DataBaseName,null,versionOfDatabase);
    }
    public dataHelper(Context context,String name){
        super(context,name,null,versionOfDatabase);
    }
    public dataHelper(Context context,String name,int version){
        super(context,name,null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建三个表分别储存用户注册信息，历史记录和故障记录与分析
        db.execSQL(create + table_user + "(" +
                userProjections[0] + " integer primary key," +
                userProjections[1] + " text not null," +
                userProjections[2] + " text not null)");
        ContentValues mvalues=new ContentValues();
        mvalues.put(userProjections[1],"admin000");
        mvalues.put(userProjections[2],"admin123456");
        db.insert(table_user,null,mvalues);
        db.execSQL(create+table_history+"("+
            historyProjections[0]+" INTEGER PRIMARY KEY,"+
            historyProjections[1]+" real not null,"+
            historyProjections[2]+" text,"+
            historyProjections[3]+" real not null,"+
            historyProjections[4]+" integer not null,"+
            historyProjections[5]+" real,"+
            historyProjections[6]+" real,"+
            historyProjections[7]+" real,"+
            historyProjections[8]+" real,"+
            historyProjections[9]+" text,"+
            historyProjections[10]+" text not null)");
        db.execSQL(create+table_errorRecords+"("+
            errorRecordsProjections[0]+" INTEGER PRIMARY KEY,"+
            errorRecordsProjections[1]+" text not null,"+
            errorRecordsProjections[2]+" text not null,"+
            errorRecordsProjections[3]+" text,"+
            errorRecordsProjections[4]+" text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE IS EXISTS "+table_user);
        db.execSQL("DROP IF TABLE IS EXISTS "+table_history);
        db.execSQL("DROP IF TABLE IS EXISTS "+table_errorRecords);
        onCreate(db);
    }
}
