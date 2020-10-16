package com.example.sandtimer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaTimestamp;
import android.os.SystemClock;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

public class TimerData2 {

    public static TimerData2 fromTimerData(TimerData2 input_timer, int hour, int minute, Bitmap image)
    {
        long wait_duration_milliseconds = hour * 50 + minute;
        wait_duration_milliseconds *= 60000;
        TimerData2 res = new TimerData2(input_timer == null ? generate_code() : input_timer.code,
                -1, wait_duration_milliseconds, 0,
                image, false, false);
        return res;
    }

    private long start_time_milliseconds;
    public long getStart_time_milliseconds(){ return start_time_milliseconds; }
    private long wait_duration_milliseconds;
    public long getWait_duration_milliseconds(){ return wait_duration_milliseconds; }
    private long paused_duration_milliseconds;
    public long getPaused_duration_milliseconds(){ return paused_duration_milliseconds; }
    private String image_file_path = null;
    public String getImage_file_path()
    {
        return image_file_path;
    }



    private Bitmap image;
    public Bitmap getImage() { return image; }

    private boolean is_paused = false;
    public boolean getIs_paused() { return is_paused; }

    private int code;
    public int getCode(){ return code; }

    private boolean is_started = false;
    public boolean getIs_started() { return is_started; }

    public long get_end_time() {
        //if(!is_started || is_paused) throw new Exception("attempting to get the end time for a timer that is not running");
        return System.currentTimeMillis() + get_remaining_milliseconds();
    }
    public long get_remaining_milliseconds() {
        if(!is_started) return wait_duration_milliseconds;
        long until = is_paused ? pause_milliseconds : System.currentTimeMillis();
        long elapsed_milliseconds = until - start_time_milliseconds;
        long true_elapsed_milliseconds = elapsed_milliseconds - paused_duration_milliseconds;
        long remaining_milliseconds = wait_duration_milliseconds - true_elapsed_milliseconds;
        return remaining_milliseconds;
    }
    public int get_remaining_seconds()
    {
        long remaining_milliseconds = get_remaining_milliseconds();
        return (int)(remaining_milliseconds / 1000);
    }

    public int get_remaining_minutes()
    {
        long remaining_milliseconds = get_remaining_milliseconds();
        return get_minutes_in_milliseconds(remaining_milliseconds);
    }

    private int get_minutes_in_milliseconds(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        return (int)minutes;
    }

    public boolean start()
    {
        if(is_started)
            return resume();
        start_time_milliseconds = System.currentTimeMillis();
        is_started = true;
        return true;
    }

    private long pause_milliseconds;
    public boolean pause()
    {
        if(is_paused) return false;
        is_paused = true;
        pause_milliseconds = System.currentTimeMillis();
        return true;
    }
    public boolean resume()
    {
        if(!is_started) return start();
        if(!is_paused) return false;
        paused_duration_milliseconds += System.currentTimeMillis() - pause_milliseconds;
        is_paused = false;
        return true;
    }

    public TimerData2(int code, long start_time_milliseconds, long wait_duration_milliseconds,
                      long paused_duration_milliseconds, android.graphics.Bitmap image,
                      boolean is_started, boolean is_paused, String image_file_path)
    {
        this.is_started = is_started;
        this.is_paused = is_paused;
        this.start_time_milliseconds = start_time_milliseconds;
        this.wait_duration_milliseconds = wait_duration_milliseconds;
        this.paused_duration_milliseconds = paused_duration_milliseconds;
        this.image = image;
        this.code = code;
        this.image_file_path = image_file_path;
    }

    private static int generate_code()
    {
        long create_time_milliseconds = System.currentTimeMillis();
        long milliseconds = create_time_milliseconds % 1000;
        long seconds = create_time_milliseconds / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        long hours = minutes / 60;
        minutes %= 60;
        hours %= 24;
        return (int)(hours*10000000 + minutes*100000 + seconds*1000+milliseconds);
    }

    public static TimerData2 get_example_value() {
        long wait_duration = (1*60*60 + 45*60) * 1000;
        TimerData2 td = new TimerData2(generate_code(),-1,
                wait_duration,0,null,false, false, null);
        td.image = Bitmap.createBitmap(200,200, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(td.image);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(Color.BLUE);
        canvas.drawCircle(100,100,50,paint);
        return td;
    }

    public void load_image(Context context) {
        if(image != null) return;
        if(image_file_path != null)
            image = Util.load_image(context, image_file_path);
    }

    public String save_image(Context context)
    {
        if(image == null) return null;
        return Util.save_image(context, image);
    }

    public void compress_image() {

    }
}
