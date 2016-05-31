package com.psf.sapt.wifiService;

/**
 * Created by psf on 2015/9/10.
 */

/**
 *
 */
public interface OnDataUpdateListener {
    /**
     * 用于监听can数据接收
     * @param data 接收到的数据
     */
    void OnDataUpdate(byte[] data);

    /**
     * 用于监听can端口扫描情况
     */
    void OnScanCompleted();

    void OnSendResult(int result);
}
