package com.adamin.androiddownloader.threadpoolexcutordemo;

import java.util.concurrent.Future;

/**
 * Created by Adam on 2016/7/29.
 */
public class Test {

    /*
* 后台任务
*/
    public void doSomeBackgroundWork() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里后台工作.
                    }
                });
    }

    /*
    * 轻量后台任务
    */
    public void doSomeLightWeightBackgroundWork() {
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里做一些轻量后台工作.
                    }
                });
    }

    /*
    * 主线程任务
    */
    public void doSomeMainThreadWork() {
        DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        // 做一些中线程工作.
                    }
                });
    }

    /**
     * 可取消后台任务
     */
    public void doFutureTask(){
   Future future= DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(new Runnable() {
                    @Override
                    public void run() {

                    }
                });

        future.cancel(true);

    }

    public void doSomeTaskAtHighPriority(){
        DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                .submit(new PriorityRunnable(Priority.HIGH){
                    @Override
                    public void run() {
                        super.run();
                    }
                });
    }

}
