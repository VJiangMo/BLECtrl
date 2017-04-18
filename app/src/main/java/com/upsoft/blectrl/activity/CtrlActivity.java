package com.upsoft.blectrl.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.upsoft.blectrl.R;
import com.upsoft.blectrl.receiver.BLEStatusChangeReceiver;
import com.upsoft.blectrl.service.BLEControlService;
import java.util.List;
import java.util.UUID;

/**
 * Created by BASTA on 2017/4/17.
 */

public class CtrlActivity extends Activity {
    private String mTag=this.getClass().toString();
    private Context mContext;
    private ViewWidgetGroup mViewWidgetGroup=new ViewWidgetGroup();
    private BleCtrlClickListener mBleCtrlClickListener;
    private boolean mIsScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private BLEControlService mService;
    private BluetoothAdapter.LeScanCallback mBLEScanCallback;
    private BLEStatusChangeReceiver mBLEStatusChangeReceiver;
    private final int CONNECT_STATUS_CONNECTED = 1;
    private final int CONNECT_STATUS_DISCONNECTED = 2;
    private int mConnectStatus = CONNECT_STATUS_DISCONNECTED;
    private boolean mIsConnected=false;
    private String mDeviceAddress="";
    private ConnectThread mConnectThread=new ConnectThread();
    private GetServiceThread mGetServiceThread=new GetServiceThread();
    private BluetoothGattCharacteristic mBCS=null;
    private boolean mIsLedOn=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(mContext=this);
        View layout = inflater.inflate(R.layout.activity_ctrl, null);
        setContentView(layout);

        init(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBLEStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(mTag, ignore.toString());
        }
        unbindService(mServiceConnection);
        if(mService != null) {
            mService.stopSelf();
            mService = null;
        }
        disconnectBLEDevice();
    }

    private void init(View layout){
        mBleCtrlClickListener =new BleCtrlClickListener();
        mViewWidgetGroup.mReConnectBtn=(Button)layout.findViewById(R.id.reconnect_btn);
        mViewWidgetGroup.mReConnectBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mLedOnBtn=(Button)layout.findViewById(R.id.led_on_btn);
        mViewWidgetGroup.mLedOnBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mLedOffBtn=(Button)layout.findViewById(R.id.led_off_btn);
        mViewWidgetGroup.mLedOffBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mLedSwitchBtn=(Button)layout.findViewById(R.id.led_switch_btn);
        mViewWidgetGroup.mLedSwitchBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mConnectStateTV=(TextView)layout.findViewById(R.id.connect_state);

        mIsScanning=false;

        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, 1);

        //BLE
        mBLEScanCallback = getBLEScanCallback();
        checkBLEDevice();
        mBLEStatusChangeReceiver = new BLEStatusChangeReceiver();
        initBLEControlService();

        scanOtherBLEDevice(true);
    }

    private class ViewWidgetGroup {
        public Button mLedOnBtn;
        public Button mLedOffBtn;
        public Button mLedSwitchBtn;
        public Button mReConnectBtn;
        public TextView mConnectStateTV;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        //绑定服务时
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((BLEControlService.LocalBinder) rawBinder).getService();
            mBLEStatusChangeReceiver.setBLEService(mService);
            mConnectStatus = CONNECT_STATUS_CONNECTED;
            if (!mService.initialize()) {
                finish();
            }
            Toast.makeText(mContext,getStringById(R.string.pairing_success_str), Toast.LENGTH_SHORT).show();
        }
        //断开服务时
        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
            mConnectStatus = CONNECT_STATUS_DISCONNECTED;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEControlService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEControlService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEControlService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEControlService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLEControlService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private class GetServiceThread extends Thread{
        public boolean mIsRunFlag=false;
        @Override
        public void run() {
            super.run();
            while (mIsRunFlag){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(mService!=null){
                    List<BluetoothGattService> dataList= mService.getBLEServices();
                    Log.d(mTag,"dataList size:"+dataList.size());
                    if(dataList.size()>=3){
                        mIsRunFlag=false;
                        for(BluetoothGattService bluetoothGattService:dataList){
                            if(bluetoothGattService.getUuid().equals(UUID.fromString("00001523-1212-efde-1523-785feabcd123"))){
                                mBCS=bluetoothGattService.getCharacteristic(UUID.fromString("00001525-1212-efde-1523-785feabcd123"));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void getDeviceServiceList(){
        if(!mGetServiceThread.mIsRunFlag){
            mGetServiceThread.mIsRunFlag=true;
            mGetServiceThread.start();
        }
    }

    private void initBLEControlService() {
        //create BLEControService
        Intent bindIntent = new Intent(this, BLEControlService.class);
        //binding BLEControService callback
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //register listener that listen BLE status change callback
        LocalBroadcastManager.getInstance(this).registerReceiver(mBLEStatusChangeReceiver, makeGattUpdateIntentFilter());

        mBLEStatusChangeReceiver.setOnBLEStatusChangeListener(new BLEStatusChangeReceiver.OnBLEStatusChangeListener() {
            @Override
            public void onConnected() {
                mIsConnected=true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewWidgetGroup.mConnectStateTV.setTextColor(mContext.getResources().getColor(R.color.green));
                        mViewWidgetGroup.mConnectStateTV.setText(getStringById(R.string.connect_state_connected));
                        getDeviceServiceList();
                    }
                });
            }

            @Override
            public void onDisConnected() {
                mIsConnected=false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewWidgetGroup.mConnectStateTV.setTextColor(mContext.getResources().getColor(R.color.red));
                        mViewWidgetGroup.mConnectStateTV.setText(getStringById(R.string.connect_state_unconnected));
                    }
                });
            }

            @Override
            public void onGattServiceDiscovered() {

            }

            @Override
            public void onDataChange(String uuid, byte[] value, String type) {

            }

            @Override
            public void onRssiRead(int rssi, String type) {

            }
        });
    }

    /**
     * check this device and judge if support BLE
     */
    private void checkBLEDevice(){
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, " ble not supported ", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, " ble not supported ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void scanOtherBLEDevice(boolean enable){
        if(enable){
            startScan();
        }else{
           stopScan();
        }
    }

    private void startScan(){
        if(!mIsScanning){
            //start scan
            mIsScanning = true;
            mBluetoothAdapter.startLeScan(mBLEScanCallback);
        }
    }

    private void stopScan(){
        if(mIsScanning){
            //stop scan
            mIsScanning = false;
            mBluetoothAdapter.stopLeScan(mBLEScanCallback);
        }
    }

    private void connectBLEDevice(){
        if(mService!=null&&!mDeviceAddress.equals("")){
            mService.connect(mDeviceAddress);
        }
    }

    private void disconnectBLEDevice(){
        if(mService!=null){
            mService.disconnect();
        }
    }

    private class ConnectThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (!mIsConnected){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectBLEDevice();
            }
        }
    }
    private void searchSuccessHdl(){
        if(!mIsConnected&&mConnectThread!=null){
            mConnectThread.start();
        }
    }

    private BluetoothAdapter.LeScanCallback getBLEScanCallback(){
        return new BluetoothAdapter.LeScanCallback(){
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                mDeviceAddress=device.getAddress();
                if(mDeviceAddress.equals(getStringById(R.string.bluetooth_address_str))){
                    stopScan();//搜索到设备后，立刻停止搜索
                    searchSuccessHdl();
                }
            }
        };
    }

    private String getStringById(int resId){
       return mContext.getResources().getString(resId);
    }

    private class BleCtrlClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int vId=v.getId();
            switch (vId){
                case R.id.reconnect_btn:{
                    startScan();
                    break;
                }
                case R.id.led_on_btn:{
                    if(mBCS!=null){
                        String cmd=new String(getStringById(R.string.open_close_cmd_str));
                        mService.writeCharacteristic(mBCS,cmd.getBytes());
                        mIsLedOn=true;
                    }else {
                        Toast.makeText(mContext,getStringById(R.string.open_fail_str),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.led_off_btn:{
                    if(mBCS!=null){
                        String cmd=new String(getStringById(R.string.open_close_cmd_str));
                        mService.writeCharacteristic(mBCS,cmd.getBytes());
                        mIsLedOn=false;
                    }else {
                        Toast.makeText(mContext,getStringById(R.string.close_fail_str),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.led_switch_btn:{
                    if(mBCS!=null){
                        String cmd=new String(getStringById(R.string.switch_cmd_str));
                        mService.writeCharacteristic(mBCS,cmd.getBytes());
                    }else {
                        Toast.makeText(mContext,getStringById(R.string.switch_fail_str),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }

}
