package com.zuni.library.listener;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface zOnImageDownload {
	 void onDownloadSucc(Bitmap bitmap, ImageView imageView, String path);
	// void onDownloadSucc(Bitmap bitmap, ImageView imageView);
}
