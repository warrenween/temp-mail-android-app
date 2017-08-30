package com.tempmail.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tempmail.services.CheckNewEmailService;

import java.util.Calendar;

/**
 * Created by Lotar on 10.07.2016.
 */
public class CheckEmailAlarmUtils {
    public static final String TAG = CheckEmailAlarmUtils.class.getSimpleName();
    public static final String ALARM_ACTION = "com.tempmail.check_new_emails";
    public static final Integer ALARM_REQUEST_CODE = 111;
    private Context mContext;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    public CheckEmailAlarmUtils(Context context) {
        mContext = context;
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setCheckEmailAlarm(boolean isNeedUpdate) {
        alarmIntent = getCheckEmailServiceIntent(isNeedUpdate);
        if (alarmIntent != null)
            createAlarm(alarmIntent);
    }


    public PendingIntent getCheckEmailServiceIntent(boolean isNeedUpdate) {
        Intent intent = new Intent(mContext, CheckNewEmailService.class);
        intent.setAction(ALARM_ACTION);
        PendingIntent alarmIntent = PendingIntent.getService(mContext, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE);
        if (alarmIntent != null && !isNeedUpdate) {
            //Log.d("CheckEmailAlarmUtils", "alarm not null");
            return null;
        }
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void createAlarm(PendingIntent alarmIntentUpdateCurrent) {
        long startTime = Calendar.getInstance().getTimeInMillis() + Constants.TEN_MINUTES;
        //Log.e(TAG,  "Start time " + new Date(startTime).toString());
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime,
                Constants.TEN_MINUTES, alarmIntentUpdateCurrent);
    }


    public void cancelAlarm() {
        alarmIntent = getCheckEmailServiceIntent(false);
        if (alarmIntent != null)
            alarmMgr.cancel(alarmIntent);
    }
}
