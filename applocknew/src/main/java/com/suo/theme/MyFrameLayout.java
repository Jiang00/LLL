package com.suo.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.suo.applock.view.Flow;

/**
 * Created by huale on 2015/2/3.
 */
public class MyFrameLayout extends FrameLayout {
    public MyFrameLayout(Context context) {
        super(context);
    }

    public MyFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    OnClickListener listener;
    Flow ctrl;

    public void setOnBackListener(OnClickListener listener){
        this.listener = listener;
    }

    public void setOverflowCtrl(Flow ctrl){
        this.ctrl = ctrl;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_UP){
                    if (this.listener != null){
                        this.listener.onClick(null);
                    }
                }
                return true;

            case KeyEvent.KEYCODE_MENU:
                if (event.getAction() == KeyEvent.ACTION_UP){
                    if (ctrl != null)
                        ctrl.pressOverflowMenu();
                }
                return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
