package com.vonchenchen.mybledemo.view;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.receiver.BLEStatusChangeReceiver;
import com.vonchenchen.mybledemo.service.BLEControlService;
import com.vonchenchen.mybledemo.utils.BLEUtils;
import com.vonchenchen.mybledemo.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by vonchenchen on 2016/2/15 0015.
 */
public class BLEServiceList extends RelativeLayout{

    Context mContext;
    private ExpandableListView  mListView;

    private View mRootView = null;
    //Service
    private List<BluetoothGattService> mDataList;
    //Characteristic
    private List<List<BluetoothGattCharacteristic>> mDetailDataList = new ArrayList<List<BluetoothGattCharacteristic>>();

    private List<Boolean> mCheckExtendsList;
    private BLEServiceAdapter mBLEServiceAdapter;
    private View mLeftView;

    private OnLeftClickListener mOnLeftClickListener;

    private PopupWindow mWritePopupWindow = null;

    private BLEControlService mService;
    private BLEStatusChangeReceiver mBroadcastReceiver;

    public BLEServiceList(Context context) {
        super(context);
        this.mContext = context;
    }

    public BLEServiceList(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(mContext, R.layout.layout_service_list, this);

        mListView = (ExpandableListView) findViewById(R.id.lv_main);
        mLeftView = findViewById(R.id.ll_left);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                LinearLayout extendsLayout = (LinearLayout) view.findViewById(R.id.ll_extends);

                extendsLayout.removeAllViews();
                if (!mCheckExtendsList.get(i)) {

                    mCheckExtendsList.set(i, true);

                    BluetoothGattService gattService = mDataList.get(i);

                    List<BluetoothGattCharacteristic> gattCharacteristicList = gattService.getCharacteristics();
//                    view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
//                    view.getLayoutParams().height = UIUtils.dip2px(80) * gattCharacteristicList.size();
//
//                    for (int j = 0; j < gattCharacteristicList.size(); j++) {
//                        BLECharInfoView bLECharInfoView = new BLECharInfoView(mContext);
//                        bLECharInfoView.setUUidText(gattCharacteristicList.get(j).getUuid().toString());
//
//                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                        extendsLayout.addView(bLECharInfoView, params);
//                    }

                } else {
                    mCheckExtendsList.set(i, false);
                    extendsLayout.getLayoutParams().height = 0;
                }
            }
        });

        mLeftView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnLeftClickListener != null){
                    mOnLeftClickListener.onLeftClick(BLEServiceList.this);
                }
            }
        });
    }

    public void setDataList(List<BluetoothGattService> dataList){
        this.mDataList = dataList;

        mDetailDataList.clear();
        if(mDataList != null) {
            for (int i = 0; i < mDataList.size(); i++) {
                mDetailDataList.add(mDataList.get(i).getCharacteristics());
            }

            if (mDataList != null) {
                if (mBLEServiceAdapter == null) {
                    mBLEServiceAdapter = new BLEServiceAdapter();
                    mListView.setAdapter(mBLEServiceAdapter);
                } else {
                    mBLEServiceAdapter.notifyDataSetChanged();
                }
            }
        }else{
            Toast.makeText(UIUtils.getContext(), "没有可用服务", Toast.LENGTH_SHORT).show();
        }
    }

    public void setService(BLEControlService service){
        this.mService = service;
    }
    public void setBroadcastReceiver(BLEStatusChangeReceiver broadcastReceiver){
        this.mBroadcastReceiver = broadcastReceiver;
    }

    private class BLEServiceAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return mDataList.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return mDetailDataList.get(i).size();
        }

        @Override
        public Object getGroup(int i) {
            return mDataList.get(i);
        }

        @Override
        public Object getChild(int i, int i1) {
            return mDetailDataList.get(i).get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

            ViewHolder holder = null;
            if(view == null){
                holder = new ViewHolder();
                view = holder.getRootView();
            }else{
                holder = (ViewHolder) view.getTag();
            }
            holder.refreshView(mDataList.get(i));

            return view;
        }

        @Override
        public View getChildView(final int i,final int i1, boolean b, View view, ViewGroup viewGroup) {

            view = View.inflate(mContext, R.layout.item_characteristic_info, null);
            TextView nameText = (TextView) view.findViewById(R.id.tv_name);
            final TextView uuidText = (TextView) view.findViewById(R.id.tv_uuid);
            TextView uuidTextTitle = (TextView) view.findViewById(R.id.tv_uuid_title);
            TextView propertiesText = (TextView) view.findViewById(R.id.tv_properties);
            final TextView valueText = (TextView) view.findViewById(R.id.tv_value);
            TextView writetypeText = (TextView) view.findViewById(R.id.tv_writetype);
            final TextView discriptersText = (TextView) view.findViewById(R.id.tv_discripters);
            TextView discriptersUUIDText = (TextView) view.findViewById(R.id.tv_descripter_uuid);
            final TextView discriptersValueText = (TextView) view.findViewById(R.id.tv_descripter_value);
            TextView discriptersUUIDTextTitle = (TextView) view.findViewById(R.id.tv_descripter_uuid_title);

            View descriptorUUIDLayout = view.findViewById(R.id.ll_descripter_uuid_layout);
            View descriptorValueLayout = view.findViewById(R.id.ll_descripter_value_layout);
            TextView descriptorTitleText = (TextView) view.findViewById(R.id.tv_descripter_title);

            Button readBtn = (Button) view.findViewById(R.id.btn_read);
            Button writeBtn = (Button) view.findViewById(R.id.btn_write);
            Button desReadBtn = (Button) view.findViewById(R.id.btn_des_read);

            final BluetoothGattCharacteristic bluetoothGattCharacteristic = mDetailDataList.get(i).get(i1);
            final String uuid = bluetoothGattCharacteristic.getUuid().toString();

            readBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    //设置返回读取信息后的回调
                    mBroadcastReceiver.updateOnReceiveDataListener(new BLEStatusChangeReceiver.OnReceiveDataListener() {
                        @Override
                        public void getRecivedData(final String _uuid, final byte[] value) {
                            ((Activity)mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(_uuid.equals(uuid)) {
                                        valueText.setText(new String(value));
                                    }
                                }
                            });
                        }
                    });

                    //读取信息
                    mService.readCharacteristic(mDetailDataList.get(i).get(i1));
                }
            });

            writeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    popWindowWrite(bluetoothGattCharacteristic);
                }
            });

            desReadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    final List<BluetoothGattDescriptor> descriptors = mDetailDataList.get(i).get(i1).getDescriptors();
                    if(descriptors != null){

                        //设置返回读取信息后的回调
                        mBroadcastReceiver.updateOnReceiveDataListener(new BLEStatusChangeReceiver.OnReceiveDataListener() {
                            @Override
                            public void getRecivedData(final String _uuid, final byte[] value) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(_uuid.equals(descriptors.get(0).getUuid()))
                                            discriptersText.setText(new String(value));
                                    }
                                });
                            }
                        });

                        mService.readDiscriptor(descriptors.get(0));
                    }
                }
            });

            int properties = bluetoothGattCharacteristic.getProperties();
            String value =null;
            if(bluetoothGattCharacteristic.getValue() != null) {
                value = new String(bluetoothGattCharacteristic.getValue());
            }

            int writeType = bluetoothGattCharacteristic.getWriteType();
            List<BluetoothGattDescriptor> descriptors = bluetoothGattCharacteristic.getDescriptors();
            String descriptorName = null;
            String descriptorUUID = null;
            String descriptorValue = null;
            if(descriptors.size() > 0) {

                descriptorUUID = descriptors.get(0).getUuid().toString();

                final UUID fUuid = descriptors.get(0).getUuid();
                if (!TextUtils.isEmpty(descriptorUUID)) {

                    if (descriptors.get(0).getValue() != null) {
                        descriptorValue = new String(descriptors.get(0).getValue());
                    }
                    //descriptorValue = new String(bluetoothGattCharacteristic.getDescriptor(fUuid).getValue());
                }
            } else{
                desReadBtn.setVisibility(View.GONE);
                descriptorUUIDLayout.setVisibility(View.GONE);
                descriptorValueLayout.setVisibility(View.GONE);
                descriptorTitleText.setVisibility(View.GONE);
            }

            if(!TextUtils.isEmpty(uuid)){
                uuidText.setText(uuid);
                uuidTextTitle.setText("UUID: "+BLEUtils.getBLECharactorsticType(uuid.substring(4,8)));
            }
            if(!TextUtils.isEmpty(properties+"")){
                //propertiesText.setText(properties+"");
                propertiesText.setText(BLEUtils.getBLEPorperties(properties));

                if((properties & 0x0c) == 0){     //不能写入
                    writeBtn.setVisibility(View.GONE);
                }
            }
//            if(!TextUtils.isEmpty(value)){
//                valueText.setText(value);
//            }
            if(!TextUtils.isEmpty(writeType+"")){
                writetypeText.setText(writeType+"");
            }
            if(descriptors != null && descriptors.size() == 0){
                //discriptersText.setText(uuid);
            }
            if(!TextUtils.isEmpty(descriptorUUID)){
                discriptersUUIDTextTitle.setText("Descriptor: "+BLEUtils.getBLECharactorsticDescripter(descriptorUUID.substring(4, 8)));
                discriptersUUIDText.setText("UUID: "+descriptorUUID);//
            }
            if(!TextUtils.isEmpty(descriptorValue)){
                discriptersValueText.setText(descriptorValue);
            }

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }

    class ViewHolder extends BaseWidgetHolder<BluetoothGattService>{

        private TextView mUUidText;
        private TextView mTypeText;
        private LinearLayout mContentLayout;
        private TextView mDiscripter;

        List<BluetoothGattCharacteristic> mCharDataList;

        @Override
        public View initView() {

            View view = View.inflate(mContext, R.layout.item_ble_service_info, null);
            mUUidText = (TextView) view.findViewById(R.id.tv_uuid);
            mTypeText = (TextView) view.findViewById(R.id.tv_type);
            mDiscripter = (TextView) view.findViewById(R.id.tv_discripter);

            return view;
        }

        @Override
        public void refreshView(BluetoothGattService data) {
            String uuidStr = data.getUuid().toString();
            String type = Integer.toHexString(data.getType());
            mCharDataList = data.getCharacteristics();

            mUUidText.setText("UUID:"+uuidStr);

            if(uuidStr.substring(9).equals(BLEControlService.SYS_UUID_REAR_STR)){
                mTypeText.setText("Service: " + BLEUtils.getBLEServiceInfo(uuidStr.substring(4, 8)));
            }else{
                mTypeText.setText("Service: " + type);
            }
        }
    }

    public interface OnLeftClickListener{
        public void onLeftClick(View view);
    }

    public void setOnLeftClickListener(OnLeftClickListener onLeftClickListener){
        this.mOnLeftClickListener = onLeftClickListener;
    }

    public void setRootView(View root){
        this.mRootView = root;
    }

    private void popWindowWrite(final BluetoothGattCharacteristic characteristic){

        View view = View.inflate(mContext, R.layout.layout_pop_write, null);

        final EditText content = (EditText) view.findViewById(R.id.et_content);
        Button cancelBtn = (Button) view.findViewById(R.id.btn_cancle);
        Button sendBtn = (Button) view.findViewById(R.id.btn_send);

        if(mWritePopupWindow == null) {
            mWritePopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2px(300), false);
            mWritePopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mWritePopupWindow.setFocusable(true);
            mWritePopupWindow.setOutsideTouchable(true);
        }

        mWritePopupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);

        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mWritePopupWindow.dismiss();
            }
        });

        sendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.writeCharacteristic(characteristic, content.getText().toString().getBytes());
            }
        });
    }
}
