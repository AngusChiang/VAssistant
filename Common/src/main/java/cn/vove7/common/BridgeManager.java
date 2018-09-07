package cn.vove7.common;

import cn.vove7.common.bridges.GlobalActionExecutor;
import cn.vove7.common.bridges.ServiceBridge;
import cn.vove7.common.executor.CExecutorI;

/**
 * @author 17719
 * <p>
 * 2018/8/6
 */
public class BridgeManager {
    private CExecutorI executor;
    private GlobalActionExecutor automator;
    private SystemOperation systemBridge;
    private ServiceBridge serviceBridge;


    public BridgeManager(CExecutorI executor, GlobalActionExecutor automator, SystemOperation systemBridge, ServiceBridge serviceBridge) {
        this.executor = executor;
        this.automator = automator;
        this.systemBridge = systemBridge;
        this.serviceBridge = serviceBridge;
    }

    public ServiceBridge getServiceBridge() {
        return serviceBridge;
    }

    public void setResultNotifier(ServiceBridge serviceBridge) {
        this.serviceBridge = serviceBridge;
    }

    public SystemOperation getSystemBridge() {
        return systemBridge;
    }

    public void setSystemBridge(SystemOperation systemBridge) {
        this.systemBridge = systemBridge;
    }

    public GlobalActionExecutor getAutomator() {
        return automator;
    }

    public void setAutomator(GlobalActionExecutor automator) {
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
