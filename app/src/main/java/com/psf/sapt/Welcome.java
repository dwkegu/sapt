package com.psf.sapt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

/**开始欢迎界面
 * Created by psf on 2015/9/8.
 */

/**
 * 程序启动的欢迎界面实现2秒跳转，跳转界面布局为R.layout.welcome
 */
public class Welcome extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        Timer timer=new Timer();
        final Intent localIntent=new Intent(this,mainActivity.class);
        TimerTask mTask=new TimerTask() {
            @Override
            public void run() {
                startActivity(localIntent);
                Welcome.this.finish();
            }
        };
        timer.schedule(mTask,2000);
    }


}
