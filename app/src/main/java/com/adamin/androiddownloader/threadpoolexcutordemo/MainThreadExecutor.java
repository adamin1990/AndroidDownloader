package com.adamin.androiddownloader.threadpoolexcutordemo;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Created by Adam on 2016/7/29.
 */
public class MainThreadExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable command) {

        handler.post(command);

    }
}
