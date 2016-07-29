package com.adamin.androiddownloader.threadpoolexcutordemo;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Adam on 2016/7/29.
 */
public class PriorityThreadFactory implements ThreadFactory {
    private final int mThreadPrority;

    public PriorityThreadFactory(int mThreadPrority) {
        this.mThreadPrority = mThreadPrority;
    }

    @Override
    public Thread newThread(final Runnable r) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPrority);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                r.run();

            }
        };
        return new Thread(runnable);
    }
}
