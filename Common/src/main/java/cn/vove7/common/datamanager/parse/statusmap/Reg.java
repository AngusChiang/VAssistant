package cn.vove7.common.datamanager.parse.statusmap;

import com.google.gson.annotations.Expose;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import cn.vove7.common.app.GlobalLog;
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
    @Expose(serialize = false)
    @Id
    private Long id;
    /**
     * 正则表达式
     */
    @NotNull
    private String regStr;
    /**
     * 数组 分隔 ','
     */
    private String paramPos = null;// String.valueOf(PARAM_NO);

    @Transient
    @Expose(serialize = false)
    private Regex regex;
    private long nodeId;

    @Keep
    public Reg(String regStr, int paramPos, long nodeId) {
        this.regStr = regStr;
        this.paramPos = String.valueOf(paramPos);
        this.nodeId = nodeId;
    }

    @Keep
    public Reg(String regStr, String paramPos) {//Test need
        this.regStr = regStr;
        this.paramPos = paramPos;
    }

    @Override
    public String toString() {
        return regStr;
    }

    @Keep
    private void buildRegex() {
        //结尾加上% ， 防止有[后续节点操作]匹配失败
        String s = (!regStr.endsWith("%") ? regStr + "%" : regStr)
                .replace("%", RegUtils.INSTANCE.getREG_ALL_CHAR());
        regex = new Regex(s);
    }

    public Reg() {
    }

    @Keep
    public Regex getRegex() {
        if (regex == null)
            buildRegex();
        return regex;
    }

    @Keep
    public Regex getFollowRegex() {
        //头部加上% ， 防止有前参数匹配失败
        String s = (!regStr.startsWith("%") ? "[\\S\\s]*?" + regStr : regStr);

        //尾部
        s = (!s.endsWith("%") ? s + "%" : s)
                .replace("%", RegUtils.INSTANCE.getREG_ALL_CHAR());

        regex = new Regex(s);
        return regex;
    }

    public void setRegex(Regex regex) {
        this.regex = regex;
    }


    @Generated(hash = 1266605423)
    public Reg(Long id, @NotNull String regStr, String paramPos, long nodeId) {
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

    @Transient
    @Expose(serialize = false)
    private Integer[] paramPosArr;

    public String getParamPos() {
        return paramPos;
    }

    public Integer[] getParamPosArray() {
        if (paramPosArr != null) return paramPosArr;
        if (this.paramPos == null) return null;
        String[] ss = this.paramPos.split(",");
        paramPosArr = new Integer[ss.length];
        try {
            int i = 0;
            for (String s : ss) {
                paramPosArr[i++] = Integer.parseInt(s);
            }
            return paramPosArr;
        } catch (NumberFormatException e) {
            GlobalLog.INSTANCE.err(e);
            return null;
        }
    }

    public void setParamPos(String paramPos) {
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
    @Deprecated
    public static final int PARAM_NO = -999;
    public static final int PARAM_POS_END = -1;
    //public static final int PARAM_POS_0 = 0;
    public static final int PARAM_POS_1 = 1;
    public static final int PARAM_POS_2 = 2;
    public static final int PARAM_POS_3 = 3;

}
