package cn.vove7.common.datamanager.executor.entity;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.Objects;

import cn.vove7.common.BuildConfig;
import cn.vove7.common.datamanager.DAO;
import cn.vove7.common.datamanager.parse.DataFrom;
import cn.vove7.common.interfaces.Markable;
import cn.vove7.common.model.UserInfo;
import cn.vove7.common.netacc.tool.SecureHelper;
import cn.vove7.common.utils.RegUtils;
import kotlin.text.Regex;

/**
 * 标记数据
 * Created by Vove on 2018/6/23
 */
@Entity(indexes = {@Index(value = "key")})
public class MarkedData implements DataFrom, Markable, Serializable {

    //打开/关闭...
    public static final String MARKED_TYPE_APP = "open_app";//应用 value -> pkg
    public static final String MARKED_TYPE_SCRIPT_LUA = "open_script_lua";
    public static final String MARKED_TYPE_SCRIPT_JS = "open_script_js";
    //通讯录
    public static final String MARKED_TYPE_CONTACT = "contact";
    private static final long serialVersionUID = 1777230631945362504L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkedData that = (MarkedData) o;
        return Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId);
    }

    @Expose(serialize = false)
    @Id
    private Long id;
    @NotNull
    private String key;//alias
    private String type;
    /**
     * key的正则
     */
    private String regStr;
    private Long publishUserId;

    @Expose(serialize = false)
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

    public boolean belongUser(boolean update) {
        if (DataFrom.FROM_USER.equals(from)) return true;
        else if (publishUserId != null && publishUserId.equals(UserInfo.getUserId())) {
            if (update) {
                from = DataFrom.FROM_USER;
                DAO.INSTANCE.getDaoSession().getMarkedDataDao().update(this);
            }
            return true;
        }
        return false;
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
        this.from = DataFrom.FROM_USER;
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

    @Generated(hash = 285394506)
    public MarkedData(Long id, @NotNull String key, String type, String regStr,
                      Long publishUserId, String value, String from, String tagId) {
        this.id = id;
        this.key = key;
        this.type = type;
        this.regStr = regStr;
        this.publishUserId = publishUserId;
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

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        switch (type) {
            case MARKED_TYPE_APP: {
                builder.append("App别名：").append(key).append('\n');
                builder.append("包名：").append(value).append('\n');
                break;
            }
            case MARKED_TYPE_CONTACT: {
                builder.append("标记名：").append(key).append('\n');
                builder.append("手机号：").append(value).append('\n');
                break;
            }
            case MARKED_TYPE_SCRIPT_JS:
            case MARKED_TYPE_SCRIPT_LUA: {
                builder.append("打开：").append(key).append('\n');
                builder.append("类型：").append(translateType()).append('\n');
                builder.append("脚本：\n").append(value).append('\n');
            }
        }
        builder.append("正则：").append(regStr).append('\n');
        builder.append("来源：").append(DataFrom.Companion.translate(from)).append('\n');
        if (BuildConfig.DEBUG) {
            builder.append("tag：").append(tagId).append('\n');
        }
        return builder.toString();
    }

    public String translateType() {
        switch (type) {
            case MARKED_TYPE_SCRIPT_JS:
                return "JS";
            case MARKED_TYPE_SCRIPT_LUA:
                return "Lua";
            default:
                return "未知";
        }

    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getPublishUserId() {
        return this.publishUserId;
    }

    public void setPublishUserId(Long publishUserId) {
        this.publishUserId = publishUserId;
    }
}
