package org.limit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * todo 限流算法存在bug,
 * todo 定义桶的大小为 500，实际进入的请求数有可能会大于500
 * 漏桶算法(这个代码的实现是按照capacity/1s的速度进行流出的,即 流出速率 = 桶的容量 / 1000ms(毫秒))
 * <p>
 * 按照一定的速率，输出，如定义桶的容量为100,流完所需的时间为1s,则速率100/1s.
 * 即 通过控制桶的容量(RPS),可限制每秒的最大请求量不超过桶的大小
 * 参考博客：
 * https://blog.csdn.net/king0406/article/details/103328354
 *
 * @author wjy5@meitu.com
 * @date 2020/8/8 10:50 下午
 */
public class LeakBucket {

    /**
     * 桶的容量,一秒流完,即请求的PRS
     */
    private AtomicLong capacity;
    /**
     * 目前水桶剩下的水量
     */
    private AtomicLong remainWater;
    /**
     * 上一时间(毫秒)
     */
    private AtomicLong preTime;

    private ReentrantLock lock = new ReentrantLock();

    public LeakBucket(long rps) {
        capacity = new AtomicLong(rps);
        remainWater = new AtomicLong(rps);
        preTime = new AtomicLong(0);
    }

    /**
     * 限流
     *
     * @return true： 获取到锁，不限流
     */
    public boolean acquire() {
        try {
            lock.lockInterruptibly();

            long cntTimeMillis = System.currentTimeMillis();
            // 设置后，返回旧的数据
            long preTimeMills = preTime.getAndSet(cntTimeMillis);
            // 流出的速率
            double rate =  capacity.get() / 1000.0d;
            //这一段时间内的出水量 速率 * 时长
            long outWater = Math.round(rate * (cntTimeMillis - preTimeMills)) ;
            System.out.println(Thread.currentThread().getName() + ":出水量:" + outWater);
            // 桶内还剩余的水量
            remainWater.set(Math.max(0, remainWater.get() - outWater));
            System.out.println("剩余的出水量：" + remainWater.get());

            // 判断  当前桶内剩余的水 + 当前流入桶内的水(1.0) 是否超过当前桶的容量
            // 剩余水量+1,表示流入桶内的水。有流出，也会有流入
            if (remainWater.addAndGet(1) <= capacity.get()) {
                System.out.println(Thread.currentThread().getName() + ":当前剩余容量：" + remainWater.get());
                return true;
            }
            // 已经超过了当前的桶容量
//            System.out.println(Thread.currentThread().getName() + ":超出了桶的大小");
            return false;
        } catch (InterruptedException e) {
            //ignore log
        } finally {
            lock.unlock();
        }
        return false;
    }


    /**
     * 来源：https://github.com/cent-c/Limiters/blob/master/src/limiters/LeakyBucketLimiter.java
     * 下面的算法：为实现匀速处理请求的逻辑为看懂
     */
    /*
    public class LeakyBucketLimiter {

        private final long capacity;                                            // 水桶容量, 一秒流光
        private double remainWater;                                             // 目前水桶剩下的水量
        private long lastTime;                                                  // 时间戳
        private ReentrantLock lock = new ReentrantLock();                       // 可重入锁

        LeakyBucketLimiter(int qps) {
            capacity = qps;
            remainWater = capacity;
            lastTime = 0;
        }

        public boolean tryAcquire() {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                double outWater = ((now - lastTime)/1000.0) * capacity;         // 计算这段时间匀速流出的水
                lastTime = now;
                remainWater = Math.max(0, remainWater - outWater);
                if (remainWater + 1 <= capacity) {
                    remainWater += 1;
                    //todo 不明白 为什么休眠该时间段 可以实现匀速处理请求
                    //todo 个人对匀速处理请求这段代码的理解：当前桶的剩余量为 remainWater，需要完全流完需要（remainWater / capacity) * 1000 的时间
                    //todo 因此，通过休眠该时长，保证了桶的水量完全流完，在进行每个请求的处理，因此就实现了匀速处理每个请求
                    long waitingMs = (long)((remainWater / capacity) * 1000);   // 计算刚加入的水滴完全滴出漏桶需要的时间（毫秒）
                    lock.unlock();
                    try {
                        Thread.sleep(waitingMs);                                // 为实现匀速处理请求，需要阻塞一段时间后再return
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else return false;
            } finally {
                if (lock.isLocked()) lock.unlock();
            }

        }
    }
    */


    // 测试
    public static void main(String[] args) {
        LeakBucket leakBucket = new LeakBucket(300);
        Map<Long, AtomicLong> countPerSec = new ConcurrentHashMap<>();
        AtomicLong count = new AtomicLong(0);
        for (int i = 0; i < 2000; i++) {
            new Thread(() -> {
                if (leakBucket.acquire()) {
                    long cntTime = System.currentTimeMillis() / 1000;
                    AtomicLong atomicCount = countPerSec.get(cntTime);
                    synchronized (leakBucket) {
                       atomicCount = countPerSec.get(cntTime);
                        if (atomicCount == null) {
                            atomicCount = new AtomicLong(0);
                            countPerSec.put(cntTime, atomicCount);
                        }
                    }
                    atomicCount.incrementAndGet();
//                    System.out.println(System.currentTimeMillis() / 1000 + ":" + Thread.currentThread().getName() + ":获取了锁");
                }
            }).start();
        }
        System.out.println(countPerSec);
    }

}
