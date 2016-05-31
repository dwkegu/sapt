package com.psf.sapt.drawing;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.psf.sapt.PreferencesString;
import com.psf.sapt.R;

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class HistoryDrawView extends SurfaceView implements SurfaceHolder.Callback ,View.OnTouchListener{
    private int currentTimes=0;
    private long startTime=0;

    private float dx;
    private boolean isOverPlay=false;

    private int naturalDrawNum=100;
    private float scales=1;
    private float Vdy,Tdy;
    private float screenWidth=0;
    private float availableScreenWidth=0;
    private float screenHeight=0;
    private float touchx=0;
    PointF touchDownPoint;
    PointF touchDownPoint2=null;
    private boolean enable_touch=false;
    private float acvailableScreenHeight=0;
    private int  currentStartPositon=0;
    SurfaceHolder mholder=null;

    float tempHigh;
    float tempLow;
    float voltHigh;
    float voltLow;

    Workbook drawBook=null;
    private int sheetNum=0;
    private int[] colums=null;
    private int[] rows=null;
    SurfaceUpdateThread mThread=null;

    Paint paintLine1;
    Paint paintLine2;
    Paint paintLine3;
    Paint paintText1;
    boolean isMultiPoint=false;


    PointF lastPoint=null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int touchId=event.getActionMasked();
        Log.v("dataTag","touched");
        switch (touchId){
            case MotionEvent.ACTION_DOWN:
                isMultiPoint=false;
                if(isOverPlay&&currentStartPositon>=0){
                    touchDownPoint.set(event.getX(),event.getY());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.v("dataTag","touchid:"+event.getActionIndex());
                touchDownPoint2.set(event.getX(1),event.getY(1));
                isMultiPoint=true;
                break;
            case MotionEvent.ACTION_MOVE:
                int touchPointId=event.getActionIndex();
                if(!isMultiPoint){
                    if(isOverPlay&&currentStartPositon>=0&&Math.abs(touchDownPoint.x-event.getX())>=dx){
                        touchx+=((event.getX()-touchDownPoint.x)/dx);
                        currentStartPositon+=((touchDownPoint.x-event.getX())/dx);
                        //currentTimes+=((touchDownPoint.x-event.getX())/dx/5);
                        if(currentStartPositon<0){
                            currentStartPositon=0;
                        }
                        touchDownPoint.set(event.getX(),event.getY());

                    }
                } else{
                    if(touchPointId==0){
                        scales*=(touchDownPoint.x-touchDownPoint2.x)/(event.getX(touchPointId)-touchDownPoint2.x);
                    }else if(touchPointId==1){
                        scales*=(touchDownPoint.x-touchDownPoint.x)/(event.getX(touchPointId)-touchDownPoint.x);
                    }
                    if(scales<0.1){
                        scales=0.1f;
                    }
                    if(scales>10){
                        scales=10;
                    }
                    Log.v("dataTag",touchPointId+"  "+touchDownPoint.x+"  "+event.getX(touchPointId)+"  "+touchDownPoint2.x+"????"+scales);
                    getDxDy();
                    if(touchPointId==0){
                        touchDownPoint.set(event.getX(touchPointId),event.getY(touchPointId));
                    }else if(touchPointId==1){
                        touchDownPoint2.set(event.getX(touchPointId),event.getY(touchPointId));
                    }

                }


                break;
            case MotionEvent.ACTION_POINTER_UP:

                break;
            case MotionEvent.ACTION_UP:
                isMultiPoint=false;
                if(isOverPlay&&currentStartPositon>=0){
                    touchx+=((event.getX()-touchDownPoint.x)/dx);
                    currentStartPositon+=((touchDownPoint.x-event.getX())/dx);
                    //currentTimes+=((touchDownPoint.x-event.getX())/dx/5);
                    if(currentStartPositon<0){
                        currentStartPositon=0;
                    }
                }
                break;
            default:break;
        }
        if(currentStartPositon<0){
            currentStartPositon=0;
        }


        return true;
    }

    public HistoryDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchDownPoint=new PointF(0,0);
        touchDownPoint2=new PointF(0,0);
        lastPoint=new PointF(0,0);
        mholder=getHolder();
        mholder.addCallback(this);
        paintLine1=new Paint();
        paintLine1.setAntiAlias(true);
        paintLine1.setColor(getResources().getColor(R.color.red_color));
        paintLine1.setStrokeWidth(1);
        paintLine2=new Paint();
        paintLine2.setAntiAlias(true);
        paintLine2.setColor(getResources().getColor(R.color.blue_color));
        paintLine2.setStrokeWidth(1);
        paintLine3=new Paint();
        paintLine3.setAntiAlias(true);
        paintLine3.setColor(getResources().getColor(R.color.green));
        paintLine3.setStrokeWidth(1);
        paintText1=new Paint();
        paintText1.setAntiAlias(true);
        paintText1.setColor(getResources().getColor(R.color.text_black));
        paintText1.setTextSize(20);
        paintText1.setTextAlign(Paint.Align.LEFT);
        Vdy=0;
    }
    float dt=0;
public void getDxDy(){
    dx=screenWidth/(scales*naturalDrawNum);
    Tdy=screenHeight/(2*60);
    Vdy=screenHeight/(2*2);
    dt=div*scales;
}


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    boolean prepareFinished=false;
    boolean UITHreadHasStart=false;
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Rect mRect=mholder.getSurfaceFrame();
        screenWidth=mRect.width();
        screenHeight= mRect.height();
        getDxDy();
        //???????????
        this.setOnTouchListener(this);
        mThread=new SurfaceUpdateThread(this,getHolder());
        if(prepareFinished){
            mThread.start();
            UITHreadHasStart=true;
        }else{
            UITHreadHasStart=false;
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mThread!=null){
            mThread.setStopFlag(true);
            mThread=null;
        }
        if(drawBook!=null){
            drawBook.close();
        }

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Sheet msheet=null;
        Cell mcell1=null;
        Cell mcell2=null;
        float dotMaxNum=naturalDrawNum*scales;
        int currentPosetionStart=currentStartPositon;
        if(drawBook!=null){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawColor(Color.WHITE);
            //canvas.drawColor(getResources().getColor(R.color.white_line), PorterDuff.Mode.CLEAR);
            canvas.drawLine(0,screenHeight/2,screenWidth,screenHeight/2,paintLine1);
            canvas.drawLine(0,screenHeight-1,screenWidth,screenHeight-1,paintLine1);
            if(!isOverPlay){
                currentTimes=(int)((System.currentTimeMillis()-startTime)/300);
            }
            if(currentTimes>=allRow-1){
                isOverPlay=true;
            }
            if(!isOverPlay&&currentTimes-currentPosetionStart>4*(dotMaxNum)/5){
                currentPosetionStart+=(currentTimes-currentPosetionStart-4*(dotMaxNum)/5);
            }
            canvas.drawLine(0,screenHeight/2-Vdy*(voltHigh-2f),screenWidth,screenHeight/2-Vdy*(voltHigh-2f),paintLine1);
            canvas.drawLine(0,screenHeight/2-Vdy*(voltLow-2f),screenWidth,screenHeight/2-Vdy*(voltLow-2f),paintLine1);
            canvas.drawLine(0,screenHeight-Tdy*(tempHigh+10),screenWidth,screenHeight-Tdy*(tempHigh+10),paintLine1);
            canvas.drawLine(0,screenHeight-Tdy*(tempLow+10),screenWidth,screenHeight-Tdy*(tempLow+10),paintLine1);
            for(int i=0;i<dotMaxNum;i++){
                if(i%((int)dotMaxNum/10)==0){
                    canvas.drawLine(i*dx,screenHeight,i*dx,screenHeight-20,paintLine1);
                    canvas.drawLine(i * dx, screenHeight / 2, i * dx, screenHeight / 2 - 20, paintLine1);
                    canvas.drawText(String.format("%.1f",((currentPosetionStart+i)*div))+"s",i*dx,screenHeight/2+20,paintText1);
                }else {
                    canvas.drawLine(i * dx, screenHeight / 2, i * dx, screenHeight / 2 - 10, paintLine1);
                    canvas.drawLine(i*dx,screenHeight,i*dx,screenHeight-10,paintLine1);
                }

                canvas.drawLine(i*dx,screenHeight,i*dx,screenHeight-20,paintLine1);
            }
            for(int i=0;i<60;i++){
                if(i%10==0){
                    canvas.drawLine(0,screenHeight-Tdy*i,30,screenHeight-Tdy*i,paintLine1);
                    canvas.drawText((i-10)+"??",0,screenHeight-Tdy*i,paintText1);
                }else {
                    canvas.drawLine(0, screenHeight - Tdy * i, 10, screenHeight - Tdy * i, paintLine1);
                }
            }
            for(int i=0;i<20;i++){
                if(i%5==0){
                    canvas.drawLine(0,screenHeight/2-Vdy*i/10,30,screenHeight/2-Vdy*i/10,paintLine1);
                    canvas.drawText((float)(i+20)/10+"V",0,screenHeight/2-Vdy*i/10,paintText1);
                }else{
                    canvas.drawLine(0,screenHeight/2-Vdy*i/10,10,screenHeight/2-Vdy*i/10,paintLine1);
                }
            }
            for(int i=0;i<allColum;i++){
                if(isOverPlay){
                    int sum1=0;
                    if(currentPosetionStart+dotMaxNum>=allRow){
                        sum1=allRow-1-currentPosetionStart;
                    }else{
                        sum1=(int)dotMaxNum-1;
                    }
                    for(int j=0;j<sum1;j++){

                        canvas.drawLine(j*dx,linesV[i][currentPosetionStart+j],
                                j*dx+dx,linesV[i][currentPosetionStart+j+1],paintLine2);
                    }
                }else{
                    for(int j=0;j<currentTimes-currentPosetionStart;j++){

                        canvas.drawLine(j*dx,linesV[i][currentPosetionStart+j],
                                j*dx+dx,linesV[i][currentPosetionStart+j+1],paintLine2);


                    }
                }
            }
            for(int i=0;i<allColumT;i++){
                if(isOverPlay){
                    int sum1=0;
                    if(currentPosetionStart+dotMaxNum>=allRow){
                        sum1=allRow-1-currentPosetionStart;
                    }else{
                        sum1=(int)(dotMaxNum)-1;
                    }
                    for(int j=0;j<sum1;j++){


                        canvas.drawLine(j*dx,linesT[i][currentPosetionStart+j],
                                j*dx+dx,linesT[i][currentPosetionStart+j+1],paintLine3);
                    }
                }else{
                    for(int j=0;j<currentTimes-currentPosetionStart;j++){

                        canvas.drawLine(j*dx,linesT[i][currentPosetionStart+j],
                                j*dx+dx,linesT[i][currentPosetionStart+j+1],paintLine3);


                    }
                }
            }

        }else{
            Log.v("dataTag", "drawbook is null");
        }

    }

    float[][] linesV=null;
    float[][] linesT=null;
    int allColum=0;
    int allRow=0;
    int allColumT=0;
    float div=0;

    public boolean init(String fileName,Context context){
        prepareFinished=false;
        ProgressDialog mpd=ProgressDialog.show(context, "历史数据", "正在读取数据", true, false);
        PrepareDataTask mtask=new PrepareDataTask(mpd);
        String[] param=new String[]{
            fileName
        };
        mtask.execute(param);
        mpd.setProgress(5);
        return true;
    }
   class PrepareDataTask extends AsyncTask<String,Integer,Boolean>{
       ProgressDialog pd;
       PrepareDataTask(ProgressDialog pd){
            this.pd=pd;
       }

       @Override
       protected Boolean doInBackground(String... params) {
           currentTimes=0;
           touchx=0;
           isOverPlay=false;
           startTime=System.currentTimeMillis();
           scales=1;
           currentStartPositon=0;
           publishProgress(0);

           try{
               drawBook= Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory() +
                       PreferencesString.DATASAVINGSTRING + params[0]));
               publishProgress(1);

           }catch (IOException e){
               e.printStackTrace();
               drawBook=null;
               return false;
           }catch (BiffException e){
               e.printStackTrace();
               drawBook=null;
               return false;
           }

           sheetNum=drawBook.getNumberOfSheets();

           for(int i=0;i<sheetNum;i++){
               if(i==0){
                   publishProgress(2);
                   allColum=drawBook.getSheet(i).getColumns()-1;
               }else{
                   publishProgress(3);
                    allColumT+=(drawBook.getSheet(i).getColumns()-1);

               }
           }
           publishProgress(4);
           allRow=drawBook.getSheet(0).getRows();
            for(int i=1;i<sheetNum;i++){
                if(allRow>drawBook.getSheet(i).getRows()){
                    allRow=drawBook.getSheet(i).getRows();
                }
            }
           allRow-=1;
           publishProgress(5);
           div=Float.parseFloat((drawBook.getSheet(0).getCell(0,2).getContents()));
           dt=div;
           linesV=new float[allColum][allRow];
           linesT=new float[allColumT][allRow];
           int line1Num=0;
           int line2Num=0;
           while(Vdy==0){
               try{
                   Thread.sleep(5);
               }catch (InterruptedException e){
                   e.printStackTrace();
               }

           }
           for(int i=0;i<sheetNum;i++){
               if(i==0){
                   publishProgress(6);
                   Sheet sheet=drawBook.getSheet(i);
                   for(int k=1;k<sheet.getColumns();k++){
                       publishProgress(25+40*k/allColum);
                       for(int j=0;j<allRow;j++){
                           if(sheet.getCell(k,1).getContents().equals("")){
                               linesV[line1Num][j]=0f;
                               break;
                           }else{
                               Cell cell=sheet.getCell(k,j+1);
                               int temp=0;
                               while(cell.getContents().equals("")){
                                   cell=sheet.getCell(k,j+1-(++temp));
                               }
                               linesV[line1Num][j]=screenHeight/2-Vdy*(Float.parseFloat(cell.getContents())-2f);
                           }

                       }
                       line1Num++;
                   }
               }else{
                   publishProgress(7);
                   Sheet sheet=drawBook.getSheet(i);
                   publishProgress(67+31*i/sheetNum);
                   for(int k=1;k<sheet.getColumns();k++){
                       for(int j=0;j<allRow;j++){
                           if(sheet.getCell(k,1).getContents().equals("")){
                               linesT[line2Num][j]=0f;
                               break;
                           }else{
                               Cell cell=sheet.getCell(k,j+1);
                               int temp=0;
                               while(cell.getContents().equals("")){
                                   cell=sheet.getCell(k,j+1-(++temp));
                               }
                               linesT[line2Num][j]=screenHeight-Tdy*(Float.parseFloat(cell.getContents())+10f);
                           }

                       }
                       line2Num++;
                   }
               }

           }
           publishProgress(8);
           return true;
       }

       @Override
       protected void onPostExecute(Boolean aBoolean) {
           super.onPostExecute(aBoolean);
           pd.dismiss();
           if(aBoolean){
               if(mThread!=null){
                   mThread=new SurfaceUpdateThread(HistoryDrawView.this,getHolder());
                   mThread.start();
                   UITHreadHasStart=true;
               }else{
                   if(!UITHreadHasStart){
                       mThread.start();
                   }
               }


           }
       }

       @Override
       protected void onProgressUpdate(Integer... values) {
           super.onProgressUpdate(values);
           switch (values[0]){
               case 0:
                   pd.setProgress(8);
                   pd.setMessage("正在打开excel表格");
                   break;
               case 1:
                   pd.setProgress(20);
                   pd.setMessage("正在读取单体数据");
                   break;
               case 2:
                   pd.setProgress(21);
                   pd.setMessage("正在读取单体数据");
                   break;
               case 3:
                   pd.setProgress(22);
                   pd.setMessage("正在读取单体数据");
                   break;
               case 4:
                   pd.setProgress(23);
                   pd.setMessage("正在读取单体数据");
                   break;
               case 5:
                   pd.setProgress(24);
                   pd.setMessage("正在读取单体数据");
                   break;
               case 6:
                   pd.setProgress(25);
                   pd.setMessage("正在读取单体数据电压");
                   break;
               case 7:
                    pd.setProgress(67);
                   pd.setMessage("正在读取单体数据温度");
                   break;
               case 8:
                   pd.setProgress(100);
                   pd.setMessage("已完成");
                   isOverPlay=true;
               default:pd.setProgress(values[0]);break;
           }
       }
   }

    /**
     * ?????????????
     * @param Vhigh
     * @param Vlow
     * @param Thigh
     * @param Tlow
     */
    public void setLimits(float Vhigh,float Vlow,float Thigh,float Tlow){
        this.voltHigh=Vhigh;
        this.voltLow=Vlow;
        this.tempHigh=Thigh;
        this.tempLow=Tlow;
    }
}
