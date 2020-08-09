package org.limit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 滑动窗口算法
 *
 * @author wjy5@meitu.com
 * @date 2020/8/8 8:30 下午
 */
public class SlideWindow {

    /**
     * 时间片段
     */
    private LoadingCache<Long, AtomicLong> timeSegmentCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, AtomicLong>() {
                @Override
                public AtomicLong load(Long aLong) throws Exception {
                    return new AtomicLong(0);
                }
            });
    /**
     * 默认是10s
     */
    private long windowSize = 10;
    /**
     * 时间片段1s
     */
    private long timeSegment = 1;
    /**
     * 流量限制
     */
    private long limit = 10;
    /**
     * 时间判断的计数器
     */
    private long counter = windowSize / timeSegment;

    public SlideWindow(long windowSize, long limit) {
        this.windowSize = windowSize;
        this.limit = limit;
        this.counter = windowSize / timeSegment;
    }

    /**
     * 限流算法
     */
    public void limit() throws ExecutionException {
        // 固定时间窗口 窗口大小：timeSegment
        long timeSegSec = System.currentTimeMillis() / (timeSegment * 1000);
        AtomicLong reqCount = timeSegmentCache.get(timeSegSec);
        reqCount.incrementAndGet();
        long totalReqCount = 0L;
        for(int i=0; i<counter; i++) {
            totalReqCount += timeSegmentCache.get(timeSegSec-1).get();
        }
        System.out.println("限制大小：" + limit + ", 总请求：" + totalReqCount);
        if(totalReqCount > limit) {
            System.out.println(Thread.currentThread().getName() + ":被限流了");
        }
    }

    public LoadingCache<Long, AtomicLong> getTimeSegmentCache() {
        return timeSegmentCache;
    }

    // 测试
    public static void main(String[] args) {
        SlideWindow slideWindow = new SlideWindow(4,500);
        for(int i=0; i<2000; i++) {
            new Thread(()-> {
                try {
                    slideWindow.limit();
                }catch (Exception e){}
            }).start();
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            }catch (InterruptedException e){}
        }
        System.out.println(slideWindow.getTimeSegmentCache().asMap());
    }

}
