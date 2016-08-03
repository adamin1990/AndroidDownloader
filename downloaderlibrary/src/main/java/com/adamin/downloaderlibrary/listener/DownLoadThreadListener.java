package com.adamin.downloaderlibrary.listener;

import com.adamin.downloaderlibrary.domain.DownThreadInfo;

/**
 * Created by Adam on 2016/8/3.
 */
public interface DownLoadThreadListener {
    void onProgress(int progress);
    void onStop(DownThreadInfo downThreadInfo);
    void onFinish(DownThreadInfo downThreadInfo);
}
