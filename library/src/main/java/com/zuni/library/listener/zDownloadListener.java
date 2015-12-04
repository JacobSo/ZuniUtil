package com.zuni.library.listener;

/**
 * Created by Administrator on 2015/12/4.
 */
public interface zDownloadListener {
    void startDownLoad();

    void onDownLoadProgress(int paramInt);

    void endDownLoad(String paramString);

    void downLoadFail();
}
