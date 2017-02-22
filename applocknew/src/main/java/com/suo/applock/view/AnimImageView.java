package com.suo.applock.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by song on 15/9/23.
 */
public class AnimImageView extends ImageView {
    Animation fadeIn;

    public AnimImageView(Context context) {
        super(context);
        loadAnimation();
    }

    public AnimImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAnimation();
    }

    public AnimImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAnimation();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        loadAnimation();
    }

    private void loadAnimation() {
        fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
    }

    public void setImageBitmap(Bitmap bm, boolean playAnimation) {
        super.setImageBitmap(bm);
        if (playAnimation) {
            startAnimation(fadeIn);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }
}
