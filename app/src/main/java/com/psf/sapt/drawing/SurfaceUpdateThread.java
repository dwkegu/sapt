package com.psf.sapt.drawing;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by psf on 2015/9/10.
 */
public class SurfaceUpdateThread extends Thread {
    private SurfaceHolder sh;
    private SurfaceView sv;
    private boolean start=false;
    private boolean stopFlag=false;
    public SurfaceUpdateThread(SurfaceView sv,SurfaceHolder sh){
        this.sh=sh;
        this.sv=sv;
    }
    public boolean hadStart(){
        return start;
    }
    @Override
    public void run() {
        super.run();
        start=true;
        Canvas c=null;
        while (!stopFlag){
            try{
                c=sh.lockCanvas(null);
                try{
                    sv.draw(c);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }finally {
                if(c!=null){
                    sh.unlockCanvasAndPost(c);
                }
            }
            try {
                Thread.sleep(50);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }
    public void setStopFlag(boolean flag){
        stopFlag=flag;
    }
}
