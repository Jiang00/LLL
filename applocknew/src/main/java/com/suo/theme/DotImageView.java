package com.suo.theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.lockscreen.api.liberal.Utils;

/**
 * Created by SongHualin on 4/9/2015.
 */
public class DotImageView extends ImageView {
    boolean hasRedDot = true;

    Paint dotPaint = new Paint();

    public static int RADIUS;

    public void showRedDot(boolean yes) {
        hasRedDot = yes;
        invalidate();
    }

    public boolean hasRedDot() {
        return hasRedDot;
    }

    public DotImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        RADIUS = Utils.getDimens(context, 3);
        dotPaint.setARGB(255, 255, 0, 0);
        setScaleType(ScaleType.FIT_CENTER);
    }

    public DotImageView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (hasRedDot) {
            float cx = getWidth() - getPaddingRight();
            float cy = RADIUS + getPaddingTop();
            canvas.drawCircle(cx, cy, RADIUS, dotPaint);
        }
    }
}