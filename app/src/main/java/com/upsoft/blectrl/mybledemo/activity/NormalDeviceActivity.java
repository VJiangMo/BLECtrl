package com.vonchenchen.mybledemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.base.BaseActivity;
import com.vonchenchen.mybledemo.base.BaseWidgetHolder;
import com.vonchenchen.mybledemo.bean.RecordInfo;
import com.vonchenchen.mybledemo.holder.InfoRecordHolder;
import com.vonchenchen.mybledemo.holder.SignalRecordHolder;
import com.vonchenchen.mybledemo.receiver.BLEStatusChangeReceiver;
import com.vonchenchen.mybledemo.service.BLEControlService;
import com.vonchenchen.mybledemo.utils.TimeUtils;
import com.vonchenchen.mybledemo.view.BLEServiceList;
import com.vonchenchen.mybledemo.view.SimpleTitleBar;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vonchenchen on 2016/1/29 0029.
 */
public class NormalDeviceActivity extends BaseActivity{

    private static final String TAG = "NormalDeviceActivity";

    private final String PACKAGE_TYPE_HEART = "ABABAB";
    private final String PACKAGE_TYPE_DATA = "ACACAC"  ;

    private BluetoothDevice mBLEDevice = null;

    private BLEControlService mService = null;
    private final BLEStatusChangeReceiver mBLEStatusChangeReceiver = new BLEStatusChangeReceiver();
    List<BluetoothGattService> mBluetoothGattServiceList = null;

    String mString;
    private TextView mReceiveText;
    private TextView mSendEdit;
    private Button mSendBtn;
    private Button mCleanBtn;
    private Button mConnectBtn;

    private String mReceiveStr;
    private SimpleTitleBar mSimpleTitleBar;
    private TextView mAddressText;
    private String mDeviceAddress;

    private final int CONNECT_STATUS_CONNECTED = 1;
    private final int CONNECT_STATUS_DISCONNECTED = 2;
    private int mConnectStatus = CONNECT_STATUS_DISCONNECTED;

    private boolean mIsTransmission = false;

    private PopupWindow mServiceInfoPopupWindow = null;

    private Button mServiceInfoBtn;
    private View mRootView;
    private ViewPager mViewPager;
    private MyPagerAdapter mMyPagerAdapter;
    private List<BaseWidgetHolder> mHolderList = new ArrayList<BaseWidgetHolder>();

    private InfoRecordHolder mInfoRecordHolder;
    private ArrayList<RecordInfo> mInfoRecordDataList = new ArrayList<RecordInfo>();

    private SignalRecordHolder mSignalRecordHolder;
    private List<Integer> mSignalPowerDataList = new ArrayList<Integer>();

    private Timer mRssiTimer = new Timer();
    private TimerTask mRssiTimeTask = new TimerTask() {
        @Override
        public void run() {
            if(mService != null){
                mService.readRemoteRssi();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_device);

        mRootView = findViewById(R.id.ll_root);

        mAddressText = (TextView) findViewById(R.id.tv_address);

        mSendBtn = (Button) findViewById(R.id.btn_send);
        mCleanBtn = (Button) findViewById(R.id.btn_clean);
        mConnectBtn = (Button) findViewById(R.id.btn_connect);
        mServiceInfoBtn = (Button) findViewById(R.id.btn_service);
        mSimpleTitleBar = (SimpleTitleBar) findViewById(R.id.titleBar);
        mSimpleTitleBar.setTitle("wait for connect ...");

        mViewPager = (ViewPager) findViewById(R.id.vp_main);

        initBLEControlService();

        mCleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInfoRecordDataList.clear();
                mInfoRecordHolder.refreshView(mInfoRecordDataList);
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendStr = mSendEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(sendStr)) {
                    sendToBLEDevice(sendStr);
                } else {
                    Toast.makeText(NormalDeviceActivity.this, "please enter some data !", Toast.LENGTH_SHORT).show();
                }
                //test
                mService.getDisInfo();
            }
        });

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectBLEDevice();
            }
        });

        mServiceInfoBtn.setOnClickListener(new View.OnClickListener() {

            private BLEServiceList mBLEServiceList;

            @Override
            public void onClick(View view) {
                View popView = View.inflate(NormalDeviceActivity.this, R.layout.layout_device_service_info, null);
                mBLEServiceList = (BLEServiceList) popView.findViewById(R.id.BLES_main);

                mBLEServiceList.setService(mService);
                mBLEServiceList.setBroadcastReceiver(mBLEStatusChangeReceiver);
                mBLEServiceList.setRootView(mServiceInfoBtn);

                mBLEServiceList.setOnLeftClickListener(new BLEServiceList.OnLeftClickListener() {
                    @Override
                    public void onLeftClick(View view) {
                        mServiceInfoPopupWindow.dismiss();
                    }
                });

                if(mService != null) {
                    mBLEServiceList.setDataList(mService.getBLEServices());

                    mServiceInfoPopupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    mServiceInfoPopupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));
                    mServiceInfoPopupWindow.setFocusable(true);
                    mServiceInfoPopupWindow.setOutsideTouchable(true);

                    mServiceInfoPopupWindow.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
                }
            }
        });

        unConnectedState();

        initPager();
    }

    private void initBLEControlService() {
        //create BLEControService
        Intent bindIntent = new Intent(this, BLEControlService.class);
        //binding BLEControService callback
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //register listener that listen BLE status change callback
        LocalBroadcastManager.getInstance(this).registerReceiver(mBLEStatusChangeReceiver, makeGattUpdateIntentFilter());

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);

        mBLEDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDeviceAddress);

        //connectBLEDevice();
        mBLEStatusChangeReceiver.setOnBLEStatusChangeListener(new BLEStatusChangeReceiver.OnBLEStatusChangeListener() {
            @Override
            public void onConnected() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NormalDeviceActivity.this, "connected", Toast.LENGTH_SHORT).show();
                        mSimpleTitleBar.setTitle("connected");
                        mAddressText.setText(mDeviceAddress);

                        mInfoRecordDataList.add(getCurrRecordInfo("Connected", 0xff000000));
                        mInfoRecordHolder.refreshView(mInfoRecordDataList);
                    }
                });
            }

            @Override
            public void onDisConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NormalDeviceActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
                        mSimpleTitleBar.setTitle("Disconnected");

                        mInfoRecordDataList.add(getCurrRecordInfo("Disconnected", 0xff000000));
                        mInfoRecordHolder.refreshView(mInfoRecordDataList);
                    }
                });
            }

            @Override
            public void onGattServiceDiscovered() {
                mSimpleTitleBar.setTitle("Service Discovered");
                mInfoRecordDataList.add(getCurrRecordInfo("Service Discovered", 0xff000000));
                mInfoRecordHolder.refreshView(mInfoRecordDataList);

                mRssiTimer.schedule(mRssiTimeTask, 3000, 3000);
            }

            @Override
            public void onDataChange(String uuid, byte[] value, String type) {

                if(type.equals(BLEControlService.TYPE_READ)) {
                    mInfoRecordDataList.add(getCurrRecordInfo(new String(value), 0x55FF0000));
                }else if(type.equals(BLEControlService.TYPE_WRITE)){
                    mInfoRecordDataList.add(getCurrRecordInfo(new String(value), 0x550000ff));
                }else if(type.equals(BLEControlService.TYPE_DESCRIPTOR_READ)){
                    mInfoRecordDataList.add(getCurrRecordInfo(new String(value), 0x55ff0000));
                }else if(type.equals(BLEControlService.TYPE_DESCRIPTOR_WRITE)){
                    mInfoRecordDataList.add(getCurrRecordInfo(new String(value), 0x550000ff));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mInfoRecordHolder.refreshView(mInfoRecordDataList);
                    }
                });
            }

            @Override
            public void onRssiRead(int rssi, String type) {
                mInfoRecordDataList.add(getCurrRecordInfo("rssi " + rssi +"db", 0x55006677));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mInfoRecordHolder.refreshView(mInfoRecordDataList);
                    }
                });
            }
        });
    }

    private void connectBLEDevice(){
        mService.connect(mDeviceAddress);
        connectedState();
    }

    private void disconnectBLEDevice(){
        mService.disconnect();
    }

    private void sendToBLEDevice(String data){
        try {
            byte[] value = data.getBytes("UTF-8");
            mService.writeRXCharacteristic(value);
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        //绑定服务时
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {

            mService = ((BLEControlService.LocalBinder) rawBinder).getService();

            mBLEStatusChangeReceiver.setBLEService(mService);

            mConnectStatus = CONNECT_STATUS_CONNECTED;

            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Toast.makeText(NormalDeviceActivity.this, "connect BLE success", Toast.LENGTH_SHORT).show();
        }
        //断开服务时
        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
            mConnectStatus = CONNECT_STATUS_DISCONNECTED;
            Toast.makeText(NormalDeviceActivity.this, "binding service failed", Toast.LENGTH_SHORT).show();
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


    public interface OnReceiveDataListener{
        void getRecivedData(byte[] value, String uuid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBLEStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        if(mService != null) {
            mService.stopSelf();
            mService = null;
        }
    }

    private void unConnectedState(){
        mCleanBtn.setEnabled(false);
        mCleanBtn.setTextColor(getResources().getColor(R.color.gray));
        mSendBtn.setEnabled(false);
        mSendBtn.setTextColor(getResources().getColor(R.color.gray));
        mServiceInfoBtn.setEnabled(false);
        mServiceInfoBtn.setTextColor(getResources().getColor(R.color.gray));
    }

    private void connectedState(){
        mCleanBtn.setEnabled(true);
        mCleanBtn.setTextColor(getResources().getColor(R.color.black));
        mSendBtn.setEnabled(true);
        mSendBtn.setTextColor(getResources().getColor(R.color.black));
        mServiceInfoBtn.setEnabled(true);
        mServiceInfoBtn.setTextColor(getResources().getColor(R.color.black));
    }

    public class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mHolderList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            container.addView(mHolderList.get(position).getRootView());
            return mHolderList.get(position).getRootView();
        }
    }

    private RecordInfo getCurrRecordInfo(String content, int contentColor){
        RecordInfo recordInfo = new RecordInfo();
        recordInfo.time = TimeUtils.getFormatedTime(System.currentTimeMillis(), "HH:mm:ss");
        recordInfo.content = content;
        recordInfo.contentColor = contentColor;
        return recordInfo;
    }

    private void initPager(){

        mInfoRecordHolder = new InfoRecordHolder();
        mInfoRecordHolder.refreshView(mInfoRecordDataList);

        mSignalRecordHolder = new SignalRecordHolder();

        mHolderList.add(mInfoRecordHolder);
        mHolderList.add(mSignalRecordHolder);

        if(mMyPagerAdapter == null) {
            mMyPagerAdapter = new MyPagerAdapter();
        }

        mViewPager.setAdapter(mMyPagerAdapter);
    }
}
