package com.upsoft.blectrl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.upsoft.blectrl.service.BLEControlService;

/**
 * 蓝牙数据变化接收者
 *
 * Created by vonchenchen on 2016/3/3 0003.
 */
public class BLEStatusChangeReceiver extends BroadcastReceiver{

    private OnReceiveDataListener mOnReceiveDataListener = null;
    private OnBLEStatusChangeListener mOnBLEStatusChangeListener = null;

    private BLEControlService mService;

    public void updateOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener){
        this.mOnReceiveDataListener = onReceiveDataListener;
    }

    public void setOnBLEStatusChangeListener(OnBLEStatusChangeListener onBLEStatusChangeListener){
        this.mOnBLEStatusChangeListener = onBLEStatusChangeListener;
    }

    public void setBLEService(BLEControlService service){
        this.mService = service;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //when communicate
        if (action.equals(BLEControlService.ACTION_GATT_CONNECTED)) {
            if(mOnBLEStatusChangeListener != null){
                mOnBLEStatusChangeListener.onConnected();
            }
        }

        //Service Disconnected
        if (action.equals(BLEControlService.ACTION_GATT_DISCONNECTED)) {
            mService.close();
            if(mOnBLEStatusChangeListener != null){
                mOnBLEStatusChangeListener.onDisConnected();
            }
        }

        //发现蓝牙GATT服务
        if (action.equals(BLEControlService.ACTION_GATT_SERVICES_DISCOVERED)) {
            //使能发送
            mService.enableTXNotification();
            if(mOnBLEStatusChangeListener != null){
                mOnBLEStatusChangeListener.onGattServiceDiscovered();
            }
        }

        //有可用数据
        if (action.equals(BLEControlService.ACTION_DATA_AVAILABLE)) {

            if(intent.getStringExtra(BLEControlService.UUID_DATA) != null){
                if(mOnReceiveDataListener != null){
                    final byte[] value = intent.getByteArrayExtra(BLEControlService.RECEIVE_DATA);
                    final String uuid = intent.getStringExtra(BLEControlService.UUID_DATA);
                    if(mOnReceiveDataListener != null){
                        mOnReceiveDataListener.getRecivedData(uuid, value);
                    }

                    if(mOnBLEStatusChangeListener != null) {
                        if(BLEControlService.TYPE_READ.equals(intent.getStringExtra(BLEControlService.ACTION_TYPE))) {
                            mOnBLEStatusChangeListener.onDataChange(uuid, value, BLEControlService.TYPE_READ);
                        }else if(BLEControlService.TYPE_WRITE.equals(intent.getStringExtra(BLEControlService.ACTION_TYPE))){
                            mOnBLEStatusChangeListener.onDataChange(uuid, value, BLEControlService.TYPE_WRITE);
                        }else if(BLEControlService.TYPE_DESCRIPTOR_READ.equals(intent.getStringExtra(BLEControlService.ACTION_TYPE))){
                            mOnBLEStatusChangeListener.onDataChange(uuid, value, BLEControlService.TYPE_DESCRIPTOR_READ);
                        }else if(BLEControlService.TYPE_DESCRIPTOR_WRITE.equals(intent.getStringExtra(BLEControlService.ACTION_TYPE))){
                            mOnBLEStatusChangeListener.onDataChange(uuid, value, BLEControlService.TYPE_DESCRIPTOR_WRITE);
                        }
                    }
                }
            }else if(BLEControlService.TYPE_RSSI_READ.equals(intent.getStringExtra(BLEControlService.ACTION_TYPE))){
                mOnBLEStatusChangeListener.onRssiRead(intent.getIntExtra(BLEControlService.RECEIVE_DATA, 0), BLEControlService.TYPE_RSSI_READ);
            }
        }

        if (action.equals(BLEControlService.DEVICE_DOES_NOT_SUPPORT_UART)){
            //mService.disconnect();
        }
    }

    /**
     * 监听连接状态的变化
     */
    public interface OnBLEStatusChangeListener{
        void onConnected();
        void onDisConnected();
        void onGattServiceDiscovered();
        void onDataChange(String uuid, byte[] value, String type);
        void onRssiRead(int rssi, String type);
    }

    /**
     * 监听数据变化
     */
    public interface OnReceiveDataListener{
        void getRecivedData(String uuid, byte[] value);
    }
}
