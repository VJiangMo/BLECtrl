package com.upsoft.blectrl.activity;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.upsoft.blectrl.DoubleClickExitHelper;
import com.upsoft.blectrl.R;
import com.upsoft.blectrl.receiver.BLEStatusChangeReceiver;
import com.upsoft.blectrl.service.BLEControlService;
import com.upsoft.blectrl.util.DataStore;

import junit.framework.TestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by BASTA on 2017/4/17.
 */

public class CtrlActivity extends Activity {
    private String mTag=this.getClass().toString();
    private Context mContext;
    private DoubleClickExitHelper mDoubleClickExit;
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
    private DataStore mDataStore;
    private List<TextView> mTVList=new ArrayList<TextView>();
    private final String[] mLedStateArr={"RedBright","RedDark","BlueOn","RedBlingFast","ResBlingSlow","DoubleBling"};

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return mDoubleClickExit.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(mContext=this);
        View layout = inflater.inflate(R.layout.activity_ctrl, null);
        setContentView(layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

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
        mDoubleClickExit=new DoubleClickExitHelper(this);
        mDataStore=new DataStore(mContext);
        mBleCtrlClickListener =new BleCtrlClickListener();
        mViewWidgetGroup.mReConnectBtn=(Button)layout.findViewById(R.id.reconnect_btn);
        mViewWidgetGroup.mReConnectBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mLedOnBtn=(Button)layout.findViewById(R.id.led_on_btn);
        mViewWidgetGroup.mLedOnBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mLedOnBtn.setEnabled(true);
        mViewWidgetGroup.mLedOffBtn=(Button)layout.findViewById(R.id.led_off_btn);
        mViewWidgetGroup.mLedOffBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mLedOffBtn.setEnabled(false);//首次不可点
        mViewWidgetGroup.mLedSwitchBtn=(Button)layout.findViewById(R.id.led_switch_btn);
        mViewWidgetGroup.mLedSwitchBtn.setOnClickListener(mBleCtrlClickListener);
        mViewWidgetGroup.mConnectStateTV=(TextView)layout.findViewById(R.id.connect_state);

        mViewWidgetGroup.mRedBright=(TextView)layout.findViewById(R.id.light_state_rb);
        mViewWidgetGroup.mRedBright.setOnClickListener(mBleCtrlClickListener);
        mTVList.add(mViewWidgetGroup.mRedBright);

        mViewWidgetGroup.mRedDark=(TextView)layout.findViewById(R.id.light_state_rd);
        mViewWidgetGroup.mRedDark.setOnClickListener(mBleCtrlClickListener);
        mTVList.add(mViewWidgetGroup.mRedDark);

        mViewWidgetGroup.mBlueOn=(TextView)layout.findViewById(R.id.light_state_bo);
        mViewWidgetGroup.mBlueOn.setOnClickListener(mBleCtrlClickListener);
        mTVList.add(mViewWidgetGroup.mBlueOn);

        mViewWidgetGroup.mRedBlingFast=(TextView)layout.findViewById(R.id.light_state_rbf);
        mViewWidgetGroup.mRedBlingFast.setOnClickListener(mBleCtrlClickListener);
        mTVList.add(mViewWidgetGroup.mRedBlingFast);

        mViewWidgetGroup.mResBlingSlow=(TextView)layout.findViewById(R.id.light_state_rbs);
        mViewWidgetGroup.mResBlingSlow.setOnClickListener(mBleCtrlClickListener);
        mTVList.add(mViewWidgetGroup.mResBlingSlow);

        mViewWidgetGroup.mDoubleBling=(TextView)layout.findViewById(R.id.light_state_brb);
        mViewWidgetGroup.mDoubleBling.setOnClickListener(mBleCtrlClickListener);
        mTVList.add(mViewWidgetGroup.mDoubleBling);

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
        //led 状态
        public TextView mRedBright;
        public TextView mRedDark;
        public TextView mBlueOn;
        public TextView mRedBlingFast;
        public TextView mResBlingSlow;
        public TextView mDoubleBling;
    }

    private void checkedLightState(TextView tv){
        for(TextView t:mTVList){
            if(tv.getId()==t.getId()){
                t.setTextColor(getTextColor(R.color.green));
            }else {
                t.setTextColor(getTextColor(R.color.button_gey));
            }
        }
    }

    private void setLightStateShow(){
        String state=mDataStore.readLightState();
        TextView tv=null;
        switch (state){
            case "RedBright":{
                tv=mViewWidgetGroup.mRedBright;
                break;
            }
            case "RedDark":{
                tv=mViewWidgetGroup.mRedDark;
                break;
            }
            case "BlueOn":{
                tv=mViewWidgetGroup.mBlueOn;
                break;
            }
            case "RedBlingFast":{
                tv=mViewWidgetGroup.mRedBlingFast;
                break;
            }
            case "ResBlingSlow":{
                tv=mViewWidgetGroup.mResBlingSlow;
                break;
            }
            case "DoubleBling":{
                tv=mViewWidgetGroup.mDoubleBling;
                break;
            }
            default:{
                break;
            }
        }
        if(tv!=null){
            checkedLightState(tv);
        }
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
                        mViewWidgetGroup.mConnectStateTV.setTextColor(getTextColor(R.color.green));
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
                        mViewWidgetGroup.mConnectStateTV.setTextColor(getTextColor(R.color.red));
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

    private int getTextColor(int colorId){
        return mContext.getResources().getColor(colorId);
    }

    private void changeSwitchState(){
        if(mIsLedOn){
            mViewWidgetGroup.mLedOnBtn.setEnabled(false);
            mViewWidgetGroup.mLedOffBtn.setEnabled(true);
            mViewWidgetGroup.mLedSwitchBtn.setEnabled(true);
        }else {
            mViewWidgetGroup.mLedOnBtn.setEnabled(true);
            mViewWidgetGroup.mLedOffBtn.setEnabled(false);
            mViewWidgetGroup.mLedSwitchBtn.setEnabled(false);
        }
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
                        changeSwitchState();
                    }else {
                        Toast.makeText(mContext,getStringById(R.string.open_fail_str),Toast.LENGTH_SHORT).show();
                    }
                    setLightStateShow();
                    break;
                }
                case R.id.led_off_btn:{
                    if(mBCS!=null){
                        String cmd=new String(getStringById(R.string.open_close_cmd_str));
                        mService.writeCharacteristic(mBCS,cmd.getBytes());
                        mIsLedOn=false;
                        changeSwitchState();
                    }else {
                        Toast.makeText(mContext,getStringById(R.string.close_fail_str),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.led_switch_btn:{
                    if(mBCS!=null){
                        String cmd=new String(getStringById(R.string.switch_cmd_str));
                        mService.writeCharacteristic(mBCS,cmd.getBytes());
                        //切换仪表盘的试图
                        String state=mDataStore.readLightState();
                        if(!state.equals("")){
                            for(int index=0;index<6;index++){
                                if(mLedStateArr[index].equals(state)){
                                    if(5==index){
                                        mDataStore.writeLightState(mLedStateArr[0]);
                                    }else {
                                        mDataStore.writeLightState(mLedStateArr[++index]);
                                    }
                                }
                            }
                        }
                        setLightStateShow();
                    }else {
                        Toast.makeText(mContext,getStringById(R.string.switch_fail_str),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.light_state_rb:{
                    mDataStore.writeLightState(mLedStateArr[0]);
                    setLightStateShow();
                    break;
                }
                case R.id.light_state_rd:{
                    mDataStore.writeLightState(mLedStateArr[1]);
                    setLightStateShow();
                    break;
                }
                case R.id.light_state_bo:{
                    mDataStore.writeLightState(mLedStateArr[2]);
                    setLightStateShow();
                    break;
                }
                case R.id.light_state_rbf:{
                    mDataStore.writeLightState(mLedStateArr[3]);
                    setLightStateShow();
                    break;
                }
                case R.id.light_state_rbs:{
                    mDataStore.writeLightState(mLedStateArr[4]);
                    setLightStateShow();
                    break;
                }
                case R.id.light_state_brb:{
                    mDataStore.writeLightState(mLedStateArr[5]);
                    setLightStateShow();
                    break;
                }
                default:{
                    break;
                }
            }
        }
    }

}
