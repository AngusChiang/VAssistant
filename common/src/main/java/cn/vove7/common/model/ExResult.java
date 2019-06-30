package cn.vove7.common.model;

/**
 * @author Vove
 * <p>
 * 2018/8/7
 */
public class ExResult<ReturnType> {
    public boolean ok = true;
    public String errMsg;
    public ReturnType returnValue;

    public ExResult() {
    }

    /**
     * 失败
     *
     * @param errMsg 失败信息
     */
    public ExResult(String errMsg) {
        ok = false;
        this.errMsg = errMsg;
    }

    public ExResult<ReturnType> with(ReturnType returnValue) {
        this.returnValue = returnValue;
        return this;
    }
}
