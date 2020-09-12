package org.cb;


import org.cb.state.CloseCBState;

/**
 * 本地熔断器(把它当成了工厂)（https://github.com/hirudy/circuitBreaker)
 *
 * @author wjy5@meitu.com
 * @date 2020/9/6 7:12 下午
 */
public class LocalCircuitBreaker extends AbstractCircuitBreaker {

    public LocalCircuitBreaker(String failRateForClose,
                               int idleTimeForOpen,
                               String passRateForHalfOpen, int failNumForHalfOpen){
        this.thresholdFailRateForClose = failRateForClose;
        this.thresholdIdleTimeForOpen = idleTimeForOpen;
        this.thresholdPassRateForHalfOpen = passRateForHalfOpen;
        this.thresholdFailNumForHalfOpen = failNumForHalfOpen;
    }

    @Override
    public void reset() {
        this.setState(new CloseCBState());
    }

    @Override
    public boolean canPassCheck() {
        return getState().canPassCheck(this);
    }

    @Override
    public void countFailNum(){
        getState().countFailNum(this);
    }

}
