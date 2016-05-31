package com.psf.sapt.wifiService;

/**
 * Created by psf on 2015/8/22.
 *对can通信协议进行数据处理
 */


/**
 * 对从can获取的帧数据进行处理，可以获得帧ID，整型数据，字符串型数据 每次拦截N帧
 */
public class DataProgress {


    static {
        System.loadLibrary("dataDecoder");
    }
    /**
     *返回数据帧的原始id 如0x100 0x102等
     * @return 当前所有帧的id
     */
    public native int[] getId();
    /**
     * 返回数据帧所属模块的（标定）id 如1,2,3,4...
     * @return 模块id
     */
    public native int[] getPureIDs();



    /**
     * 得到单帧数据
     * @param i 帧序
     * @return  单帧数据
     */
    public native byte[] getFrame(int i);
    //
    /**
     * 动态设置要处理的数据帧，设置后会有一个锁，在当前数据处理完之前，当前对象不接受其它数据帧，
     * 所以在数据处理结束后，需要手动解锁，调用 {@link #setAllowSetNewData()}};
     * @param frames 要处理的数据帧
     */
    public native void setFrames(byte[] frames);

    /**
     * 解锁，允许接收其它数据帧
     */
    public native void setAllowSetNewData();

    /**
     * 根据协议获取数据帧的ID
     * @param frame 获取单帧ID ，如0x104等
     * @return 帧ID
     */
    public native int getID(byte[] frame);


    /**
     *获取当前数据温度路数
     * @param moduleId 要判断的模块
     * @return 返回当前数据包含的温度路数，如果不是LCU数据或者不是当前模块,数据，则返回-1，是的话返回路数
     */
    public native int getLcuTempNum(int moduleId);
    /**
     * 对接收到的帧数据进行解包，提取数据部分
     * @param Frame 收到的can数据帧
     * @return  can数据帧里面的8个字节的数据
     */

    public native byte[] getFrameData(byte[] Frame);


    /**
     *将数据转换成浮点数组形式
     * @param numOfFrame 第i帧
     * @return  转换后的结果
     */
    public native float[] getFrameFloatData(int numOfFrame);

}
