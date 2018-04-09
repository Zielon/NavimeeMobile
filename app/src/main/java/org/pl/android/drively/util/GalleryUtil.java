package org.pl.android.drively.util;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import com.github.oliveiradev.image_zoom.lib.widget.ZoomAnimation;

import org.pl.android.drively.R;

import timber.log.Timber;

public class GalleryUtil {

    public static void startPickingPhoto(Activity activity, final int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(
                Intent.createChooser(intent, activity.getString(R.string.select_picture)), requestCode);
    }

    public static boolean checkSize(Activity activity, Uri returnUri, long size) {
        Cursor returnCursor =
                activity.getContentResolver().query(returnUri, null, null, null, null);

        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        if (returnCursor.getLong(sizeIndex) > size) {
            Toast.makeText(activity.getBaseContext(), "The file is too big!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static void zoomInImageView(Activity activity, View view, Drawable drawable) {
        try {
            ZoomAnimation.zoom(view, drawable, activity, false);
        } catch (ClassCastException e) {
            Timber.d(e);
        }
    }

}
