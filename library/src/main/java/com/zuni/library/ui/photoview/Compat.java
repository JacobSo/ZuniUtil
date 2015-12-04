package com.zuni.library.ui.photoview;

/**
 * Created by Administrator on 2015/12/4.
 */

import android.os.Build;
import android.os.Build.VERSION;
import android.view.View;

public class Compat {
    private static final int SIXTY_FPS_INTERVAL = 16;

    public static void postOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= 16) {
            SDK16.postOnAnimation(view, runnable);
        } else {
            view.postDelayed(runnable, 16L);
        }
    }
}
