package cn.vove7.common.datamanager.parse.statusmap;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import cn.vove7.paramregexengine.ParamRegex;

/**
 * 次节点以%开始
 * % -> ([\S\s]*)
 * ActionReg
 * Created by Vove on 2018/6/23
 */
@Entity
public class Reg implements Serializable {
    public static final long serialVersionUID = 1L;
    @Expose(serialize = false)
    @Id
    private Long id;
    /**
     * 正则表达式
     */
    @NotNull
    private String regStr;

    @Transient
    @Expose(serialize = false)
    private ParamRegex regex;
    private long nodeId;

    @Keep
    public Reg(String regStr, long nodeId) {
        this.regStr = regStr;
        this.nodeId = nodeId;
    }

    @Keep
    public Reg(String regStr) {//Test need
        this.regStr = regStr;
    }

    @NonNull
    @Override
    public String toString() {
        return regStr;
    }

    @Keep
    private void buildRegex() {
        //结尾加上% ， 防止有[后续节点操作]匹配失败
        String s = (!regStr.endsWith("%") ? regStr + "%" : regStr);
        regex = new ParamRegex(s);
    }

    public Reg() {
    }

    @Generated(hash = 1232803860)
    public Reg(Long id, @NotNull String regStr, long nodeId) {
        this.id = id;
        this.regStr = regStr;
        this.nodeId = nodeId;
    }

    @Keep
    public ParamRegex getRegex() {
        if (regex == null)
            buildRegex();
        return regex;
    }

    @Keep
    public ParamRegex getFollowRegex() {
        //头部加上% ， 防止有前参数匹配失败
        String s = (!regStr.startsWith("%") ? ("%" + regStr) : regStr);

        //尾部
        s = !s.endsWith("%") ? (s + "%") : s;

        regex = new ParamRegex(s);
        return regex;
    }

    public void setRegex(ParamRegex regex) {
        this.regex = regex;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegStr() {
        return this.regStr;
    }

    public void setRegStr(String regStr) {
        this.regStr = regStr;
    }

    public long getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * 参数位置，提取参数用
     */
    @Deprecated
    public static final int PARAM_NO = -999;
    public static final int PARAM_POS_END = -1;
    //public static final int PARAM_POS_0 = 0;
    public static final int PARAM_POS_1 = 1;
    public static final int PARAM_POS_2 = 2;
    public static final int PARAM_POS_3 = 3;

}
