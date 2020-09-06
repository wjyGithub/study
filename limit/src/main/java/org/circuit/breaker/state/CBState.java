package org.circuit.breaker.state;

import org.circuit.breaker.cb.AbstractCircuitBreaker;

/**
 * 熔断器状态（https://github.com/hirudy/circuitBreaker)
 *
 * @author wjy5@meitu.com
 * @date 2020/9/6 6:44 下午
 */
public interface CBState {

    /**
     * 获取当前状态名称
     */
    String getStateName();

    /**
     * 检查以及校验当前状态是否需要扭转
     */
    void checkAndSwitchState(AbstractCircuitBreaker cb);

    /**
     * 是否允许通过熔断器
     */
    boolean canPassCheck(AbstractCircuitBreaker cb);

    /**
     * 统计失败次数
     */
    void countFailNum(AbstractCircuitBreaker cb);


}
