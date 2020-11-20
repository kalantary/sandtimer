package com.example.sandtimer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class TimerData implements Parcelable {


    private long start_time_milliseconds;
    public long getStart_time_milliseconds(){ return start_time_milliseconds; }

    private long wait_duration_milliseconds;
    public long getWait_duration_milliseconds(){ return wait_duration_milliseconds; }
    public int getWait_duration_seconds(){ return (int)(wait_duration_milliseconds / 1000); }

    private long paused_duration_milliseconds;
    public long getPaused_duration_milliseconds(){ return paused_duration_milliseconds; }

    private Uri image_file_path = null;
    public Uri getImage_file_path() { return image_file_path; }

    private Bitmap image;
    public Bitmap getImage() { return image; }
    public void setImage(Bitmap image) {
        this.image = image;
        image_file_path = null;
    }

    private boolean is_paused = false;
    public boolean getIs_paused() { return is_paused; }

    private final int code;
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
    public int get_remaining_seconds() {
        long remaining_milliseconds = get_remaining_milliseconds();
        return (int)(remaining_milliseconds / 1000);
    }
    public int get_remaining_minutes() {
        long remaining_milliseconds = get_remaining_milliseconds();
        return get_minutes_in_milliseconds(remaining_milliseconds);
    }

    private int get_minutes_in_milliseconds(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        return (int)minutes;
    }

    public boolean start() {
        if(is_started)
            return resume();
        start_time_milliseconds = System.currentTimeMillis();
        is_started = true;
        return true;
    }

    private long pause_milliseconds;
    public boolean pause() {
        if(is_paused) return false;
        is_paused = true;
        pause_milliseconds = System.currentTimeMillis();
        return true;
    }
    public boolean resume() {
        if(!is_started) return start();
        if(!is_paused) return false;
        paused_duration_milliseconds += System.currentTimeMillis() - pause_milliseconds;
        is_paused = false;
        return true;
    }
    public boolean isFinished(){
        return is_started && !is_paused && get_remaining_milliseconds() <= 0;
    }
    public TimerData()
    {
        this(generate_code(), -1, 0, 0, null, false, false, null);
    }
    public TimerData(int code, long start_time_milliseconds, long wait_duration_milliseconds,
                     long paused_duration_milliseconds, android.graphics.Bitmap image,
                     boolean is_started, boolean is_paused, Uri image_file_path) {
        this.is_started = is_started;
        this.is_paused = is_paused;
        this.start_time_milliseconds = start_time_milliseconds;
        this.wait_duration_milliseconds = wait_duration_milliseconds;
        this.paused_duration_milliseconds = paused_duration_milliseconds;
        this.image = image;
        this.code = code;
        this.image_file_path = image_file_path;
    }

    private static int generate_code() {
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

    public void load_image(Context context) {
        if(image != null) return;
        if(image_file_path != null)
            image = Util.load_image(context, image_file_path);
    }

    public Uri save_image(Context context) {
        if(image_file_path != null) return image_file_path;
        if(image == null) return null;
        return image_file_path = Util.save_image(context, image);
    }

    public void compress_image() {
        if(image == null) return;
        int width = 60;
        int height = image.getHeight() * width / image.getWidth();
        image = Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void resetTimer(int hour, int minute) {
        long wait_duration_milliseconds = hour * 50 + minute;
        wait_duration_milliseconds *= 60000;
        this.wait_duration_milliseconds = wait_duration_milliseconds;

        start_time_milliseconds=-1;
        paused_duration_milliseconds = 0;
        is_started=false;
        is_paused=false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(start_time_milliseconds);
        dest.writeLong(wait_duration_milliseconds);
        dest.writeLong(paused_duration_milliseconds);
        dest.writeParcelable(image_file_path, flags);
        if(image_file_path == null)
        {
            compress_image();
            dest.writeParcelable(image, flags);
        }
        else
            dest.writeParcelable(null, flags);
        dest.writeByte((byte) (is_paused ? 1 : 0));
        dest.writeInt(code);
        dest.writeByte((byte) (is_started ? 1 : 0));
        dest.writeLong(pause_milliseconds);
    }
    protected TimerData(Parcel in) {
        start_time_milliseconds = in.readLong();
        wait_duration_milliseconds = in.readLong();
        paused_duration_milliseconds = in.readLong();
        image_file_path = in.readParcelable(Uri.class.getClassLoader());
        image = in.readParcelable(Bitmap.class.getClassLoader());
        is_paused = in.readByte() != 0;
        code = in.readInt();
        is_started = in.readByte() != 0;
        pause_milliseconds = in.readLong();
    }

    public static final Creator<TimerData> CREATOR = new Creator<TimerData>() {
        @Override
        public TimerData createFromParcel(Parcel in) {
            return new TimerData(in);
        }

        @Override
        public TimerData[] newArray(int size) {
            return new TimerData[size];
        }
    };

    public boolean hasUnsavedImage() {
        return image != null && image_file_path == null;
    }

    public int getElapsedSeconds() {
        return getWait_duration_seconds() - get_remaining_seconds();
    }
}
