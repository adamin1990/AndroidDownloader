package com.adamin.androiddownloader.threadpoolexcutordemo;

/**
 * Created by Adam on 2016/7/29.
 */
public class PriorityRunnable implements Runnable {

    private final Priority priority;

    public PriorityRunnable(Priority priority) {
        this.priority = priority;
    }

    @Override
    public void run() {

    }
    public Priority getPriority(){
        return  priority;
    }
}
