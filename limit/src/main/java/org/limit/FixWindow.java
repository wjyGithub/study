package com.springboot.first.windows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 固定窗口算法,限制流量
 *
 * @author wjy5@meitu.com
 * @date 2020/8/8 5:37 下午
 */
public class FixWindow {

    /**
     * 固定窗口算法
     */
    private LoadingCache<Long, AtomicLong> fixWindowCache = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(
                    new CacheLoader<Long, AtomicLong>() {
                        @Override
                        public AtomicLong load(Long aLong) throws Exception {
                            return new AtomicLong(0);
                        }
                    }
            );
    //限制
    private long limit = 15;
    private long size;

    public FixWindow(long limit, long size) {
        this.limit = limit;
        this.size = size;
    }

    /**
     * 限流
     */
    public void limit() throws ExecutionException {
        if (size != 0) {
            // 在一个时间窗口内，商是一样的，如 1000/10 = 50  1001/10 = 50 ....
            long cntTimeSec = System.currentTimeMillis() / (size * 1000);
            AtomicLong reqCount = fixWindowCache.get(cntTimeSec);
            if (reqCount.get() >= limit) {
                //限流的操作
                System.out.println(Thread.currentThread().getName() + ":被限流了");
            } else {
                reqCount.incrementAndGet();
            }
        }
    }

    public LoadingCache<Long, AtomicLong> getFixWindowCache() {
        return fixWindowCache;
    }


    public static void main(String[] args) {
        FixWindow fixWindow = new FixWindow(20, 5);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            try{
                TimeUnit.MILLISECONDS.sleep(20);
            }catch (InterruptedException e){}
            new Thread(() -> {
                try {
                    fixWindow.limit();
                } catch (Exception e) {
                }
            }).start();
        }
        System.out.println(fixWindow.getFixWindowCache().asMap());
        System.out.println((System.currentTimeMillis() - start) / 1000 / 5);
    }

}
