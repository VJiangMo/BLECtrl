package com.vonchenchen.mybledemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vonchenchen.mybledemo.R;

/**
 * Created by vonchenchen on 2016/2/16 0016.
 */
public class BLECharInfoView extends RelativeLayout{

    private Context mContext;
    private TextView mUUidText;

    private String mUUidStr;

    public BLECharInfoView(Context context) {
        super(context);
        mContext = context;
    }

    public BLECharInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(mContext, R.layout.item_ble_char_info, this);

        mUUidText = (TextView) findViewById(R.id.tv_uuid);

        mUUidText.setText("uuid"+mUUidStr);
    }

    public void setUUidText(String uuid){
        this.mUUidStr = uuid;
        if(mUUidText != null){
            mUUidText.setText("uuid"+mUUidStr);
        }
    }
}
