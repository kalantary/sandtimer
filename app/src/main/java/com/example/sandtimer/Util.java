package com.example.sandtimer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaTimestamp;
import android.os.Environment;
import android.view.contentcapture.ContentCaptureCondition;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {
    public static Bitmap get_small_image(Bitmap large_bitmap)
    {
        return large_bitmap;
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

    //get nested directories
//        File directory = context.getFilesDir();
//        File file = new File(directory, filename);

    //create cache file
    //File.createTempFile(filename, null, context.getCacheDir());
    //File cacheFile = new File(context.getCacheDir(), filename);
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
        return String.format("yyyyMMdd-HHmmss",new java.util.Date());
    }
        @Nullable
    public static File get_image_storage_directory(Context context) {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        File file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (file == null || !file.mkdirs()) {
          //  Log.e(LOG_TAG, "Directory not created");
            return null;
        }
        return file;
    }

    public static Bitmap load_image(Context context, String filename)
    {
        try{
            File file = new File(filename);
            FileInputStream istream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(istream);
            return bitmap;
        }
        catch (Exception e)
        {
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
    public static String save_image(Context context, Bitmap bitmap) {
        try{
            File file = new File(get_image_storage_directory(context), get_timestamp_filename());
            FileOutputStream ostream = new FileOutputStream (file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
            //            String filename = "bitmap.png";
//            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//            //Cleanup
//            stream.close();
//            bmp.recycle();

//            String filename = getIntent().getStringExtra("image");
//            try {
//                FileInputStream is = this.openFileInput(filename);
//                bmp = BitmapFactory.decodeStream(is);
//                is.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return file.getPath();
        }
        catch (Exception e)
        {
        e.printStackTrace();
        return null;
        }
    }
}
