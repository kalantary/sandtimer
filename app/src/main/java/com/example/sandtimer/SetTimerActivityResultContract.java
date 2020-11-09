package com.example.sandtimer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetTimerActivityResultContract
        extends androidx.activity.result.contract.ActivityResultContract<TimerData, TimerData> {
    public static final String EXTRA_TIMER_DATA_NULL_VALUE = "com.example.sandtimer.NULL_VALUE";
    public static final String EXTRA_TIMER_DATA_START_TIME = "com.example.sandtimer.START_TIME";
    public static final String EXTRA_TIMER_DATA_WAIT_DURATION = "com.example.sandtimer.WAIT_DURATION";
    public static final String EXTRA_TIMER_DATA_PAUSED_DURATION = "com.example.sandtimer.PAUSED_DURATION";
    public static final String EXTRA_TIMER_DATA_IMAGE = "com.example.sandtimer.IMAGE";
    public static final String EXTRA_TIMER_DATA_IMAGE_FILENAME = "com.example.sandtimer.IMAGE_FILENAME";
    public static final String EXTRA_TIMER_DATA_IS_PAUSED = "com.example.sandtimer.IS_PAUSED";
    public static final String EXTRA_TIMER_DATA_IS_STARTED = "com.example.sandtimer.IS_STARTED";
    public static final String EXTRA_TIMER_DATA_CODE = "com.example.sandtimer.CODE";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, TimerData input) {
        android.content.Intent intent = new android.content.Intent(context, SetTimerActivity.class);
        putExtra(input, intent);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        return intent;
    }

    @Override
    public TimerData parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            return fromExtras(intent);
        }
        return null;
    }

    public static TimerData fromExtras(Intent intent) {
        try {
            boolean is_null_value = intent.getBooleanExtra(EXTRA_TIMER_DATA_NULL_VALUE, true);
            if(is_null_value) return null;

            long start_time = intent.getLongExtra(EXTRA_TIMER_DATA_START_TIME, System.currentTimeMillis());
            long wait_duration = intent.getLongExtra(EXTRA_TIMER_DATA_WAIT_DURATION, 0);
            long paused_duration = intent.getLongExtra(EXTRA_TIMER_DATA_PAUSED_DURATION,0);
            Bitmap image = intent.getParcelableExtra(EXTRA_TIMER_DATA_IMAGE);
            boolean is_paused = intent.getBooleanExtra(EXTRA_TIMER_DATA_IS_PAUSED, false);
            boolean is_started = intent.getBooleanExtra(EXTRA_TIMER_DATA_IS_STARTED, false);
            int code = intent.getIntExtra(EXTRA_TIMER_DATA_CODE, -1);
            Uri filename = intent.getParcelableExtra(EXTRA_TIMER_DATA_IMAGE_FILENAME);
            return new TimerData(code, start_time, wait_duration, paused_duration, image, is_started, is_paused, filename);
        }
        catch(Exception e){
            return null;
        }
    }
    public static void putExtra(TimerData td, android.content.Intent intent)
    {
        if(td == null) {
            intent.putExtra(EXTRA_TIMER_DATA_NULL_VALUE, true);
            intent.removeExtra(EXTRA_TIMER_DATA_START_TIME);
            intent.removeExtra(EXTRA_TIMER_DATA_WAIT_DURATION);
            intent.removeExtra(EXTRA_TIMER_DATA_PAUSED_DURATION);
            intent.removeExtra(EXTRA_TIMER_DATA_IMAGE);
            intent.removeExtra(EXTRA_TIMER_DATA_CODE);
            intent.removeExtra(EXTRA_TIMER_DATA_IS_STARTED);
            intent.removeExtra(EXTRA_TIMER_DATA_IS_PAUSED);
            intent.removeExtra(EXTRA_TIMER_DATA_IMAGE_FILENAME);
        }
        else {
            intent.putExtra(EXTRA_TIMER_DATA_NULL_VALUE, false);
            intent.putExtra(EXTRA_TIMER_DATA_START_TIME, td.getStart_time_milliseconds());
            intent.putExtra(EXTRA_TIMER_DATA_WAIT_DURATION, td.getWait_duration_milliseconds());
            intent.putExtra(EXTRA_TIMER_DATA_PAUSED_DURATION, td.getPaused_duration_milliseconds());
            if(td.getImage_file_path() == null) {
                td.compress_image();
                intent.putExtra(EXTRA_TIMER_DATA_IMAGE, td.getImage());
            }
            intent.putExtra(EXTRA_TIMER_DATA_IMAGE_FILENAME, td.getImage_file_path());
            intent.putExtra(EXTRA_TIMER_DATA_CODE, td.getCode());
            intent.putExtra(EXTRA_TIMER_DATA_IS_PAUSED, td.getIs_paused());
            intent.putExtra(EXTRA_TIMER_DATA_IS_STARTED, td.getIs_started());
        }
    }
}
