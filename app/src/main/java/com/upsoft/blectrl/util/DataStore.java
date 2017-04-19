package com.upsoft.blectrl.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by BASTA on 2017/4/18.
 */

public class DataStore {
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private final String mLightState="ligheState";

    public DataStore(Context ctx){
        mContext=ctx;
        mSharedPreferences=mContext.getSharedPreferences("BLECtrlData",
                Context.MODE_PRIVATE);
        mEditor=mSharedPreferences.edit();
    }

    public void writeLightState(String val){
        writeData(mLightState,val);
    }

    public String readLightState(){
        return readData(mLightState);
    }

    public void writeData(String key,String val){
        mEditor.putString(key,val);
        mEditor.commit();
    }

    public void writeData(HashMap<String,String> data){
        Iterator iter = data.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iter.next();
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();
            mEditor.putString(key,val);
        }
        mEditor.commit();
    }

    public String readData(String key){
        String res=mSharedPreferences.getString(key,"");
        return res;
    }
}
