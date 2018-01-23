package org.pl.android.drively.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class InternalStorageManager {

    public static String IMAGES_DICTIONARY = "IMAGES";

    public static Uri saveBitmap(String FILENAME, Bitmap bitmap, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(IMAGES_DICTIONARY, Context.MODE_PRIVATE);
        File file = new File(directory, FILENAME);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Uri.fromFile(file);
    }
}
