package org.pl.android.drively.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorageManager {

    public static String IMAGES_DICTIONARY = "IMAGES";

    public static Uri saveBitmap(String FILENAME, Bitmap bitmap, Context context) throws IOException {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(IMAGES_DICTIONARY, Context.MODE_PRIVATE);
        File file = new File(directory, FILENAME);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } finally {
            if (fos != null)
                fos.close();
        }

        return Uri.fromFile(file);
    }
}
