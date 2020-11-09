package com.example.sandtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private static final int code_length = 4;
    private  TimerDataViewModel model;

    private androidx.activity.result.ActivityResultLauncher set_timer_launcher;
    private TextView textViewRemainingMinutes;
    private ProgressBar progressbar_minute;
    private TextView text_pause_resume;
    private ImageView imageViewWaitingFor;
    private TextView textSetCode;
    private TextView textPauseCode;
    private java.util.Timer timer_ui_update = new java.util.Timer();
    TextView textViewDebug;

    public MainActivity() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isFinishing())
            cancelSystemAlarm();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        model.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        start_ui_update_timer();
    }

    private void setRandomCodesForButtons(){
        String code_digits = model.getCodeDigits();
        int i = 0;
        ((android.widget.Button)findViewById(R.id.button_set_1)).setText(code_digits.substring(i++, i));
        ((android.widget.Button)findViewById(R.id.button_set_2)).setText(code_digits.substring(i++, i));
        ((android.widget.Button)findViewById(R.id.button_set_3)).setText(code_digits.substring(i++, i));
        ((android.widget.Button)findViewById(R.id.button_set_4)).setText(code_digits.substring(i++, i));
        textSetCode.setText(model.getSetCode());
        textPauseCode.setText(model.getPauseCode());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewRemainingMinutes = findViewById(R.id.text_minutes);
        progressbar_minute = findViewById(R.id.progressbar_minute);
        text_pause_resume = findViewById(R.id.text_pause);
        imageViewWaitingFor = findViewById(R.id.image_waiting_for);
        textSetCode = findViewById(R.id.text_set_code);
        textPauseCode = findViewById(R.id.text_pause_code);
        textViewDebug = findViewById(R.id.textViewDebug);

        textViewDebug.setVisibility(View.GONE);

        model = new ViewModelProvider(this, TimerDataViewModel.getFactory(code_length)).get(TimerDataViewModel.class);
        if(savedInstanceState != null) {
            model.loadInstanceState(savedInstanceState);
            textViewDebug.setText("saved code digits " + model.getCodeDigits());
        }
        else
            textViewDebug.setText("saved instance is null");

        setRandomCodesForButtons();

        set_timer_launcher = registerForActivityResult(new SetTimerActivityResultContract(),
                new ActivityResultCallback<TimerData>()
                {
                    @Override
                    public void onActivityResult(TimerData result) {
                        if(result == null) return;
                        model.setTimerData(result);
                        start_timer();
                    }
                });
    }
    private void start_timer() {
        TimerData current_timer = model.getTimerData();
        current_timer.start();
        setSystemAlarm();
        start_ui_update_timer();
    }
    private void setSystemAlarm() {
        TimerData current_timer = model.getTimerData();
        AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmFinishedActivity.class);
        SetTimerActivityResultContract.putExtra(current_timer, intent);
        PendingIntent pending_intent = android.app.PendingIntent.getActivity
                (this, current_timer.getCode(),
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager.setExact(AlarmManager.RTC_WAKEUP, current_timer.get_end_time(), pending_intent);
    }
    private void cancelSystemAlarm() {
        TimerData current_timer = model.getTimerData();
        if(current_timer == null) return;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, current_timer.getCode(),
                        new Intent(this, AlarmFinishedActivity.class),
                        PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null && alarmManager != null)
            alarmManager.cancel(pendingIntent);
    }
    private void start_ui_update_timer() {
        timer_ui_update = new Timer();
        timer_ui_update.schedule(new java.util.TimerTask()
        {
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        update_ui();
                    }
                });
            }
        }, 0, 500);
    }
    public void button_onclick(android.view.View view) {
        android.widget.Button b = (android.widget.Button) view;
        String digit = b.getText().toString();

        String match = model.match_code_digit(digit);
        if(match.equals(model.getSetCode()))
            set_timer_launcher.launch(model.getTimerData());
        else if (match.equals(model.getPauseCode()))
            pause_or_resume_timer();
    }
    private void pause_or_resume_timer() {
        TimerData current_timer = model.getTimerData();
        if(current_timer == null || current_timer.isFinished()) return;

        if(current_timer.getIs_paused()) {
            current_timer.resume();
            setSystemAlarm();
            start_ui_update_timer();
        }
        else {
            current_timer.pause();
            cancelSystemAlarm();
        }
    }
    private void update_ui() {
        TimerData current_timer = model.getTimerData();
        if(current_timer == null)
        {
            progressbar_minute.setProgress(0);
            textViewRemainingMinutes.setText(getString(R.string.timer_not_set));
            imageViewWaitingFor.setImageBitmap(null);
            text_pause_resume.setText(R.string.textViewPause);
        }
        else {
            current_timer.load_image(this);

            long this_minute_progress = current_timer.get_remaining_seconds() % 60;
            progressbar_minute.setProgress((int) this_minute_progress);

            long remaining_minutes = Math.max(0, current_timer.get_remaining_minutes());
            if (this_minute_progress > 0) remaining_minutes++;
            textViewRemainingMinutes.setText(String.format("%d", remaining_minutes));
            imageViewWaitingFor.setImageBitmap(current_timer.getImage());
            text_pause_resume.setText(current_timer.getIs_paused() ? R.string.text_resume : R.string.textViewPause);

            if (remaining_minutes == 0 || current_timer.getIs_paused()) {
                timer_ui_update.cancel();
                timer_ui_update.purge();
            }
        }

        int setCodeMatchLength = model.getSetCodeMatchLength();
        Spannable spannableSetCode = new SpannableString(model.getSetCode());
        spannableSetCode.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, setCodeMatchLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textSetCode.setText(spannableSetCode);

        int pauseCodeMatchLength = model.getPauseCodeMatchLength();
        Spannable spannablePauseCode = new SpannableString(model.getPauseCode());
        spannablePauseCode.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, pauseCodeMatchLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textPauseCode.setText(spannablePauseCode);
    }
}






