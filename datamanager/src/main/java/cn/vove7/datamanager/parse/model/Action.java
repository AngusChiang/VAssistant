package cn.vove7.datamanager.parse.model;

import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Action集合
 * Created by Vove on 2018/6/18
 */
@Entity
public class Action implements Comparable<Action> {
    @Id
    private
    Long id;

    @Transient
    private String matchWord;
    /**
     * 执行优先级
     */
    private int priority;
    private long nodeId;

    /**
     * 脚本
     */
    private String actionScript;
    /**
     * 操作参数
     */
    @Transient
    private
    Param param;
    /**
     * 请求结果
     */
    @Transient
    Boolean responseResult = true;
    /**
     * 返回数据
     */
    @Transient
    Bundle responseBundle = new Bundle();


    @Generated(hash = 1674871007)
    public Action(Long id, int priority, long nodeId, String actionScript) {
        this.id = id;
        this.priority = priority;
        this.nodeId = nodeId;
        this.actionScript = actionScript;
    }

    @Generated(hash = 2056262033)
    public Action() {
    }


    public Action(Long id, String actionScript) {
        this.id = id;
        this.actionScript = actionScript;
    }

    @Keep
    public Action(String actionScript) {
        this.actionScript = actionScript;
    }

    public Action(int priority, String actionScript) {
        this.priority = priority;
        this.actionScript = actionScript;
    }

    public Param getParam() {
        if (this.param == null) {
            param = new Param();
        }
        return this.param;
    }


    public void setParam(Param param) {
        this.param = param;
    }

    public Bundle getResponseBundle() {
        return responseBundle;
    }

    public void setResponseBundle(Bundle responseBundle) {
        this.responseBundle = responseBundle;
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

    public Boolean getResponseResult() {
        return this.responseResult;
    }

    public void setResponseResult(Boolean responseResult) {
        this.responseResult = responseResult;
    }
}
