package com.zuni.library.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.zuni.library.utils.zContextUtil;
import com.zuni.library.utils.zFileUtil;
import com.zuni.library.utils.zToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * manager about download the update and install
 *
 * @author jacob
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FileDownloadManager {
    public static String WPS_DOWNLOAD_URL = "http://kad.www.wps.cn/wps/download/android/kingsoftoffice_2052/moffice_cn00563.apk";
    private static final int DOWNLOAD = 1;
    private static final int DOWNLOAD_APK_FINISH = 2;
    private static final int DOWNLOAD_FILE_FINISH = 3;
    private String DOWNLOAD_PATH = null;
 //   private static int DOWNLOAD_FLAG = -1;//0wifi 1data
    private String mSavePath;
    private int progress;
    private boolean cancelUpdate = false;
    private Context mContext;
    ProgressDialog builder = new ProgressDialog(mContext);
    private String mDownloadFileName;
    private String mUrl;
    private NotificationManager mNotiManager = null;
    private Notification.Builder mNotiBuilder = null;
    private boolean isApk = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD:
                    //update progress
                    //		System.out.println(progress);
/*                    if (DOWNLOAD_FLAG == 0) {
                        if (progress % 20 == 0) {
                            mNotiBuilder.setProgress(100, progress, false)
                                    .setContentInfo(progress + "%");
                            mNotiManager.notify(0, mNotiBuilder.build());
                        }
                    } else*/
                        builder.setProgress(progress);
                    break;

                case DOWNLOAD_APK_FINISH:
                    // download finish and cancel the notification
                  //  if (DOWNLOAD_FLAG == 0)
                   //     mNotiManager.cancelAll();
                    if(builder!=null&&builder.isShowing())
                        builder.dismiss();
                    // install
                    installApk();
                    break;
                case DOWNLOAD_FILE_FINISH:
                    openOfficeFileWithWPS(msg.getData().getString("path"));
                    if(builder!=null&&builder.isShowing())
                        builder.dismiss();
                    break;
                case 999:
                    zToastUtil.show(mContext, "下载地址出错");
                    if(builder!=null&&builder.isShowing())
                        builder.dismiss();
                    break;
            }
        }
    };

    public FileDownloadManager(Context context, String url, String name, boolean b) {
        super();
        mContext = context;
        mUrl = url;
        mDownloadFileName = name;
        isApk = b;
    }

/*
    public void checkUpdate(int flag) {
        DOWNLOAD_FLAG = flag;
        if (Const.isUpdate) {
            if (DOWNLOAD_FLAG == 0) //wifi
                showNotification();
            else                //data
                showNoticeDialog();
        } else {
            FDToastUtil.show(mContext, "现在已经是最新版本");
        }
    }*/

    public void downloadFile() {
     //   DOWNLOAD_FLAG = 1;
        Builder builder = new Builder(mContext);
        builder.setTitle("下载");
        builder.setMessage("需要先下载文档才可查看，是否下载？");
        builder.setPositiveButton("下载",
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDownloadDialog();
                    }
                });
        builder.setNegativeButton("暂不", null);
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    public void openOfficeFileWithWPS(String path) {
        if (zContextUtil.isAvilible(mContext, "cn.wps.moffice_eng")) {
            Uri uri = Uri.fromFile(new File(path));
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2");
            intent.setData(uri);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
               zToastUtil.show(mContext, "无法打开文件");
            }
        } else {
            String sd = zFileUtil.getExtPath() + "/" + DOWNLOAD_PATH +"wps.apk";
            if(zFileUtil.fileIsExists(sd)){
                mDownloadFileName = "wps.apk";
                mSavePath = zFileUtil.getExtPath() + "/" + DOWNLOAD_PATH;
                installApk();
            }else{
                WPSInstallDialog();
            }

        }
    }

    public void WPSInstallDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("下载WPS");
        builder.setMessage("需要先下载WPS才可查看文档，是否下载？");
        builder.setPositiveButton("下载",
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mUrl = WPS_DOWNLOAD_URL;
                        mDownloadFileName = "wps.apk";
                        isApk = true;
                        showDownloadDialog();
                    }
                });
        builder.setNegativeButton("暂不", null);
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showNotification() {
        mNotiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiBuilder = new Notification.Builder(mContext)
                .setContentTitle("正在下载")
                .setProgress(100, 0, false)
                .setTicker("开始下载")
                .setContentInfo("0%");
        mNotiManager.notify(0, mNotiBuilder.build());
        downloadApk();
    }


    public void showNoticeDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("更新");
        builder.setMessage("发现新版本，是否更新？");
        builder.setPositiveButton("更 新",
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDownloadDialog();
                    }
                });
        builder.setNegativeButton("暂 不", null);
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog() {
        builder.setCancelable(false);
        builder.setTitle("进度");
        builder.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        builder.show();
        downloadApk();
    }

    /**
     * run the download thread
     */
    private void downloadApk() {
        new DownloadApkThread().start();
    }


    /**
     * download thread
     *
     * @author
     */
    public class DownloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    mSavePath = zFileUtil.getExtPath() + "/" + DOWNLOAD_PATH;
                    URL url = new URL(mUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mDownloadFileName);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte buf[] = new byte[1024];
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        progress = (int) (((float) count / length) * 100);
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            if (isApk)
                                mHandler.sendEmptyMessage(DOWNLOAD_APK_FINISH);
                            else {
                                Message msg = mHandler.obtainMessage();
                                Bundle b = new Bundle();
                                b.putString("path", apkFile.toString());
                                msg.setData(b);
                                msg.what = DOWNLOAD_FILE_FINISH;
                                mHandler.sendMessage(msg);
                            }
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                mHandler.sendEmptyMessage(999);
                e.printStackTrace();
            }

        }
    }

    /**
     * install
     */
    private void installApk() {
        zContextUtil.installApk(mContext,mSavePath,mDownloadFileName);
    }
}
