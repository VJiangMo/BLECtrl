package com.upsoft.blectrl.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.upsoft.blectrl.activity.MainActivity;
import com.upsoft.blectrl.service.BluetoothService;

import static com.upsoft.blectrl.service.Msg.DEVICE_NAME;
import static com.upsoft.blectrl.service.Msg.MESSAGE_DEVICE_NAME;
import static com.upsoft.blectrl.service.Msg.MESSAGE_READ;
import static com.upsoft.blectrl.service.Msg.MESSAGE_STATE_CHANGE;
import static com.upsoft.blectrl.service.Msg.MESSAGE_TOAST;
import static com.upsoft.blectrl.service.Msg.MESSAGE_WRITE;
import static com.upsoft.blectrl.service.Msg.TOAST;

/**
 * Created by yangzhou on 2017/4/16.
 */

public class BleUtil {
    private String mTag=this.getClass().toString();
    private Context mContext;
    private BluetoothService mBluetoothService;
    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName;
    private String devicename,deviceprotocol,tmpmsg;
    private boolean initialized=false;

    public BleUtil(Context ctx){
        mContext=ctx;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, "蓝牙不可用", Toast.LENGTH_LONG).show();
        }

        if(mBluetoothAdapter.isEnabled()){
            if (mBluetoothService == null){
                mBluetoothService = new BluetoothService(mContext, mHandler);
            }
        }
        if (mBluetoothService != null) {
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                mBluetoothService.start();
            }
        }
        connectDevice();
    }

    private void connectDevice(){
        String address=((MainActivity)mContext).getBlueToothAddress();
        if(address.equals("")||null==address){
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        initialized=false;
        mBluetoothService.connect(device);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(mTag, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED: {
                            sendMsg("ATZ");
                            break;
                        }
                        case BluetoothService.STATE_CONNECTING:{
                            break;
                        }
                        case BluetoothService.STATE_LISTEN:{
                            break;
                        }
                        case BluetoothService.STATE_NONE:{
                            break;
                        }
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    compileMessage( msg.obj.toString());
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(mContext, "已连接到 "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(mContext, msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void compileMessage(String msg) {

    }

    private synchronized void sendMsg(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            message=message+"\r";
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
        }
    }

}
