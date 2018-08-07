package cn.vove7.common;

import cn.vove7.common.bridges.GlobalActionAutomator;
import cn.vove7.common.executor.CExecutorI;
import cn.vove7.common.executor.OnExecutorResult;

/**
 * @author 17719
 * <p>
 * 2018/8/6
 */
public class BridgeManager {
    private CExecutorI executor;
    private GlobalActionAutomator automator;
    private SystemOperation systemBridge;
    private OnExecutorResult resultNotifier;
    // TODO: 2018/8/6 other bridge



    public BridgeManager(CExecutorI executor, GlobalActionAutomator automator, SystemOperation systemBridge, OnExecutorResult resultNotifer) {
        this.executor = executor;
        this.automator = automator;
        this.systemBridge = systemBridge;
        this.resultNotifier = resultNotifer;

    }

    public OnExecutorResult getResultNotifier() {
        return resultNotifier;
    }

    public void setResultNotifier(OnExecutorResult resultNotifier) {
        this.resultNotifier = resultNotifier;
    }

    public SystemOperation getSystemBridge() {
        return systemBridge;
    }

    public void setSystemBridge(SystemOperation systemBridge) {
        this.systemBridge = systemBridge;
    }

    public GlobalActionAutomator getAutomator() {
        return automator;
    }

    public void setAutomator(GlobalActionAutomator automator) {
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
