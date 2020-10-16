package com.example.sandtimer;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class SetTimerActivity extends AppCompatActivity {

    private TimerData2 input_timer;
    ImageView image_view;
    private android.widget.TimePicker tp;
    ActivityResultLauncher get_image_launcher;
    Intent intent_image_chooser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);

        create_get_image_launcher();


        tp = findViewById(R.id.set_time_picker);
        tp.setIs24HourView(true);

        image_view = findViewById(R.id.image_view_set_timer_image);


        android.content.Intent intent = getIntent();
        input_timer = SetTimerActivityResultContract.fromExtras(intent);
        input_timer.load_image(this);

        show_current_timer();


    }
    private void create_get_image_launcher() {
        final Intent intent_pick = new Intent();
        intent_pick.setAction(Intent.ACTION_PICK);
        intent_pick.setType(getString(R.string.intent_type_image));
        //intent_pick.addCategory(Intent.CATEGORY_OPENABLE);
        //intent_pick.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        Intent intent_capture = new Intent();
        intent_capture.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent[] array_intent = { intent_capture};


        intent_image_chooser = Intent.createChooser(intent_pick, getString(R.string.pick_image));
        intent_image_chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, array_intent);
        get_image_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK) {

                            Intent intent = result.getData();
                            Bitmap bitmap = null;
                            Uri imageUri = null;

                            // from camera
                            if(intent.getExtras() != null)
                            {
                               bitmap = (Bitmap) intent.getParcelableExtra("data");
                               set_image_from_bitmap(bitmap);
                            }
                            // from gallery
                            else
                            {
                                imageUri = intent.getData();
                                set_image_from_Media_store(imageUri);

                            }
                            change_timer_image(imageUri, bitmap);
                        }
                    }
                });
    }

    private void change_timer_image(Uri imageUri, Bitmap bitmap)
    {
        try {

            bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        image_view.setImageBitmap(bitmap);
        image_view.setTag(bitmap);
    }
    private void show_current_timer() {
        int hour = 0;
        int minutes = 0;
        if(input_timer != null) {
            image_view.setImageBitmap(input_timer.getImage());
            image_view.setTag(input_timer.getImage());

            int remaining_minutes = input_timer.get_remaining_minutes();
             hour = remaining_minutes / 60;
             minutes = remaining_minutes % 60;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tp.setHour(hour);
        else
            tp.setCurrentHour(hour);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tp.setMinute(minutes);
        }
        else
            tp.setCurrentMinute(minutes);
    }

    public void button_ok_onclick(android.view.View view)
    {
        int hour;
        int minute;
        Bitmap image = (Bitmap)image_view.getTag();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = tp.getHour();
            minute = tp.getMinute();
        }
        else
        {
            hour = tp.getCurrentHour();
            minute = tp.getCurrentMinute();
        }

        android.content.Intent intent = new android.content.Intent();
        TimerData2 td = TimerData2.fromTimerData(input_timer, hour, minute, image);

        SetTimerActivityResultContract.putExtra(td, intent);
        setResult(RESULT_OK, intent);

        finish();
    }
    public void button_cancel_onclick(android.view.View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void button_choose_image_onclick(View view)
    {
        get_image_launcher.launch(intent_image_chooser);
    }
}