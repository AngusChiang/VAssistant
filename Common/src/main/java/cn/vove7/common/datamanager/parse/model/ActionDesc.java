package cn.vove7.common.datamanager.parse.model;

import com.google.gson.annotations.Expose;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Administrator.
 * Date: 2018/9/17
 */
@Entity
public class ActionDesc implements Cloneable {
    @Expose(serialize = false)
    @Id
    private Long id;

    private String instructions;

    private String example;

    public ActionDesc(String desc, String example) {
        this.instructions = desc;
        this.example = example;
    }

    @Override
    public ActionDesc clone() throws CloneNotSupportedException {
        super.clone();
        ActionDesc desc = new ActionDesc();
        desc.example = example;
        desc.instructions = instructions;
        return desc;
    }

    public ActionDesc() {
    }

    @Generated(hash = 936804400)
    public ActionDesc(Long id, String instructions, String example) {
        this.id = id;
        this.instructions = instructions;
        this.example = example;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
