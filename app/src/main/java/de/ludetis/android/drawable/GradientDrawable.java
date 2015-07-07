package de.ludetis.android.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Created by uwe on 10.09.14.
 */
public class GradientDrawable extends Drawable {
    @Override
    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setShader(new LinearGradient(0, 0, 0, getIntrinsicHeight(), Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));
        canvas.drawPaint(p);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
