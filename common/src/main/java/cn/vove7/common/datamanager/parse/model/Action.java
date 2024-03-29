package cn.vove7.common.datamanager.parse.model;

import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cn.vove7.common.datamanager.parse.statusmap.ActionNode;

/**
 * Action 执行动作
 * Created by Vove on 2018/6/18
 */
@Entity
public class Action implements Comparable<Action>, Serializable {
    public static final long serialVersionUID = 1L;
    @Expose(serialize = false)
    @Id
    private
    Long id;

    @Expose(serialize = false)
    @Transient
    private String matchWord;
    @Expose(serialize = false)

    @Transient
    private ActionScope scope;

    /**
     * 执行优先级
     */
    private int priority;
    @Expose(serialize = false)
    private long nodeId;

    /**
     * same to {@link ActionNode#actionScopeType}
     */
    @Transient
    private int actionScopeType = -1;

    /**
     * 脚本
     */
    private String actionScript;

    private String scriptType;//脚本语言


    public static final String SCRIPT_TYPE_LUA = "lua";
    public static final String SCRIPT_TYPE_JS = "js";

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public boolean isNull() {
        return TextUtils.isEmpty(actionScript) && TextUtils.isEmpty(scriptType);
    }

    public ActionScope getScope() {
        return scope;
    }

    public void setScope(ActionScope scope) {
        this.scope = scope;
    }

    /**
     * 操作参数
     */
    @Transient
    @Expose(serialize = false)
    private
    Map<String, Object> param;
    /**
     * 返回数据
     */
    @Expose(serialize = false)
    @Transient
    private
    Bundle responseBundle = new Bundle();


    @Generated(hash = 404781010)
    public Action(Long id, int priority, long nodeId, String actionScript,
                  String scriptType) {
        this.id = id;
        this.priority = priority;
        this.nodeId = nodeId;
        this.actionScript = actionScript;
        this.scriptType = scriptType;
    }

    @Generated(hash = 2056262033)
    public Action() {
    }


    public Action(Long id, String actionScript, String scriptType) {
        this(actionScript, scriptType);
        this.id = id;
    }

    @Keep
    public Action(String actionScript, String scriptType) {
        this.actionScript = actionScript;
        this.scriptType = scriptType;
    }

    @Keep
    public Action(int priority, String actionScript, String scriptType) {
        this(actionScript, scriptType);
        this.priority = priority;
    }

    public Action cloneNew() {
        Action newA = new Action();
        newA.scriptType = scriptType;
        newA.actionScript = actionScript;
        return newA;
    }

    public Map<String, Object> getParam() {
        if (this.param == null) {
            param = new HashMap<>();
        }
        return this.param;
    }

    public int getActionScopeType() {
        return actionScopeType;
    }

    public void setActionScopeType(int actionScopeType) {
        this.actionScopeType = actionScopeType;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    @Override
    public int compareTo(@NonNull Action o) {
        return priority - o.priority;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatchWord() {
        return this.matchWord;
    }

    public void setMatchWord(String matchWord) {
        this.matchWord = matchWord;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public String getActionScript() {
        return this.actionScript;
    }

    public void setActionScript(String actionScript) {
        this.actionScript = actionScript;
    }

    @Override
    public String toString() {
        return "{" +
                actionScript + '\'' +
                "," + scriptType + '\'' + param +
                '}';
    }

}
