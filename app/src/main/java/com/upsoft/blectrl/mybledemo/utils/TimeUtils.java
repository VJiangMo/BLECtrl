package com.vonchenchen.mybledemo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vonchenchen on 2016/3/12 0012.
 */
public class TimeUtils {

    /**
     * 获得yyyy-MM-dd HH:mm:ss格式
     *
     * @param time
     * @return
     */
    public static String getFormatedTime(long time, String format) {
        String retTime = null;
        SimpleDateFormat sdf = null;

        sdf = new SimpleDateFormat(format);
        retTime = sdf.format(new Date(time));
        return retTime;
    }
}
