package com.zuni.library.ui.photoview;

/**
 * Created by Administrator on 2015/12/4.
 */

import android.annotation.TargetApi;
import android.view.View;

@TargetApi(16)
public class SDK16 {
    public static void postOnAnimation(View view, Runnable r) {
        view.postOnAnimation(r);
    }
}
