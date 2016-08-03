package com.adamin.downloaderlibrary.listener;

import java.io.File;

/**
 * Created by Adam on 2016/8/3.
 */
public class SimpleDownLoadListener implements DownLoadListener {
    @Override
    public void onPepare() {

    }

    @Override
    public void onStart(String fileName, String url, int fileLength) {

    }

    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onStop(int progress) {

    }

    @Override
    public void onFinish(File file) {

    }

    @Override
    public void onSpeed(int speed) {

    }

    @Override
    public void onError(int status, String error) {

    }
}
