package com.springboot.first.windows;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * 漏桶算法
 *
 * @author wjy5@meitu.com
 * @date 2020/8/8 10:50 下午
 */
public class LeakBucket<T> {

    /**
     * 桶的容量,一秒流完,即请求的PRS
     */
    private AtomicLong capacity;
    /**
     * 目前水桶剩下的水量
     */
    private AtomicDouble remainWater;
    /**
     * 上一时间(毫秒)
     */
    private AtomicLong preTime;

    private ReentrantLock lock = new ReentrantLock();

    public LeakBucket(long rps) {
        capacity = new AtomicLong(rps);
        remainWater = new AtomicDouble(rps);
        preTime = new AtomicLong(0);
    }

    /**
     * 限流
     */
    public void limit() {

        long cntTimeMillis = System.currentTimeMillis();
        long preTimeMillis = preTime.get();
        //毫秒级别的时间间隔
        long timeInterval = cntTimeMillis - preTimeMillis;
        double outWater = timeInterval * capacity.get();
        remainWater.set(Math.max(0,remainWater.get() - outWater));


    }



}
