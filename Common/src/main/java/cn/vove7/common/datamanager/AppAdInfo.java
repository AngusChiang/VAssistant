package cn.vove7.common.datamanager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by 17719247306 on 2018/9/3
 */
@Entity
public class AppAdInfo {
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

    public AppAdInfo(String pkg, String activity, String descs) {
        this.pkg = pkg;
        this.activity = activity;
        this.descs = descs;
    }

    public AppAdInfo(String descTitle, String pkg, String activity, String texts) {
        this.descTitle = descTitle;
        this.pkg = pkg;
        this.activity = activity;
        this.texts = texts;
    }

    @Generated(hash = 1639516284)
    public AppAdInfo(Long id, String descTitle, String pkg, String activity,
            String texts, String viewId, String descs, Integer versionCode) {
        this.id = id;
        this.descTitle = descTitle;
        this.pkg = pkg;
        this.activity = activity;
        this.texts = texts;
        this.viewId = viewId;
        this.descs = descs;
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

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getDescs() {
        return descs;
    }

    public void setDescs(String descs) {
        this.descs = descs;
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
}
