package org.circuit.breaker.cb;

/**
 * 熔断器接口（https://github.com/hirudy/circuitBreaker)
 *
 * @author wjy5@meitu.com
 * @date 2020/9/6 6:47 下午
 */
public interface CircuitBreaker {

    /**
     * 重置熔断器
     */
    void reset();

    /**
     * 是否允许通过熔断器
     */
    boolean canPassCheck();

    /**
     * 统计失败次数
     */
    void countFailNum();
}
