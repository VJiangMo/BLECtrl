package com.vonchenchen.mybledemo.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.vonchenchen.mybledemo.R;
import com.vonchenchen.mybledemo.view.ProgressImage;

/**
 * Created by vonchenchen on 2015/10/30 0030.
 */
public class BaseActivity extends Activity {

    private ProgressBar mProgressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.main_color);//通知栏所需颜色
        }

        initProgressBar();
    }

    private void initProgressBar(){
        if (mProgressBar == null) {
            mProgressBar = new ProgressImage(this).getProgress();//dialog的上下文必须是activity，其他的先可以用application
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void addProgressBar(ViewGroup parentView){
        if(parentView != null) {
            if(mProgressBar.getParent() == null) {
                parentView.addView(mProgressBar);
            }
        }
    }

    public void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar(){
        mProgressBar.setVisibility(View.GONE);
    }



    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
