package cn.vove7.common.datamanager.executor.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

import cn.vove7.common.datamanager.parse.DataFrom;
import cn.vove7.common.interfaces.Markable;
import cn.vove7.common.netacc.tool.SecureHelper;
import cn.vove7.common.utils.RegUtils;
import kotlin.text.Regex;

/**
 * 标记数据
 * Created by Vove on 2018/6/23
 */
@Entity(indexes = {@Index(value = "key")})
public class MarkedData implements DataFrom, Markable {

    //打开/关闭...
    public static final String MARKED_TYPE_APP = "open_app";//应用 value -> pkg
    public static final String MARKED_TYPE_SCRIPT_LUA = "open_script_lua";
    public static final String MARKED_TYPE_SCRIPT_JS = "open_script_js";
    //通讯录
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

    private String from = null;

    private String tagId;

    @Override
    public void sign() {
        setTagId(SecureHelper.MD5(key, type, from, regStr, value));
    }

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
    public MarkedData(Long id, @NotNull String key) {
        this.id = id;
        this.key = key;
    }

    public Regex getRegex() {
        if (regex == null) buildRegex();
        return regex;
    }

    @Keep
    public MarkedData() {
    }

    @Keep
    public MarkedData(String key, String type, String regStr, String value, String from) {
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.value = value;
        this.from = from;
    }

    @Keep
    public MarkedData(String key, String type, String regStr, String value) {
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.value = value;
        this.from = DataFrom.FROM_SERVER;
    }

    @Keep
    private void buildRegex() {
        String s = (!regStr.endsWith("%") ? regStr + "%" : regStr)
                .replace("%", RegUtils.INSTANCE.getREG_ALL_CHAR());
        regex = new Regex(s);
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    @Keep
    public MarkedData(Long id, @NotNull String key, String type, String regStr,
                      String value) {
        this.id = id;
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.value = value;
        buildRegex();
    }

    @Generated(hash = 1495649276)
    public MarkedData(Long id, @NotNull String key, String type, String regStr, String value,
            String from, String tagId) {
        this.id = id;
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.value = value;
        this.from = from;
        this.tagId = tagId;
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
        return "MarkedData{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", regStr='" + regStr + '\'' +
                ", regex=" + regex +
                ", value='" + value + '\'' +
                '}';
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
