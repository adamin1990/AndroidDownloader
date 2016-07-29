package com.adamin.androiddownloader.threadpoolexcutordemo;

import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adam on 2016/7/29.
 */
public class DefaultExecutorSupplier {
    /*
    *指定线程数量
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    /**
     * 后台任务的线程池
     */
    private final ThreadPoolExecutor mForBackgroundTasks;
    /**
     * 轻量后台任务的线程池
     */
    private final ThreadPoolExecutor mForLightWeightBackgroundTasks;

    /**
     * 主线程任务的线程池executor
     */
    private final Executor mMainThreadExcutor;

    private static DefaultExecutorSupplier mInstance;

    /**
     * 返回DefaultExecutorSupplier的实例
     */
    public static DefaultExecutorSupplier getInstance() {

        if (mInstance == null) {

            synchronized (DefaultExecutorSupplier.class) {
                mInstance = new DefaultExecutorSupplier();
            }
        }

        return mInstance;

    }

    private DefaultExecutorSupplier() {
        ThreadFactory backgroundPriorityThreadFactory = new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);
//        mForBackgroundTasks = new ThreadPoolExecutor(
//                NUMBER_OF_CORES * 2,
//                NUMBER_OF_CORES * 2,
//                60L,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<Runnable>(),
//                backgroundPriorityThreadFactory
//
//        );
        mForBackgroundTasks=new PriorityThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        );

        mForLightWeightBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        );

        mMainThreadExcutor = new MainThreadExecutor();
    }


    /*
  * returns the thread pool executor for background task
  */
    public ThreadPoolExecutor forBackgroundTasks() {
        return mForBackgroundTasks;
    }

    /*
    * returns the thread pool executor for light weight background task
    */
    public ThreadPoolExecutor forLightWeightBackgroundTasks() {
        return mForLightWeightBackgroundTasks;
    }

    /*
    * returns the thread pool executor for main thread task
    */
    public Executor forMainThreadTasks() {
        return mMainThreadExcutor;
    }


}
