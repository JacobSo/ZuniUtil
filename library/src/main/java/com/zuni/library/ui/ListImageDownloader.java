package com.zuni.library.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.zuni.library.listener.zOnImageDownload;
import com.zuni.library.utils.zFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片下载工具类
 *
 * @author
 */
public class ListImageDownloader {
    private static final String TAG = "ImageDownloader";
    private static int DOWNLOAD_FLAG = 0;
    private HashMap<String, MyAsyncTask> map = new HashMap<String, MyAsyncTask>();
    private Map<String, SoftReference<Bitmap>> imageCaches = new HashMap<String, SoftReference<Bitmap>>();

    /**
     * @param url        该mImageView对应的url
     * @param mImageView
     * @param path       文件存储路径
     * @param download   OnImageDownload回调接口，在onPostExecute()中被调用
     */
    public void imageDownload(String url, ImageView mImageView, String path, Context context, zOnImageDownload download) {
        SoftReference<Bitmap> currBitmap = imageCaches.get(url); // 软引用缓存列表
        Bitmap softRefBitmap = null;
        if (currBitmap != null)
            softRefBitmap = currBitmap.get();// 从软引用缓存列表取图
        String imageName = "";
        if (url != null) {
            imageName = zFileUtil.getFileName(url); // 取名
            Log.v(TAG, "-图片名字：" + imageName);
        }
        Bitmap bitmap = getBitmapFromFile(context, imageName, path); // 从文件取图
        if (currBitmap != null && mImageView != null && softRefBitmap != null) {// 先从软引用中拿数据
          //  download.onDownloadSucc(softRefBitmap, mImageView);
            download.onDownloadSucc(softRefBitmap, mImageView, zFileUtil.getExtPath() +"/"+ path + imageName);
        }else if (bitmap != null && mImageView != null) {// 软引用中没有，从文件中拿数据
           // download.onDownloadSucc(bitmap, mImageView);
            download.onDownloadSucc(bitmap, mImageView, zFileUtil.getExtPath() +"/"+ path + imageName);
        }else if (url != null && needCreateNewTask(mImageView)) {//download stack
            MyAsyncTask task = new MyAsyncTask(url, mImageView, path, context, download);
            if (mImageView != null) {
                Log.i(TAG, "执行MyAsyncTask --> " + DOWNLOAD_FLAG);
                DOWNLOAD_FLAG++;
                task.execute();
                map.put(url, task);
            }
        }
    }

    /**
     * 判断是否需要重新创建线程下载图片，如果需要，返回值为true。
     */
    private boolean needCreateNewTask(ImageView mImageView) {
        boolean b = true;
        if (mImageView != null) {
            String curr_task_url = (String) mImageView.getTag();
            if (isTasksContains(curr_task_url))
                b = false;
        }
        return b;
    }

    /**
     * 检查该url（最终反映的是当前的ImageView的tag，tag会根据position的不同而不同）对应的task是否存在
     *
     * @param url
     * @return
     */
    private boolean isTasksContains(String url) {
        boolean b = false;
        if (map != null && map.get(url) != null)
            b = true;
        return b;
    }

    /**
     * 删除map中该url的信息，这一步很重要，不然MyAsyncTask的引用会“一直”存在于map中
     *
     * @param url
     */
    private void removeTaskFormMap(String url) {
        if (url != null && map != null && map.get(url) != null)
            map.remove(url);
    }

    /**
     * 从文件中拿图片
     */
    private Bitmap getBitmapFromFile(Context context, String imageName,String path) {
        Bitmap bitmap = null;
        if (imageName != null) {
            String real_path;
            try {
                if (zFileUtil.hasSDCard()) {
                    real_path = zFileUtil.getExtPath() + (path != null && path.startsWith("/") ? path : "/" + path);
                    //	Log.v("sd:path:", real_path);
                } else {
                    real_path = zFileUtil.getPackagePath(context) + (path != null && path.startsWith("/") ? path : "/" + path);
                }
                File file = new File(real_path, imageName);
                if (file.exists()) {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream( file));
                    if (bitmap == null)
                        Log.v(TAG, "bitmap failed:null");
                }
            } catch (Exception e) {
                e.printStackTrace();
                bitmap = null;
            }
        }
        return bitmap;
    }

    /**
     * 将下载好的图片存放到文件中
     */
    private boolean setBitmapToFile(String path, Context context, String imageName, Bitmap bitmap) {
        String real_path;
        try {
            if (zFileUtil.hasSDCard()) {
                real_path = zFileUtil.getExtPath() + (path != null && path.startsWith("/") ? path : "/"+ path);
            } else {
                real_path = zFileUtil.getPackagePath(context) + (path != null && path.startsWith("/") ? path : "/" + path);
            }
            File file = new File(real_path, imageName);
            if (!file.exists()) {
                File file2 = new File(real_path + "/");
                file2.mkdirs();
            }
            file.createNewFile();
            FileOutputStream fos;
            if (zFileUtil.hasSDCard()) {
                fos = new FileOutputStream(file);
            } else {
                fos = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            }

            if (imageName != null && (imageName.contains(".png") || imageName.contains(".PNG"))) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
            fos.flush();
            if (fos != null)
                fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 辅助方法，一般不调用
     */
    private void removeBitmapFromFile(String path, Context context,String imageName) {
        String real_path;
        try {
            if (zFileUtil.hasSDCard()) {
                real_path = zFileUtil.getExtPath()
                        + (path != null && path.startsWith("/") ? path : "/"
                        + path);
            } else {
                real_path = zFileUtil.getPackagePath(context)
                        + (path != null && path.startsWith("/") ? path : "/"
                        + path);
            }
            File file = new File(real_path, imageName);
            if (file != null)
                file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步下载图片的方法
     *
     * @author yanbin
     */
    private class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView mImageView;
        private String url;
        private zOnImageDownload download;
        private String path;
        private Context mContext;

        public MyAsyncTask(String url, ImageView mImageView, String path,
                           Context context, zOnImageDownload download) {
            this.mImageView = mImageView;
            this.url = url;
            this.path = path;
            this.mContext = context;
            this.download = download;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap data = null;
            if (url != null) {
                try {
                    URL c_url = new URL(url);
                    InputStream bitmap_data = c_url.openStream();
                    data = BitmapFactory.decodeStream(bitmap_data);
                    String imageName = zFileUtil.getFileName(url);
                    if (!setBitmapToFile(path, mContext, imageName, data)) {
                        removeBitmapFromFile(path, mContext, imageName);
                    }
                    imageCaches.put(url, new SoftReference<>(Bitmap.createScaledBitmap(data, 400, 400, true)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (download != null) {
            //    download.onDownloadSucc(result, mImageView);
                download.onDownloadSucc(result, mImageView, zFileUtil.getExtPath()+"/" + path + zFileUtil.getFileName(url));
                removeTaskFormMap(url);
            }
            super.onPostExecute(result);
        }
    }
}
