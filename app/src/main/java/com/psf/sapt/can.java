package com.psf.sapt;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 *
 * Created by psf on 2015/8/27.
 */
public class can {
    /**
     * Can透传设备与pad之间Socket通讯的通讯端口
     */
    private int port;
    /**
     * 用于统计当前Socket通讯端口1建立的连接数量
     */
    public static int port1Num=0;
    /**
     * 用于统计当前Socket通讯端口2建立的连接数量
     */
    public static int port2Num=0;

    private Socket sconn;
    private OutputStream outputStream;
    private InputStream inputStream;
    /**
     * socket 连接是否已经建立
     */
    public boolean isConn=false;

    /**
     * 初始化Can通讯端口
     * @param port Socket 端口
     */
    public can(int port){
        this.port=port;
        try{
            sconn=new Socket();
            sconn.connect(new InetSocketAddress("192.168.0.178",port),1000);
            sconn.setSoTimeout(1000);
            outputStream=sconn.getOutputStream();
            inputStream=sconn.getInputStream();
            isConn=true;
            if(port==4002){
                port2Num++;
            }else{
                port1Num++;
            }

        }catch (UnknownHostException e){
            e.printStackTrace();
            Log.v("dataTag","UnknownHostException");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * 发送前对要发送的数据进行can数据帧协议包装
     * @param length 要发送的数据（指令）的字节数（0-8）
     * @param data 要发送的数据（8个字节）数据不足8个字节后面补零
     * @return  经过can协议包装后的数据帧
     */
    public byte[] getSendFrame(int id,int length,byte[] data){
        byte[] send=new byte[20];
        send[0]=(byte)0xfe;
        send[1]=(byte)0xfd;
        send[2]=(byte)0x00;
        send[3]=(byte)(length&0x0f);
        send[4]=(byte)0x00;
        send[5]=(byte)0x00;
        if(id>=0x100){
            send[6]=(byte)((id>>8)&0xff);
        }else{
            send[6]=(byte)0x00;
        }

        send[7]=(byte)(id&0x00ff);
        for(int i=0;i<11;i++){
            if(i<length){
                send[8+i]=data[i];
            }else{
                send[8+i]=(byte)0x00;
            }
        }
        send[19]=send[0];
        for(int i=1;i<19;i++){
            send[19]^=send[i];
        }
        return send;
    }


    /**
     * 发送数据到can端口
     * @param id 请求帧的id
     * @param lenght 发送数据的字节数
     * @param send 要发送的数据必须为8个字节，不足补零
     * @return 是否发送成功
     */
    public boolean write(int id,int lenght,byte[] send){
        if(isConn){
            try {
                outputStream.write(getSendFrame(id,lenght, send));
            }catch (IOException e){
                isConn=false;
                try{
                    outputStream.close();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }

                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * 读取can的数据
     * @param data 读取的数据存放的位置 长度默认为data的字节数-20个字节/帧
     */
    public boolean read(byte[] data){
        if (isConn){
            try{
                //data=new byte[inputStream.available()];
                inputStream.read(data);
            }catch (SocketTimeoutException e){
                return false;
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }

        }
        return true;

    }

    /**
     * 获取当前输入流中未读取的字节数
     * @return 未读取的字节数
     */
    public int getAviable(){
        int aviable=0;
        try {
            aviable=inputStream.available();
        }catch (IOException e){
            e.printStackTrace();
        }
        return aviable;
    }
    /**
     * 关闭从can总线连接
     */
    public void close(){
        if(outputStream!=null){
            try {
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(inputStream!=null){
            try{
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(sconn!=null){
            try{
                sconn.close();
                if(port==4002){
                    port2Num--;
                }else{
                    port1Num--;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
