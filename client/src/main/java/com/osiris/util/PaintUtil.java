package com.osiris.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.osiris.MainActivity;

public class PaintUtil {

    private static Canvas canvas;
    private static Paint paint;

    public static Bitmap drawTextToBitmap(Bitmap bitmap, Color color, String text, int x, int y) {
        if (canvas == null)
            canvas = new Canvas(bitmap);
        // new antialised Paint
        if (paint == null)
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(color.toArgb());
        // text size in pixels
        paint.setTextSize(14 * 4);

        paint.setTypeface(MainActivity.rsRegular);
        // text shadow
        paint.setShadowLayer(5f, 5f, 5f, Color.BLACK);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text, x - (float)bounds.width() / 2, -bounds.top + y, paint);

        return bitmap;
    }
}
