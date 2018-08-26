package cn.vove7.common.datamanager.parse.statusmap;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import cn.vove7.common.utils.RegUtils;
import kotlin.text.Regex;

/**
 * 次节点以%开始
 * % -> ([\S\s]*)
 * Created by Vove on 2018/6/23
 */
@Entity
public class Reg implements Serializable {
    public static final long serialVersionUID = 1L;
    @Id
    private Long id;
    /**
     * 正则表达式
     */
    @NotNull
    private String regStr;
    private int paramPos = PARAM_NO;
    @Transient
    private Regex regex;
    private long nodeId;

    @Keep
    public Reg(String regStr, int paramPos, long nodeId) {
        this.regStr = regStr;
        this.paramPos = paramPos;
        this.nodeId = nodeId;
    }

    @Keep
    public Reg(String regStr, int paramPos) {//Test need
        this.regStr = regStr;
        this.paramPos = paramPos;
    }

    @Override
    public String toString() {
        return "Reg{" +
                "regStr='" + regStr + '\'' +
                '}';
    }

    @Keep
    private void buildRegex() {
        //结尾加上% ， 防止有[后续节点操作]匹配失败
        this.regStr = (!regStr.endsWith("%") ? regStr + "%" : regStr)
                .replace("%", RegUtils.INSTANCE.getREG_ALL_CHAR());
        //Vog.INSTANCE.v(this, regStr);
        regex = new Regex(this.regStr);
    }

    public Reg() {
    }

    @Keep
    public Regex getRegex() {
        if (regex == null)
            buildRegex();
        return regex;
    }

    public void setRegex(Regex regex) {
        this.regex = regex;
    }


    @Generated(hash = 526783767)
    public Reg(Long id, @NotNull String regStr, int paramPos, long nodeId) {
        this.id = id;
        this.regStr = regStr;
        this.paramPos = paramPos;
        this.nodeId = nodeId;
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

    public int getParamPos() {
        return this.paramPos;
    }

    public void setParamPos(int paramPos) {
        this.paramPos = paramPos;
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
    public static final int PARAM_NO = -999;
    public static final int PARAM_POS_END = -1;
    public static final int PARAM_POS_0 = 0;
    public static final int PARAM_POS_1 = 1;
    public static final int PARAM_POS_2 = 2;
    public static final int PARAM_POS_3 = 3;

}
