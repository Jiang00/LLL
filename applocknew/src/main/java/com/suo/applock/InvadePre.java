package com.suo.applock;

import android.content.Intent;

/**
 * Created by song on 15/8/18.
 */
public class InvadePre {
    public static void show() {
        Application.getContext().startActivity(new Intent(Application.getContext(), InvadeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
