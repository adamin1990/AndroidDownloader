package com.adamin.androiddownloader.threadpoolexcutordemo;

/**
 * Created by Adam on 2016/7/29.
 */
public enum Priority {
    /**
     * 注意：不要在任何情况下改变顺序，否则会使排序不准确
     */
    /**
     * 最低优先级，预加载数据用
     */
    LOW,
    /**
     * 中优先级
     */
    MEDIUM,
    /**
     * 高优先级
     */
    HIGH,
    /**
     * 立即
     */
    IMMEDIATE,
}
