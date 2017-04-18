package com.vonchenchen.mybledemo.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by vonchenchen on 2016/1/29 0029.
 */
public class MyBLEApplication extends Application{

    private static MyBLEApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mInstance = this;
    }

    public static Context getContext(){
        return mInstance;
    }
}
