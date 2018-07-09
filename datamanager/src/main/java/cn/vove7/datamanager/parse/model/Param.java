package cn.vove7.datamanager.parse.model;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Action参数
 */
@Entity
public class Param {
    @Id
    private Long id;
    @Transient
    private String value;
    private String desc;
    private String askText;
    /**
     * 参数何来
     */
    @Transient
    private int paramType = TYPE_GET;
    public static final int TYPE_WITH = 0;//自带
    public static final int TYPE_GET = 1;//获取

    @Generated(hash = 1996034722)
    public Param(Long id, String desc, String askText) {
        this.id = id;
        this.desc = desc;
        this.askText = askText;
    }

    public int getParamType() {
        return paramType;
    }

    public void setParamType(int paramType) {
        this.paramType = paramType;
    }

    public Param(String desc, String askText) {
        this.desc = desc;
        this.askText = askText;
    }

    public Param(String askText) {
        this.askText = askText;
    }

    @Generated(hash = 2002329870)
    public Param() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAskText() {
        return this.askText;
    }

    public void setAskText(String askText) {
        this.askText = askText;
    }
}