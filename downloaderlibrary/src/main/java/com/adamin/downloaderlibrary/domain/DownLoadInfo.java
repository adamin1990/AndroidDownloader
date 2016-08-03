package com.adamin.downloaderlibrary.domain;

import com.adamin.downloaderlibrary.listener.DownLoadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam on 2016/8/3.
 */
public class DownLoadInfo {
    public int totalsize;
    public int currentsize;
    public String fileName;
    public String downloadPath;
    public String downloadUrl;

    boolean hasListener;
    boolean isResume;
    boolean isStop;
    List<DownThreadInfo> downThreadInfos;
    DownLoadListener listener;

    File file;

    public DownLoadInfo() {
        downThreadInfos=new ArrayList<>();
    }

    synchronized  void addDownloadThread(DownThreadInfo downThreadInfo){
        this.downThreadInfos.add(downThreadInfo);
    }

    synchronized void removeDownloadThread(DownThreadInfo downThreadInfo){
        this.downThreadInfos.remove(downThreadInfo);
    }
}
