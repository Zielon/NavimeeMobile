package org.pl.android.drively.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    public static Bitmap getCircular(Bitmap bitmap, int width, int height) {
        if (bitmap == null) return null;
        final int targetWidth = width;
        final int targetHeight = height;
        final int color = 0xff424242;

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);

        canvas.clipPath(path);
        canvas.drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(),
                        bitmap.getHeight()),
                new Rect(0, 0, targetWidth,
                        targetHeight), paint);
        return targetBitmap;
    }

    public static byte[] parseBitmapIntoBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }


    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, int quality) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap.createScaledBitmap(realImage, width, height, true).compress(Bitmap.CompressFormat.PNG, quality, out);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }
}
