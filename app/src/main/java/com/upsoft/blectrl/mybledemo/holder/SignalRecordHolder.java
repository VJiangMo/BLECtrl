package com.vonchenchen.mybledemo.holder;

import android.view.View;
import android.widget.TextView;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.base.BaseWidgetHolder;
import com.vonchenchen.mybledemo.bean.RecordInfo;
import com.vonchenchen.mybledemo.utils.UIUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/3/16 0016.
 */
public class SignalRecordHolder extends BaseWidgetHolder<List<Integer>> {

    private TextView mSignalText;

    @Override
    public View initView() {
        View view = View.inflate(UIUtils.getContext(), R.layout.layout_signal_record, null);
        mSignalText = (TextView) view.findViewById(R.id.tv_signal);
        return view;
    }

    @Override
    public void refreshView(List<Integer> data) {

    }
}
