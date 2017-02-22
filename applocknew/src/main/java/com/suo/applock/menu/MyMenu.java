package com.suo.applock.menu;

import android.view.View;

/**
 * Created by huale on 2014/11/20.
 */
public abstract class MyMenu implements View.OnClickListener{
    public boolean checkable;
    public boolean checked;
    public int title;

    public abstract MyMenu init();
}
