package com.upsoft.blectrl.service.conn;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.upsoft.blectrl.service.BLEControlService;

/**
 * Created by Administrator on 2016/2/15 0015.
 */
public class BLEServiceConnection implements ServiceConnection {

    private final String TAG = "BLEServiceConnection";
    private BLEControlService mService = null;
    private Context mContext;

    public BLEServiceConnection(Context context){
        this.mContext = context;
    }

    //绑定服务时
    public void onServiceConnected(ComponentName className, IBinder rawBinder) {

        mService = ((BLEControlService.LocalBinder) rawBinder).getService();

        Log.d(TAG, "onServiceConnected mService= " + mService);
        if (!mService.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
        }
        Toast.makeText(mContext, "connect BLE success", Toast.LENGTH_SHORT).show();
    }
    //断开服务时
    public void onServiceDisconnected(ComponentName classname) {
        ////     mService.disconnect(mDevice);
        mService = null;

        Toast.makeText(mContext, "binding service failed", Toast.LENGTH_SHORT).show();
    }

    public BLEControlService getService(){

        if(mService == null){
            Toast.makeText(mContext, "try to binding later...", Toast.LENGTH_SHORT).show();
        }

        return mService;
    }
}
