package com.adamin.downloaderlibrary.listener;

import java.io.File;

/**
 * Created by Adam on 2016/8/3.
 */
public interface DownLoadListener {
    void onPepare();
    void onStart(String fileName,String url,int fileLength);
    void onProgress(int progress);
    void onStop(int progress);
    void onFinish(File file);
    void onSpeed(int speed);
    void onError(int status,String error);
}
