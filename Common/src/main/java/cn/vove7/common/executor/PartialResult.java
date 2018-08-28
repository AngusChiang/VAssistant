package cn.vove7.common.executor;

/**
 * @author 17719
 * <p>
 * 2018/8/6
 */
public class PartialResult {
    public boolean isSuccess;
    public boolean needTerminal;
    public String msg;

    public static PartialResult success() {
        return new PartialResult(true);
    }

    public static PartialResult failed() {
        return new PartialResult(false);
    }

    public static PartialResult fatal(String msg) {
        return new PartialResult(false, true, msg);
    }

    public PartialResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public PartialResult(boolean isSuccess, String msg) {
        this.isSuccess = isSuccess;
        this.msg = msg;
    }


    public PartialResult(boolean isSuccess, boolean needTerminal, String msg) {
        this.isSuccess = isSuccess;
        this.needTerminal = needTerminal;
        this.msg = msg;
    }

    public PartialResult(boolean isSuccess, boolean needTerminal) {
        this.isSuccess = isSuccess;
        this.needTerminal = needTerminal;
    }
}
