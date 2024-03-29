package cn.vove7.common.datamanager.history;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Created by Vove on 2018/9/11
 */

@Keep
public class CommandHistory {
    @Keep
    @Expose
    private Long userId;

    @Keep
    private Date requestTime;

    @Keep
    private String command;

    @Keep
    private String result;
    @Keep
    private String tagId; //todo

    public CommandHistory(Long userId, Date requestTime, String command, String result, String tagId) {
        this.userId = userId;
        this.requestTime = requestTime;
        this.command = command;
        this.result = result;
        this.tagId = tagId;
    }

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
