package com.adamin.downloaderlibrary.domain;

/**
 * Created by Adam on 2016/8/3.
 */
public class DownThreadInfo {
    String threadId;
    String url;
    int start,end;
    boolean isStop;

    public DownThreadInfo(String threadId, int end, int start, String url) {
        this.threadId = threadId;
        this.end = end;
        this.start = start;
        this.url = url;
    }
}
