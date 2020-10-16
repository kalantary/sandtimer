package com.example.sandtimer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SetTimerActivityResultContract
        extends androidx.activity.result.contract.ActivityResultContract<TimerData2, TimerData2> {
    public static final String EXTRA_TIMER_DATA_NULL_VALUE = "com.example.sandtimer.NULL_VALUE";
    public static final String EXTRA_TIMER_DATA_START_TIME = "com.example.sandtimer.START_TIME";
    public static final String EXTRA_TIMER_DATA_WAIT_DURATION = "com.example.sandtimer.WAIT_DURATION";
    public static final String EXTRA_TIMER_DATA_PAUSED_DURATION = "com.example.sandtimer.PAUSED_DURATION";
    public static final String EXTRA_TIMER_DATA_IMAGE = "com.example.sandtimer.IMAGE";
    public static final String EXTRA_TIMER_DATA_IMAGE_FILENAME = "com.example.sandtimer.IMAGE_FILENAME";
    public static final String EXTRA_TIMER_DATA_IS_PAUSED = "com.example.sandtimer.IS_PAUSED";
    public static final String EXTRA_TIMER_DATA_IS_STARTED = "com.example.sandtimer.IS_STARTED";
    public static final String EXTRA_TIMER_DATA_CODE = "com.example.sandtimer.IS_CODE";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, TimerData2 input) {
        android.content.Intent intent = new android.content.Intent(context, SetTimerActivity.class);
        putExtra(input, intent);
        return intent;
    }


    @Override
    public TimerData2 parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            return fromExtras(intent);
        }
        return null;
    }

    public static TimerData2 fromExtras(Intent intent) {
        try {
            boolean is_null_value = intent.getBooleanExtra(EXTRA_TIMER_DATA_NULL_VALUE, true);
            if(is_null_value) return null;

            long start_time = intent.getLongExtra(EXTRA_TIMER_DATA_START_TIME, System.currentTimeMillis());
            long wait_duration = intent.getLongExtra(EXTRA_TIMER_DATA_WAIT_DURATION, 0);
            long paused_duration = intent.getLongExtra(EXTRA_TIMER_DATA_PAUSED_DURATION,0);
            android.graphics.Bitmap image = intent.getParcelableExtra(EXTRA_TIMER_DATA_IMAGE);
            boolean is_paused = intent.getBooleanExtra(EXTRA_TIMER_DATA_IS_PAUSED, false);
            boolean is_started = intent.getBooleanExtra(EXTRA_TIMER_DATA_IS_STARTED, false);
            int code = intent.getIntExtra(EXTRA_TIMER_DATA_CODE, -1);
            String filename = intent.getStringExtra(EXTRA_TIMER_DATA_IMAGE_FILENAME);
            return new TimerData2(code, start_time, wait_duration, paused_duration, image, is_started, is_paused, filename);
        }
        catch(Exception e){
            return null;
        }
    }

    public static void putExtra(TimerData2 td, android.content.Intent intent)
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
