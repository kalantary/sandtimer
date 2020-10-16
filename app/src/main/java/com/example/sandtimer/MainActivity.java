package com.example.sandtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    private String code_digits;
    private String set_code;
    private String pause_code;
    private static final int code_length = 4;
    private static final int REQUEST_CODE_SET_TIMER = 1;

    private String set_code_partial = "";
    private String pause_code_partial = "";

    private TimerData2 current_timer;
    private androidx.activity.result.ActivityResultLauncher set_timer_launcher;
    private android.widget.TextView text_minutes;
    private android.widget.ProgressBar progressbar_minute;
    private android.widget.TextView text_pause_resume;
    ImageView image_waiting_for;

    private java.util.Timer timer = new java.util.Timer();

    public MainActivity() {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void create_random_codes(){
        java.util.ArrayList<String> digits = new java.util.ArrayList<String>();
        for(int d = 0; d < 10; d++)
            digits.add(String.valueOf(d));

        java.util.Collections.shuffle(digits);
        java.util.ArrayList<String> chosen_digits = new java.util.ArrayList<String>();
        for(int i = 0; i < code_length; i++)
            chosen_digits.add(digits.get(i));

        code_digits = "";
        for(int i = 0; i < code_length; i++)
            code_digits += chosen_digits.get(i);

        java.util.Collections.shuffle(chosen_digits);
        set_code = "";
        for(int i = 0; i < code_length; i++)
            set_code += chosen_digits.get(i);

        pause_code = set_code;
        while(pause_code.equals(set_code))
        {
            java.util.Collections.shuffle(chosen_digits);
            pause_code = "";
            for (int i = 0; i < code_length; i++)
                pause_code += chosen_digits.get(i);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        create_random_codes();

        setContentView(R.layout.activity_main);
        text_minutes = findViewById(R.id.text_minutes);
        progressbar_minute = findViewById(R.id.progressbar_minute);
        text_pause_resume = findViewById(R.id.text_pause);
        image_waiting_for = findViewById(R.id.image_waiting_for);

        set_code_texts();

        set_timer_launcher = registerForActivityResult(new SetTimerActivityResultContract(),
                new ActivityResultCallback<TimerData2>()
                {
                    @Override
                    public void onActivityResult(TimerData2 result) {
                        set_new_timer(result);
                    }
                });
    }
    private void set_code_texts() {
        int i = 0;
        ((android.widget.Button)findViewById(R.id.button_set_1)).setText(code_digits.substring(i++, i));
        ((android.widget.Button)findViewById(R.id.button_set_2)).setText(code_digits.substring(i++, i));
        ((android.widget.Button)findViewById(R.id.button_set_3)).setText(code_digits.substring(i++, i));
        ((android.widget.Button)findViewById(R.id.button_set_4)).setText(code_digits.substring(i++, i));
        ((android.widget.TextView) findViewById(R.id.text_set_code)).setText(set_code);
        ((android.widget.TextView) findViewById(R.id.text_pause_code)).setText(pause_code);
    }
    public void button_onclick(android.view.View view) {
        android.widget.Button b = (android.widget.Button) view;
        String digit = b.getText().toString();

        set_code_partial += digit;
        if (set_code_partial.equals(set_code)) {
            set_code_partial = "";
            pause_code_partial = "";
            launch_set_timer_activity();
            return;
        }
        if(!set_code_partial.equals(set_code.substring(0, set_code_partial.length())))
            set_code_partial = "";

        //((android.widget.TextView)findViewById(R.id.text_set)).setText(set_code_partial);

        pause_code_partial += digit;
        if (pause_code.equals(pause_code_partial)) {
            set_code_partial = "";
            pause_code_partial = "";
            pause_or_resume_timer();
            return;
        }
        if (!pause_code_partial.equals(pause_code.substring(0, pause_code_partial.length())))
            pause_code_partial = "";

        //((android.widget.TextView)findViewById(R.id.text_pause)).setText(pause_code_partial);

    }
    private void launch_set_timer_activity()
    {
        set_timer_launcher.launch(current_timer);
    }


    private void set_new_timer(TimerData2 td)
    {
        if(td == null) return;
        current_timer = td;
        current_timer.start();
        text_minutes.setText(String.format("%d",current_timer.get_remaining_minutes()));
        image_waiting_for.setImageBitmap(current_timer.getImage());
        progressbar_minute.setProgress(0);
        start_timer();
    }
    private void start_timer()
    {
        android.app.AlarmManager alarm_manager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
        android.content.Intent intent = new android.content.Intent(this, AlarmFinishedActivity.class);
        current_timer.save_image(this);
        SetTimerActivityResultContract.putExtra(current_timer, intent);
        PendingIntent pending_intent = android.app.PendingIntent.getActivity
                (this, current_timer.getCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager.setExact(android.app.AlarmManager.RTC_WAKEUP, current_timer.get_end_time(), pending_intent);
        start_ui_update_timer();
        text_pause_resume.setText(R.string.text_pause);
    }
    private void pause_or_resume_timer()
    {
        if(text_pause_resume.getText().equals(getString(R.string.text_pause)))
            pause_timer();
        else
            resume_timer();
    }
    private void pause_timer() {
        text_pause_resume.setText(R.string.text_resume);

        timer.cancel();
        timer.purge();

        current_timer.pause();

        //todo: cancel or pause alarm in system
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, current_timer.getCode(),
                        new Intent(this, AlarmFinishedActivity.class),
                                        PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null && alarmManager != null)
          alarmManager.cancel(pendingIntent);
    }
    private void resume_timer()
    {
        text_pause_resume.setText(R.string.text_pause);
        current_timer.resume();
        start_ui_update_timer();

        Intent intent = new Intent(this, AlarmFinishedActivity.class);
        SetTimerActivityResultContract.putExtra(current_timer, intent);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending_intent = PendingIntent.getActivity(this, current_timer.getCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, current_timer.get_end_time(), pending_intent);
    }

    private void start_ui_update_timer()
    {
        timer = new Timer();
        timer.schedule(new java.util.TimerTask()
        {
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                         long this_minute_progress = current_timer.get_remaining_seconds() % 60;
                        progressbar_minute.setProgress((int)this_minute_progress);
                        long remaining_minutes = current_timer.get_remaining_minutes();
                        if(this_minute_progress > 0) remaining_minutes++;
                        text_minutes.setText(String.format("%d", remaining_minutes));
                        if(remaining_minutes == 0)
                        {
                            timer.cancel();
                            timer.purge();
                        }
                    }
                });
            }
        }, 0, 500);
    }
}