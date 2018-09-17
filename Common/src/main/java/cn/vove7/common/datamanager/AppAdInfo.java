package cn.vove7.common.datamanager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import java.io.Serializable;

import cn.vove7.common.interfaces.Markable;
import cn.vove7.common.netacc.tool.SignHelper;

/**
 * Created by 17719247306 on 2018/9/3
 */
@Entity
public class AppAdInfo  implements Serializable, Markable{
    static final long serialVersionUID = 111L;
    @Id
    private Long id;
    private String descTitle;

    private String pkg;
    private String activity;
    /**
     * 视图文本s split with ###
     */
    private String texts;
    /**
     * view id
     */
    private String viewId;
    /**
     * desc s split with ###
     */
    private String descs;
    private String depths;
    private String type;//class name

    private String tagId;
    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
    @Override
    public void sign() {
        setTagId(SignHelper.MD5(descTitle, pkg, activity, viewId, type, String.valueOf(versionCode), texts, descs, depths));
    }

    private Integer versionCode;//? 不需要 找不到就是找不到 . 消耗资源?

    public String getDescTitle() {
        return descTitle;
    }

    public void setDescTitle(String descTitle) {
        this.descTitle = descTitle;
    }

    @Generated(hash = 1361551302)
    public AppAdInfo() {
    }

    public AppAdInfo(String descTitle, String pkg, String activity, String depths, String type) {
        this.descTitle = descTitle;
        this.pkg = pkg;
        this.activity = activity;
        this.depths = depths;
        this.type = type;
    }

    public AppAdInfo(String descTitle, String pkg, String activity) {
        this.descTitle = descTitle;
        this.pkg = pkg;
        this.activity = activity;
    }

    public AppAdInfo(String descTitle, String pkg, String activity, String texts) {
        this.descTitle = descTitle;
        this.pkg = pkg;
        this.activity = activity;
        this.texts = texts;
    }

    @Keep
    public AppAdInfo(Long id, String descTitle, String pkg, String activity, String texts,
            String viewId, String descs, String depths, String type, String tagId,
            Integer versionCode) {
        this.id = id;
        this.descTitle = descTitle;
        this.pkg = pkg;
        this.activity = activity;
        this.texts = texts;
        this.viewId = viewId;
        this.descs = descs;
        this.depths = depths;
        this.type = type;
        setTagId(tagId);
        this.versionCode = versionCode;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getTexts() {
        return texts;
    }

    public void setTexts(String text) {
        this.texts = text;
    }

    public String getViewId() {
        return viewId;
    }

    public AppAdInfo setViewId(String viewId) {
        this.viewId = viewId;
        return this;
    }

    public String getDescs() {
        return descs;
    }

    public AppAdInfo setDescs(String descs) {
        this.descs = descs;
        return this;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepths() {
        return depths;
    }

    public Integer[] getDepthArr() {
        if (depths == null) return null;
        String[] is = depths.split(",");
        Integer[] si = new Integer[is.length];
        int i = 0;
        for (String s : is) {
            si[i++] = Integer.valueOf(s);
        }
        return si;
    }

    public AppAdInfo setDepths(String depths) {
        this.depths = depths;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public AppAdInfo setType(String type) {
        this.type = type;
        return this;
    }
}
