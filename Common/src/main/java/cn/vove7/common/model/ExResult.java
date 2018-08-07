package cn.vove7.common.model;

/**
 * @author Vove
 * <p>
 * 2018/8/7
 */
public class ExResult <ReturnType> {
    public boolean ok = true;
    public String errMsg;
    public String readableMsg;
    public ReturnType returnValue;

    public ExResult() {
    }

    /**
     * 失败
     * @param errMsg 失败信息
     */
    public ExResult(String errMsg, String readableMsg) {
        ok = false;
        this.errMsg = errMsg;
        this.readableMsg = readableMsg;
    }

    public ExResult(ReturnType returnValue) {
        this.returnValue = returnValue;
    }
}
