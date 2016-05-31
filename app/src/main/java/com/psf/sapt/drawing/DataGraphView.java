package com.psf.sapt.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.psf.sapt.R;


/**
 * I'm use this to draw something
 * Created by psf on 2015/9/15.
 */
public class DataGraphView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceUpdateThread mThread;
    /**
     * 最大线程数
     */
    private final int maxModuleNum=40;
    private final int maxItem=24;
    private boolean[] modules=new boolean[maxModuleNum];
    private final int dotNum=100;
    private float[][] data=new float[maxModuleNum][maxItem];
    private int dataNum=0;
    private int moduleNum=0;
    private float dx;
    private float dy;
    public int maxVoltItem=0;
    public int minVoltItem=0;
    public int maxVoltModule=0;
    public int minVoltModule=0;
    public float maxVolt=0;
    public float minVolt=0;

    public boolean isHasProblem() {
        return hasProblem;
    }

    boolean hasProblem=false;
    //private float[] data=null;

    /**
     *
     * @param multiData
     * @param moduleId
     * @param perModuleDetail
     * @param isTempType
     * @param singleMode
     */
    public void setArguements(float[] multiData,int[] moduleId,int[] perModuleDetail,
                              boolean isTempType,boolean singleMode){
        this.isTempType=isTempType;
        this.singleMode=singleMode;
        this.modulesId=moduleId;
        //moduleNum=modulesId.length;
        this.perModuleDetail=perModuleDetail;

        if(singleMode){
            this.multiData=multiData;
            dataNum=this.multiData.length;
            if(hasSurfaceViewCreated){
                setNewGraph();
            }
        }else{
            for(int i=0;i<maxModuleNum;i++){
                modules[i]=false;
                for(int j=0;j<maxItem;j++){
                    data[i][j]=9999f;
                }
            }
            for(int i=0;i<moduleId.length;i++){
                Log.v("dataTag","选中的模块是："+moduleId[i]);
                this.modules[moduleId[i]-1]=true;
                //for(int j=0;j<perModuleDetail.length;j++){
                //    data[moduleId[i]][j]=multiData[j];
                //}
            }
            if(hasSurfaceViewCreated){
                setNewGraph();
            }
        }

    }

    boolean refresh=false;
    /**
     * 单个模块绘图时调用的更新 数据的方法
     * @param multiData
     */
    public void setMultiData(float[] multiData) {
        this.multiData=multiData;
        dataNum=this.multiData.length;
        refresh=true;

    }

    /**
     * 多个模块绘图时调用的 更新数据方法
     * @param data
     * @param moduleId
     */
    public void setData(float[] data,int moduleId){
        /*
        Log.v("dataTag","setData"+moduleId);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<data.length;i++){
            sb.append(data[i]);
            sb.append(",");
        }
        Log.v("dataTag",sb.toString());
        */
        System.arraycopy(data,0,(this.data[moduleId-1]),0,data.length);
        getMaxAndMin();
        refresh=true;
    }

    public void getMaxAndMin(){
        float MaxPerItem=0;
        float MinPerItem=6;
        for(int i=0;i<modulesId.length;i++){
            for(int j=0;j<maxItem;j++){
                if(data[modulesId[i]-1][j]!=9999f){
                    if(data[modulesId[i]-1][j]>MaxPerItem){
                        MaxPerItem=data[modulesId[i]-1][j];
                        maxVoltModule=modulesId[i];
                        maxVolt=data[modulesId[i]-1][j];
                        maxVoltItem=j;
                    }
                    if(data[modulesId[i]-1][j]<MinPerItem){
                        MinPerItem=data[modulesId[i]-1][j];
                        minVoltModule=modulesId[i];
                        minVolt=data[modulesId[i]-1][j];
                        minVoltItem=j;
                    }
                }
            }
        }
    }


    private float[] multiData=null;//绘图所需的数据


    private int[] modulesId=null;//需要绘制的模块

    private int[] perModuleDetail=null;//每个模块的单体数量


    public boolean isTempType() {
        return isTempType;
    }

    private boolean isTempType=false;   //是否是需要绘制温度信息




    //绘制坐标系的一些基本元素
    long time;
    Rect mRect;
    Paint mpaintText;
    Paint mpaintLine;
    Paint mpaintLine2;
    Paint mpaintLineStrong;
    Paint WhiteLine;
    Paint transpPaint;
    private boolean hasLimits=false;
    private float limitsHigh=0;
    private float limitLow=0;
    private float virualLimitHigh=0;
    private float virualLimitLow=0;

    private boolean hasSurfaceViewCreated=false;
    private Bitmap yzhou,yzhous;
    private Bitmap xzhou,xzhous;
    private Matrix matrix=null;
    final int yleftmargin=40;

    public PointF[] dataPoint=null;
    public PointF startPoint;
    public int currentTimes=0;
    private boolean singleMode=true;



    public DataGraphView(Context context) {
        super(context);
    }
    public void setNewGraph(){
        getDxDy();

        if(singleMode){
            dataPoint=new PointF[dataNum*dotNum];
            for(int i=0;i<dataNum*dotNum;i++){
                dataPoint[i]=new PointF();
            }
        }else{
            dataPoint=new PointF[maxModuleNum*maxItem*dotNum];
            for(int i=0;i<maxModuleNum*maxItem*dotNum;i++){
                dataPoint[i]=new PointF(9999f,9999f);
            }
        }
        startPoint = new PointF();
        if(isTempType){
            startPoint.set(yzhous.getWidth() / 2+yleftmargin, mRect.bottom);
        }else{
            startPoint.set(yzhous.getWidth() / 2+yleftmargin, mRect.bottom - xzhous.getHeight() / 2);
        }

        currentTimes=0;
    }

    /**
     * 设置数据范围，超出范围特殊绘制
     * @param high
     * @param low
     */
    public void setLimit(float high,float low){
        this.hasLimits=true;
        this.limitsHigh=high;
        this.limitLow=low;
    }

    public DataGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //使得sufaceView 在顶层，为使得其透明
        setZOrderOnTop(true);
        SurfaceHolder sh=getHolder();
        sh.addCallback(this);
        //设置surfaceView透明效果
        sh.setFormat(PixelFormat.TRANSPARENT);
        mThread=new SurfaceUpdateThread(this,sh);
        mRect=sh.getSurfaceFrame();
        hasSurfaceViewCreated=false;
        mpaintLine=new Paint();
        mpaintLine.setStrokeWidth(2);
        mpaintLine.setColor(getResources().getColor(R.color.red_color));
        mpaintLine.setAntiAlias(true);
        mpaintLine2=new Paint();
        mpaintLine2.setStrokeWidth(2);
        mpaintLine2.setColor(getResources().getColor(R.color.blue_color));
        mpaintLine2.setAntiAlias(true);
        mpaintLineStrong=new Paint();
        mpaintLineStrong.setStrokeWidth(1);
        mpaintLineStrong.setColor(getResources().getColor(R.color.red_color));
        mpaintLineStrong.setAntiAlias(true);
        WhiteLine=new Paint();
        WhiteLine.setStrokeWidth(2);
        WhiteLine.setColor(getResources().getColor(R.color.white_line));
        WhiteLine.setAntiAlias(true);
        WhiteLine.setTextSize(32);
        mpaintText=new Paint();
        mpaintText.setTextSize(32);
        mpaintText.setColor(getResources().getColor(R.color.blue_color));
        mpaintText.setAntiAlias(true);
        transpPaint=new Paint();
        transpPaint.setAlpha(0);
        BitmapFactory.Options mop1=new BitmapFactory.Options();
        mop1.inScaled=false;
        yzhou=BitmapFactory.decodeResource(getResources(), R.drawable.yzhou,mop1);
        xzhou=BitmapFactory.decodeResource(getResources(), R.drawable.xzhou,mop1);
        Log.v("dataTag","new one");

    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        hasProblem=false;
        //画坐标系
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(yzhous, yleftmargin, 0, mpaintLine);
        //canvas.drawLine(0, mRect.bottom - 20, mRect.right, mRect.bottom - 20, WhiteLine);
        //canvas.drawText("O",0,mRect.bottom-xzhous.getHeight()-40,mpaintText);
        int sumHeight=yzhous.getHeight()-xzhous.getHeight()/2;
        if(!isTempType){
            //绘制电压坐标
            virualLimitHigh=sumHeight * (1 - (limitsHigh-2) / 2);
            virualLimitLow=sumHeight*(1-(limitLow-2)/2);
            canvas.drawBitmap(xzhous, 0, mRect.bottom - xzhous.getHeight(), mpaintLine);
            //画警戒线
            canvas.drawLine(yzhous.getWidth() / 2 + yleftmargin, virualLimitHigh,
                    mRect.right, virualLimitHigh, mpaintLineStrong);
            canvas.drawLine(yzhous.getWidth()/2+yleftmargin,virualLimitLow,mRect.right,
                    virualLimitLow,mpaintLineStrong);
            for(int i=0;i<20;i++){
                if(i%5==0){
                    canvas.drawText(String.valueOf((float)i/10+2)+"V",0,sumHeight*(20-i)/20,mpaintText);
                    canvas.drawLine(yzhous.getWidth()/2+yleftmargin,sumHeight*(20-i)/20,
                            yzhous.getWidth()/2+40+yleftmargin,sumHeight*(20-i)/20,mpaintLineStrong);
                }else {
                    canvas.drawLine(yzhous.getWidth()/2+yleftmargin,sumHeight*(20-i)/20,
                            yzhous.getWidth()/2+20+yleftmargin,sumHeight*(20-i)/20,mpaintLine);
                }
            }
        }else{
            virualLimitHigh=mRect.bottom*(1-(limitsHigh+30)/100);
            virualLimitLow=mRect.bottom*(1-(limitLow+30)/100);
            //绘制温度坐标
            canvas.drawBitmap(xzhous, 0, mRect.bottom*7/10 - xzhous.getHeight()/2, mpaintLine);
            //画警戒线
            canvas.drawLine(yzhous.getWidth()/2+yleftmargin,virualLimitHigh,
                    mRect.right,virualLimitHigh,mpaintLineStrong);
            canvas.drawLine(yzhous.getWidth()/2+yleftmargin,virualLimitLow,
                    mRect.right,virualLimitLow,mpaintLineStrong);
            for(int i=10;i<91;i++){
                if(i%10==0){
                    canvas.drawText(String.valueOf(i-30)+"°C",0,yzhous.getHeight()*(100-i)/100,mpaintText);
                    canvas.drawLine(yzhous.getWidth()/2+yleftmargin,yzhous.getHeight()*(100-i)/100,
                            yzhous.getWidth()/2+40+yleftmargin,yzhous.getHeight()*(100-i)/100,mpaintLineStrong);
                }else {
                    canvas.drawLine(yzhous.getWidth()/2+yleftmargin,yzhous.getHeight()*(100-i)/100,
                            yzhous.getWidth()/2+20+yleftmargin,yzhous.getHeight()*(100-i)/100,mpaintLine);
                }
            }


        }

//单模块下的绘图
        if(singleMode){
            for(int i=0;i<dataNum;i++){
                dataPoint[i*dotNum+currentTimes].set(currentTimes*dx+startPoint.x,
                        startPoint.y-dy*(multiData[i]+(isTempType? 30:-2f)));
                for(int j=0;j<currentTimes;j++){
                    if(dataPoint[i*dotNum+j].y<virualLimitHigh||dataPoint[i*dotNum+j].y>virualLimitLow){
                        canvas.drawText(String.valueOf(modulesId[0]),dataPoint[i*dotNum+j].x,
                                dataPoint[i*dotNum+j].y-5,mpaintLine2);
                    }
                    if(j==0){
                        canvas.drawLine(dataPoint[i*dotNum+j].x,dataPoint[i*dotNum+j].y,
                                dataPoint[i*dotNum+j].x,dataPoint[i*dotNum+j].y,mpaintLine2);
                    }else{
                        canvas.drawLine(dataPoint[i*dotNum+j-1].x,dataPoint[i*dotNum+j-1].y,
                                dataPoint[i*dotNum+j].x,dataPoint[i*dotNum+j].y,mpaintLine2);
                    }
                }
            }
            if(refresh){
                currentTimes++;
            }
            refresh=false;
            if(currentTimes>99){
                currentTimes=0;
            }
        }else{
            //多模块下的绘图
           // Log.v("dataTag","multiLine");
            for(int i=0;i<maxModuleNum;i++){
                if(modules[i]){
                    //Log.v("dataTag","正在绘制"+i+"个模块");
                    for(int j=0;j<maxItem;j++){
                        if(data[i][j]!=9999f){
                           // Log.v("dataTag","正在绘制第"+j+"条线"+data[i][j]);
                            dataPoint[i*maxItem*dotNum+j*dotNum+currentTimes].set(currentTimes*dx+startPoint.x,
                                    startPoint.y-dy*(data[i][j]+(isTempType? 30f:-2f)));
                            //Log.v("dataTag","当前坐标("+dataPoint[i*maxItem*dotNum+j*dotNum+currentTimes].x+","
                            //    +dataPoint[i*maxItem*dotNum+j*dotNum+currentTimes].y+")");
                            for(int k=0;k<currentTimes;k++){
                                //判断是否超出限制
                                if(dataPoint[i*maxItem*dotNum+j*dotNum+k].y!=9999f){
                                    if(dataPoint[i*maxItem*dotNum+j*dotNum+k].y<virualLimitHigh||dataPoint[i*maxItem*dotNum+j*dotNum+k].y>virualLimitLow){
                                       // Log.v("dataTag","报警");
                                        hasProblem=true;
                                        canvas.drawText(String.valueOf(i+1),dataPoint[i*maxItem*dotNum+j*dotNum+k].x,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k].y,mpaintText);

                                    }
                                    if(k==0||dataPoint[i*maxItem*dotNum+j*dotNum+k-1].y==9999f){
                                        canvas.drawLine(dataPoint[i*maxItem*dotNum+j*dotNum+k].x,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k].y,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k].x,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k].y,mpaintLine2);
                                    }else{
                                        canvas.drawLine(dataPoint[i*maxItem*dotNum+j*dotNum+k-1].x,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k-1].y,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k].x,
                                                dataPoint[i*maxItem*dotNum+j*dotNum+k].y,mpaintLine2);
                                    }
                                }

                            }
                        }else{
                            dataPoint[i*maxItem*dotNum+j*dotNum+currentTimes].set(currentTimes*dx+startPoint.x,
                                    9999f);
                        }
                    }
                }
            }
            if(refresh){
                currentTimes++;
            }
            refresh=false;
            if(currentTimes>99){
                currentTimes=0;
            }

        }


    }
    public void getDxDy(){
        final float yVoltLength=2;//绘制电压的时候y轴的长度为6
        final float yTempLength=100;//绘制温度的时候y轴的长度为100
        dx=mRect.right/dotNum;
        if(isTempType){
            dy=yzhous.getHeight()/yTempLength;
        }else{
            dy=(yzhous.getHeight()-xzhous.getHeight()/2)/yVoltLength;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(matrix==null){
            Log.v("dataTag","surfaceCreated new x-y");
            //当view有了到达前台的时候进行一些预处理
            matrix = new Matrix();
            matrix.postScale((float) mRect.bottom / yzhou.getHeight(), (float) mRect.bottom / yzhou.getHeight());
            yzhous = Bitmap.createBitmap(yzhou, 0, 0, yzhou.getWidth(), yzhou.getHeight(), matrix, false);
            matrix=new Matrix();
            matrix.postScale((float) mRect.right / xzhou.getWidth(), (float) mRect.right / xzhou.getWidth());
            xzhous=Bitmap.createBitmap(xzhou, 0, 0, xzhou.getWidth(), xzhou.getHeight(), matrix, false);
        }
        setNewGraph();
        //重新开始新的绘制界面
        Log.v("dataTag",mRect.bottom+"  "+mRect.right);
        if(mThread!=null&&!mThread.hadStart()){
            mThread.start();
        }else{
            mThread=new SurfaceUpdateThread(this,getHolder());
            mThread.start();
        }
        hasSurfaceViewCreated=true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mThread!=null){
            mThread.setStopFlag(true);
        }
    }
}
