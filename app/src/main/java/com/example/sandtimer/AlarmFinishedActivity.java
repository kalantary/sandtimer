package com.example.sandtimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AlarmFinishedActivity extends AppCompatActivity {

    TimerData2 td;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_finished);

        android.content.Intent intent = getIntent();
        td = SetTimerActivityResultContract.fromExtras(intent);

        ((android.widget.ImageView)findViewById(R.id.image_finished_waiting_for)).setImageBitmap(td.getImage());
    }

    public void button_ok_click(android.view.View view)
    {
        finish();
    }
}