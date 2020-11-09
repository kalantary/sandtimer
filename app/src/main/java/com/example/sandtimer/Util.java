package com.example.sandtimer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaTimestamp;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.view.contentcapture.ContentCaptureCondition;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Util {
    public static Bitmap loadTemporaryImage(String filePath) {
        try {
            File cacheFile = new File(filePath);
            if(!cacheFile.exists()) return null;
            FileInputStream is = new FileInputStream(cacheFile);
            Bitmap image = BitmapFactory.decodeStream(is);
            is.close();
            return image;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public static String saveTemporaryImage(Context context, String requestingActivity, Bitmap image) {
        try {
            File file = File.createTempFile(requestingActivity, null, context.getCacheDir());
            if(image == null && file.exists())
                file.delete();
            else {
                save_image(file, image);
                return file.getPath();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteFiles(ArrayList<String> filesToDelete) {
        for (String filePath : filesToDelete) {
            File file = new File(filePath);
            file.delete();
        }
    }
    //java.io.File persistent_files_internal_storage = this.getFilesDir();
//    String filename = "test.txt";
//    File file = new File(getFilesDir(), filename);
//
//    //FileOutputStream writes to a file within the filesDir directory
//        try {
//        FileOutputStream ostream = openFileOutput(filename, Context.MODE_PRIVATE);
//    } catch (
//    FileNotFoundException e) {
//        e.printStackTrace();
//    }

    // Checks if a volume containing external storage is available
    // for read and write.
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
        //Environment.MEDIA_MOUNTED_READ_ONLY

        // in case of multiple external storage dirs, the [0] is the primary one
//        File[] externalStorageVolumes =
//                ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
//        File primaryExternalStorage = externalStorageVolumes[0];

        //File appSpecificExternalDir = new File(context.getExternalFilesDir(), filename);
        //File externalCacheFile = new File(context.getExternalCacheDir(), filename);
    }

    public static String get_timestamp_filename(){
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return f.format(new java.util.Date());
    }
        @Nullable
    public static File get_image_storage_directory(Context context) {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        File file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file == null) {
          //  Log.e(LOG_TAG, "Directory not created");
            return null;
        }
            file.mkdirs();

            return file;
    }

    public static Bitmap load_image(Context context, Uri uri_image) {
//            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
//                    != PERMISSION_GRANTED) ;

        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri_image, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();

            return image;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
//    private void read_file(String filename){
//        FileInputStream fis = openFileInput(filename);
//        InputStreamReader inputStreamReader =
//                new InputStreamReader(fis, StandardCharsets.UTF_8);
//        StringBuilder stringBuilder = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
//            String line = reader.readLine();
//            while (line != null) {
//                stringBuilder.append(line).append('\n');
//                line = reader.readLine();
//            }
//        } catch (IOException e) {
//            // Error occurred when opening raw file for reading.
//        } finally {
//            String contents = stringBuilder.toString();
//        }
//    }
    public static Uri save_image(Context context, Bitmap bitmap) {
    File file = new File(get_image_storage_directory(context), get_timestamp_filename());
    return save_image(file, bitmap);
}
    public static Uri save_image(File file, Bitmap bitmap) {
        try{
            FileOutputStream ostream = new FileOutputStream (file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
            return Uri.fromFile(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
