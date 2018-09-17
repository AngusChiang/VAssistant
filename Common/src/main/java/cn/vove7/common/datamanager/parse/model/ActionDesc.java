package cn.vove7.common.datamanager.parse.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator.
 * Date: 2018/9/17
 */
@Entity
public class ActionDesc {
    @Id
    private Long id;

    private String descText;

    private String example;

    public ActionDesc(String desc, String example) {
        this.descText = desc;
        this.example = example;
    }

    public ActionDesc() {
    }

    @Generated(hash = 130193740)
    public ActionDesc(Long id, String descText, String example) {
        this.id = id;
        this.descText = descText;
        this.example = example;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescText() {
        return descText;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
