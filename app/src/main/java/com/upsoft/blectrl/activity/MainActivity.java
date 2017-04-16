package com.upsoft.blectrl.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.upsoft.blectrl.R;
import com.upsoft.blectrl.fragment.BleFragment;
import com.upsoft.blectrl.utils.DoubleClickExitHelper;

/**
 * Created by yangzhou on 2017/4/16.
 */

public class MainActivity extends Activity {
    private static final int size=3;
    private Fragment[] mFragmentArr;
    private FragmentManager mFragmentManager;
    private String mBlueToothAddress="";//蓝牙地址
    private SharedPreferences mSP;//用于保存初始里程数、市场油价及其他需要缓存的数据
    private DoubleClickExitHelper mDoubleClickExit;//双击返回按钮退出

    public SharedPreferences getSP(){
        return mSP;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return mDoubleClickExit.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        LayoutInflater inflater=this.getLayoutInflater();
        View layoutView=inflater.inflate(R.layout.activity_main,null);
        setContentView(layoutView);
        mSP=this.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        mDoubleClickExit = new DoubleClickExitHelper(this);

        initFragmentArr();

        //默认显示蓝牙连接界面
        mFragmentManager.beginTransaction();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void initFragmentArr(){
        if(null==mFragmentArr){
            mFragmentArr=new Fragment[size];
            mFragmentArr[0]=new BleFragment();
            showFragment(mFragmentArr[0]);//默认显示蓝牙搜索界面
        }
    }

    public void swithToObdFragment(String blueToothAddress){
        mBlueToothAddress=blueToothAddress;
        showFragment(mFragmentArr[1]);
    }

    public String getBlueToothAddress(){
        return mBlueToothAddress;
    }

    private void showFragment(Fragment fragment){
        mFragmentManager= this.getFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_area,fragment);
        fragmentTransaction.commit();
    }

}

