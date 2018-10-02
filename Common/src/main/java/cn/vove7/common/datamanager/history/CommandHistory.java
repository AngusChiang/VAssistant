package cn.vove7.common.datamanager.history;

import java.util.Date;

/**
 * Created by 17719247306 on 2018/9/11
 */

public class CommandHistory {
    private Long userId;

    private Date requestTime;

    private String command;

    private String result;

    public CommandHistory(Long userId, String command, String result) {
        this.userId = userId;
        this.command = command;
        this.result = result;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getRequestTime() {
        return this.requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
