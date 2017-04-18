package com.vonchenchen.mybledemo.view;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.service.BLEControlService;
import com.vonchenchen.mybledemo.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vonchenchen on 2016/2/15 0015.
 */
public class BLEServiceList_bak extends RelativeLayout{

    Context mContext;
    private ListView mListView;

    private List<BluetoothGattService> mDataList;
    private List<Boolean> mCheckExtendsList;
    private BLEServiceAdapter mBLEServiceAdapter;

    public BLEServiceList_bak(Context context) {
        super(context);
        this.mContext = context;
    }

    public BLEServiceList_bak(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(mContext, R.layout.layout_service_list, this);

        mListView = (ListView) findViewById(R.id.lv_main);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                LinearLayout extendsLayout = (LinearLayout) view.findViewById(R.id.ll_extends);

                extendsLayout.removeAllViews();
                if(!mCheckExtendsList.get(i)) {

                    mCheckExtendsList.set(i, true);

                    BluetoothGattService gattService = mDataList.get(i);

                    List<BluetoothGattCharacteristic> gattCharacteristicList = gattService.getCharacteristics();
                    view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    view.getLayoutParams().height = UIUtils.dip2px(80) * gattCharacteristicList.size();

                    for (int j = 0; j < gattCharacteristicList.size(); j++) {
                        BLECharInfoView bLECharInfoView = new BLECharInfoView(mContext);
                        bLECharInfoView.setUUidText(gattCharacteristicList.get(j).getUuid().toString());

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        extendsLayout.addView(bLECharInfoView, params);
                    }

                }else{
                    mCheckExtendsList.set(i, false);
                    extendsLayout.getLayoutParams().height = 0;
                }
            }
        });
    }

    public void setDataList(List<BluetoothGattService> dataList){
        this.mDataList = dataList;
        this.mCheckExtendsList = new ArrayList<Boolean>();
        for(int i=0; i<mDataList.size(); i++){
            mCheckExtendsList.add(false);
        }

        if(mDataList != null){
            if(mBLEServiceAdapter == null){
                mBLEServiceAdapter = new BLEServiceAdapter();
                mListView.setAdapter(mBLEServiceAdapter);
            }else{
                mBLEServiceAdapter.notifyDataSetChanged();
            }
        }
    }

    private class BLEServiceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return mDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

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
                mTypeText.setText("type:" + type + "     system service");
            }else{
                mTypeText.setText("type:" + type);
            }

            if(mCharDataList != null && mCharDataList.size() > 0) {
                for (int i = 0; i < mCharDataList.size(); i++) {

//                    BLECharInfoView bLECharInfoView = new BLECharInfoView(mContext);
//                    mContentLayout.addView(bLECharInfoView);
//
//                    BluetoothGattCharacteristic bluetoothGattCharacteristic = mCharDataList.get(i);
//
//                    String charUUidStr = bluetoothGattCharacteristic.getUuid().toString();
//                    //String charValueStr = new String(bluetoothGattCharacteristic.getValue());
//                    int properties = bluetoothGattCharacteristic.getProperties();
//
//                    bLECharInfoView.setUUidText(charUUidStr);

//                    List<BluetoothGattDescriptor> discripters = bluetoothGattCharacteristic.getDescriptors();
//                    for(int j=0; j<discripters.size(); j++){
//                        //new String(discripters.get(j).getValue());
//                    }
                }
            }
        }
    }
}
