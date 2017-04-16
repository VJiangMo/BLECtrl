package com.upsoft.blectrl;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by yangzhou on 2017/1/12.
 */

public class SoftApplication extends Application {
    private final String mTag=this.getClass().toString();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(mTag, "onCreate: SoftApplication.");

    }
}
