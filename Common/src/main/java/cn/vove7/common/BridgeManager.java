package cn.vove7.common;

import cn.vove7.common.bridges.GlobalActionAutomator;
import cn.vove7.common.executor.CExecutorI;

/**
 * @author 17719
 * <p>
 * 2018/8/6
 */
public class BridgeManager {
     private CExecutorI executor;
     private GlobalActionAutomator automator;
    // TODO: 2018/8/6 other bridge


    public GlobalActionAutomator getAutomator() {
        return automator;
    }

    public void setAutomator(GlobalActionAutomator automator) {
        this.automator = automator;
    }

    public BridgeManager(CExecutorI executor, GlobalActionAutomator automator) {
        this.executor = executor;
        this.automator = automator;
    }

    public BridgeManager(CExecutorI executor) {
        this.executor = executor;
    }

    public CExecutorI getExecutor() {
        return executor;
    }

    public void setExecutor(CExecutorI executor) {
        this.executor = executor;
    }
}
