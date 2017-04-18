package com.upsoft.blectrl.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2016/1/29 0029.
 */
public class BLEControlService  extends Service {
    private final static String TAG = BLEControlService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private final MyBluetoothGattCallback mGattCallback = new MyBluetoothGattCallback();;

    public static final String TYPE_READ = "4";
    public static final String TYPE_WRITE = "5";
    public static final String TYPE_DESCRIPTOR_READ = "6";
    public static final String TYPE_DESCRIPTOR_WRITE = "7";
    public static final String TYPE_RSSI_READ = "8";

    //broadcast
    public final static String ACTION_GATT_CONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART = "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";
    public final static String RECEIVE_DATA = "com.nordicsemi.nrfUART.RECEIVE_DATA";
    public final static String UUID_DATA = "com.nordicsemi.nrfUART.UUID_DATA";

    public final static String ACTION_TYPE = "com.nordicsemi.nrfUART.ACTION_TYPE";

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final String SYS_UUID_REAR_STR = "0000-1000-8000-00805f9b34fb";

    public class LocalBinder extends Binder {
        public BLEControlService getService() {
            return BLEControlService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }

        mGattCallback.setBluetoothInfoChangeListener(new MyBluetoothGattCallback.BluetoothInfoChangeListener() {
            @Override
            public void onConnectionStateChange(String action) {
                broadcastUpdate(action);
                mBluetoothGatt.discoverServices();
            }
            @Override
            public void onServicesDiscovered(String action) {
                broadcastUpdate(action);
            }
            @Override
            public void onCharacteristicRead(String action, BluetoothGattCharacteristic characteristic) {
                broadcastUpdate(action, characteristic, TYPE_READ);
            }
           @Override
            public void onCharacteristicChanged(String action, BluetoothGattCharacteristic characteristic) {
               broadcastUpdate(action, characteristic, TYPE_WRITE);
            }
            @Override
            public void onDescriptorRead(String action, BluetoothGattDescriptor descriptor) {
                broadcastUpdate(action, descriptor, TYPE_DESCRIPTOR_READ);
            }
            @Override
            public void onDescriptorWrite(String action, BluetoothGattDescriptor descriptor) {
                broadcastUpdate(action, descriptor, TYPE_DESCRIPTOR_WRITE);
            }
            @Override
            public void onReadRemoteRssi(String action, int rssi) {
                broadcastUpdate(action, rssi, TYPE_RSSI_READ);
            }
        });

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        mBluetoothDeviceAddress = address;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        //mBluetoothGatt.close();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void readDiscriptor(BluetoothGattDescriptor descriptor){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readDescriptor(descriptor);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value){

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        characteristic.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void readRemoteRssi(){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    //*******************************************************************************************************

    //*******************************************************************************************************
    public void enableTXNotification()
    {
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void writeRXCharacteristic(byte[] value)
    {
        BluetoothGattService RxService = null;
        //通过UUID获取 接收端的服务
        if(mBluetoothGatt != null) {
            RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        }else{
           Toast.makeText(BLEControlService.this, "please reconnect", Toast.LENGTH_SHORT).show();
            return ;
       }

        showMessage("mBluetoothGatt null"+ mBluetoothGatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        //写入值
        RxChar.setValue(value);

        //写回信息
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

        Log.d(TAG, "write TXchar - status=" + status);
    }

    //****************************************************************************************************
    //test
    public void getDisInfo(){
        BluetoothGattService disService = mBluetoothGatt.getService(DIS_UUID);
        if(disService == null){
            showMessage("Dis charateristic not found!");
        }else{
            showMessage("Dis:" + disService.getCharacteristics());
        }
    }

    /**
     * get BLEService by uuid
     * @param uuid
     * @return
     */
    public BluetoothGattService getBLEService(UUID uuid){

        BluetoothGattService disService = mBluetoothGatt.getService(uuid);
        return disService;
    }

    public List<BluetoothGattService> getBLEServices(){

        if(mBluetoothGatt == null){
            Toast.makeText(this, "BluetoothGatt is null", Toast.LENGTH_SHORT).show();
            return null;
        }

        return mBluetoothGatt.getServices();
    }

    public boolean WriteBLECharacteristic(BluetoothGattService service, UUID characteristicUuid, byte[] value){
        BluetoothGattCharacteristic bluetoothGattCharacteristic = service.getCharacteristic(characteristicUuid);
        bluetoothGattCharacteristic.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
        return status;
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic, String type) {
        final Intent intent = new Intent(action);

        intent.putExtra(BLEControlService.RECEIVE_DATA, characteristic.getValue());
        intent.putExtra(BLEControlService.UUID_DATA, characteristic.getUuid().toString()); //此处不能直接使用intent传递UUID类型的数据
        intent.putExtra(BLEControlService.ACTION_TYPE, type);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattDescriptor descriptor, String type) {
        final Intent intent = new Intent(action);

        intent.putExtra(BLEControlService.RECEIVE_DATA, descriptor.getValue());
        intent.putExtra(BLEControlService.UUID_DATA, descriptor.getUuid().toString()); //此处不能直接使用intent传递UUID类型的数据
        intent.putExtra(BLEControlService.ACTION_TYPE, type);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, int rssi, String type){
        final Intent intent = new Intent(action);
        intent.putExtra(BLEControlService.RECEIVE_DATA, rssi);
        intent.putExtra(BLEControlService.ACTION_TYPE, type);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
