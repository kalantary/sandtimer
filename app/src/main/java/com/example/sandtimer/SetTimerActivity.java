package com.example.sandtimer;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResult;

import java.util.ArrayList;

public class SetTimerActivity extends AppCompatActivity {

    private TimerData current_timer;
    private ImageView image_view;
    private TimePicker timePicker;
    private TextView textViewDebug;
    ActivityResultLauncher get_image_launcher;
    Intent intent_image_chooser;
    private ArrayList<String> tempFilePaths = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);

        create_get_image_launcher();

        timePicker = findViewById(R.id.set_time_picker);
        timePicker.setIs24HourView(true);
        image_view = findViewById(R.id.image_view_set_timer_image);
        textViewDebug = findViewById(R.id.textViewSetActivityDebug);
        textViewDebug.setVisibility(View.GONE);

        if(savedInstanceState != null) {
            current_timer = savedInstanceState.getParcelable(KEY_CURRENT_TIMER);
            boolean hasSavedImage = savedInstanceState.getBoolean(KEY_HAS_SAVED_IMAGE);
            tempFilePaths = savedInstanceState.getStringArrayList(KEY_IMAGE_FILE_PATH);
            Bitmap image = null;
            if(hasSavedImage) {
                String filePath = tempFilePaths.get(tempFilePaths.size() - 1);
                image = Util.loadTemporaryImage(filePath);
                current_timer.setImage(image);
            }
            textViewDebug.setText("savedInstanceState is not null\n hasSavedImage = " + hasSavedImage
            +", image is "+(image == null?"":"NOT")+"null");
        }
        else
        {
            textViewDebug.setText("savedInstanceState is null");
            Intent intent = getIntent();
            current_timer = SetTimerActivityResultContract.fromExtras(intent);
            if(current_timer == null) current_timer = new TimerData();
        }

        current_timer.load_image(this);
        updateUI();
    }

    private static final String KEY_CURRENT_TIMER = "current_timer";
    private static final String MY_NAME = "sta";
    private static final String KEY_HAS_SAVED_IMAGE = "has_saved_image";
    private static final String KEY_IMAGE_FILE_PATH = "image_file_path";
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        saveSelectedTime();
        outState.putParcelable(KEY_CURRENT_TIMER, current_timer);
        boolean saveImage = current_timer != null && current_timer.hasUnsavedImage();
        if(saveImage) {
            String filePath = Util.saveTemporaryImage(this, MY_NAME, current_timer.getImage());
            tempFilePaths.add(filePath);
        }
        outState.putStringArrayList(KEY_IMAGE_FILE_PATH, tempFilePaths);
        outState.putBoolean(KEY_HAS_SAVED_IMAGE, saveImage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isFinishing())
            Util.deleteFiles(tempFilePaths);
    }

    private void create_get_image_launcher() {
        final Intent intent_pick = new Intent();
        intent_pick.setAction(Intent.ACTION_PICK);
        intent_pick.setType(getString(R.string.MIME_data_type_image));

        Intent intent_open_document = new Intent();
        intent_open_document.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent_open_document.setType(getString(R.string.MIME_data_type_image));

        Intent intent_capture = new Intent();
        intent_capture.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent[] array_intent = { intent_capture , intent_open_document};


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

                            // from gallery
                            if(intent.getData() != null)
                                imageUri = intent.getData();
                            // from camera
                            else if (intent.getExtras() != null)
                               bitmap = (Bitmap) intent.getParcelableExtra("data");

                            change_timer_image(imageUri, bitmap);
                        }
                    }
                });
    }

    private void change_timer_image(Uri imageUri, Bitmap bitmap)
    {
        if(bitmap == null && imageUri != null) {
            bitmap = Util.load_image(this, imageUri);
        }

        image_view.setImageBitmap(bitmap);
        current_timer.setImage(bitmap);
    }
    private void updateUI() {
        image_view.setImageBitmap(current_timer.getImage());
        setTimePickerTime();
    }
    private void setTimePickerTime() {
        int remaining_minutes = current_timer.get_remaining_minutes();
        int hour = remaining_minutes / 60;
        int minute = remaining_minutes % 60;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            timePicker.setHour(hour);
        else
            timePicker.setCurrentHour(hour);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(minute);
        }
        else
            timePicker.setCurrentMinute(minute);
    }
    private void saveSelectedTime(){
        int hour;
        int minute;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        }
        else
        {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }
        current_timer.resetTimer(hour, minute);
    }
    public void button_ok_onclick(android.view.View view) {
        saveSelectedTime();
        android.content.Intent intent = new android.content.Intent();

        current_timer.save_image(this);
        SetTimerActivityResultContract.putExtra(current_timer, intent);
        setResult(RESULT_OK, intent);

        finish();
    }
    public void button_cancel_onclick(android.view.View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void button_choose_image_onclick(View view)
    {
        get_image_launcher.launch(intent_image_chooser);
    }
}