package cn.vove7.common.datamanager.executor.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

import cn.vove7.common.utils.RegUtils;
import cn.vove7.vtp.log.Vog;
import kotlin.text.Regex;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 打开事件
 * Created by Vove on 2018/6/23
 */
@Entity(indexes = {@Index(value = "key")})
public class MarkedOpen {
    public static final String MARKED_TYPE_APP = "app";//应用 value -> pkg
    //public static final String MARKED_TYPE_SYS_FUN = "sys_fun";//系统功能 value: fun_key
    //public static final String MARKED_TYPE_SCRIPT = "script";
    public static final String MARKED_TYPE_SCRIPT_LUA = "script_lua";
    public static final String MARKED_TYPE_SCRIPT_JS = "script_js";
    public static final String MARKED_TYPE_CONTACT = "contact";

    @Id
    private Long id;
    @NotNull
    private String key;//alias
    private String type;
    /**
     * key的正则
     */
    private String regStr;
    @Transient
    private Regex regex;
    private String value;//标识

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //AppInfo data;
    @Keep
    public MarkedOpen(Long id, @NotNull String key) {
        this.id = id;
        this.key = key;
    }

    public Regex getRegex() {
        return regex;
    }

    @Keep
    public MarkedOpen() {
    }

    @Keep
    public MarkedOpen(String key, String type, String regStr, String value) {
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.value = value;
    }

    @Keep
    private void buildRegex() {
        String s = (!regStr.endsWith("%") ? regStr + "%" : regStr)
                .replace("%", RegUtils.INSTANCE.getREG_ALL_CHAR());
        regex = new Regex(s);
    }

    @Keep
    public MarkedOpen(Long id, @NotNull String key, String type, String regStr,
                      String value) {
        this.id = id;
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.value = value;
        buildRegex();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRegStr() {
        return this.regStr;
    }

    public void setRegStr(String regStr) {
        this.regStr = regStr;
    }

    @Override
    public String toString() {
        return "MarkedOpen{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", regStr='" + regStr + '\'' +
                ", regex=" + regex +
                ", value='" + value + '\'' +
                '}';
    }
}
