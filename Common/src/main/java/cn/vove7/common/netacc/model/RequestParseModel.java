package cn.vove7.common.netacc.model;

import cn.vove7.common.datamanager.parse.model.ActionScope;

/**
 * Created by Administrator on 2018/10/8
 */
public class RequestParseModel {
    private String command;
    private ActionScope scope;

    public String getCommand() {
        return command;
    }

    public RequestParseModel() {
    }

    public RequestParseModel(String command, ActionScope scope) {
        this.command = command;
        this.scope = scope;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ActionScope getScope() {
        return scope;
    }

    public void setScope(ActionScope scope) {
        this.scope = scope;
    }
}
