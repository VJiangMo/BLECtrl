package com.vonchenchen.mybledemo.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.base.BaseWidgetHolder;
import com.vonchenchen.mybledemo.bean.RecordInfo;
import com.vonchenchen.mybledemo.utils.UIUtils;

import java.util.List;

/**
 * Created by vonchenchen on 2016/3/9 0009.
 */
public class InfoRecordHolder extends BaseWidgetHolder<List<RecordInfo>> {


    private ListView mListView;

    private InfoRecordAdapter mInfoRecordAdapter = null;

    @Override
    public View initView() {

        View view = View.inflate(UIUtils.getContext(), R.layout.layout_info_record, null);
        mListView = (ListView) view.findViewById(R.id.lv_record);

        return view;
    }

    @Override
    public void refreshView(List<RecordInfo> data) {

        if(mInfoRecordAdapter == null){
            mInfoRecordAdapter = new InfoRecordAdapter(data);
            mListView.setAdapter(mInfoRecordAdapter);
        }else{
            mInfoRecordAdapter.notifyDataSetChanged();
        }
    }

    public class InfoRecordAdapter extends BaseAdapter{

        private List<RecordInfo> mDataList;

        public InfoRecordAdapter(List<RecordInfo> data){
            this.mDataList = data;
        }

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

            ViewHolde holder;
            if(view == null) {
                view = View.inflate(UIUtils.getContext(), R.layout.layout_info_record_item, null);
                holder = new ViewHolde();
                holder.contentText = (TextView)view.findViewById(R.id.tv_content);
                holder.timeText = (TextView) view.findViewById(R.id.tv_time);
                view.setTag(holder);
            }else {
                holder = (ViewHolde) view.getTag();
            }

            holder.contentText.setText(mDataList.get(i).content);
            holder.contentText.setTextColor(mDataList.get(i).contentColor);

            holder.timeText.setText(mDataList.get(i).time);

            return view;
        }

        private class ViewHolde{
            TextView timeText;
            TextView contentText;
        }
    }
}
