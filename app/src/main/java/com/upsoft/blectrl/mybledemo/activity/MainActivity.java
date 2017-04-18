package com.vonchenchen.mybledemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.adapter.BLEDeviceAdapter;
import com.vonchenchen.mybledemo.base.BaseActivity;
import com.vonchenchen.mybledemo.view.SimpleTitleBar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by vonchenchen on 2016/1/29 0029.
 */
public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    public static final String BLEDEVICE_NAME = "BLEDEVICE_NAME";

    private ListView mBLEDeviceListView;
    private BLEDeviceAdapter mBLEDeviceAdapter;
    private Button mScanButton;
    private ViewGroup mMainLayout;
    private SimpleTitleBar mSimpleTitleBar;

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000; //5 seconds

    private final int RESULTCODE_TRUE_ON_BLUETOOTH = 0;

    private List<BluetoothDevice> mDataList;
    Map<String, Integer> mDevRssiMap;
    private boolean mIsScanning;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mBLEScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBLEDeviceListView = (ListView) findViewById(R.id.lv_device);
        mScanButton = (Button) findViewById(R.id.btn_scan);

        mMainLayout = (ViewGroup) findViewById(R.id.rl_main).getParent();
        addProgressBar(mMainLayout);

        mHandler = new Handler();

        mDataList = new LinkedList<BluetoothDevice>();
        mDevRssiMap = new HashMap<String, Integer>();

        mBLEDeviceAdapter = new BLEDeviceAdapter(MainActivity.this, mDataList);
        mBLEDeviceListView.setAdapter(mBLEDeviceAdapter);

        mSimpleTitleBar = (SimpleTitleBar) findViewById(R.id.titleBar);
        mSimpleTitleBar.setTitle("ble list");

        //自动开启蓝牙
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, 1);

        //BLE
        mBLEScanCallback = getBLEScanCallback();
        checkBLEDevice();
        //scanOtherBLEDevice(true);

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanOtherBLEDevice(!mIsScanning);
            }
        });

        mBLEDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mDataList.get(i);
                mBluetoothAdapter.stopLeScan(mBLEScanCallback);

                Intent intent = new Intent(MainActivity.this, NormalDeviceActivity.class);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device.getAddress());
                intent.putExtra(BLEDEVICE_NAME, device.getName());
                startActivity(intent);
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

    /**
     * scan other ble device
     * @param enable
     */
    private void scanOtherBLEDevice(boolean enable){
        if(enable){
            mDataList.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //stop scan
                    mIsScanning = false;
                    mBluetoothAdapter.stopLeScan(mBLEScanCallback);
                    mScanButton.setText("start scan");
                    hideProgressBar();
                }
            }, SCAN_PERIOD);
            //start scan
            mIsScanning = true;
            mBluetoothAdapter.startLeScan(mBLEScanCallback);
            mScanButton.setText("stop scan");
            showProgressBar();
        }else{
            //stop scan
            mIsScanning = false;
            mBluetoothAdapter.stopLeScan(mBLEScanCallback);
            mScanButton.setText("start scan");
            hideProgressBar();
        }
    }

    private BluetoothAdapter.LeScanCallback getBLEScanCallback(){
        return new BluetoothAdapter.LeScanCallback(){
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

                Log.i("MainActivity", device.getAddress());
                addBLEDeviceData(device, rssi);

            }
        };
    }

    private void addBLEDeviceData(BluetoothDevice device, int rssi){
        boolean deviceFound = false;

        for (BluetoothDevice listDev : mDataList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        mDevRssiMap.put(device.getAddress(), rssi);
        if(!deviceFound){
            mDataList.add(device);
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBLEDeviceAdapter.notifyDataSetChanged();
            }});
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULTCODE_TRUE_ON_BLUETOOTH){
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
