package cn.vove7.common.datamanager.parse.model;


import java.io.Serializable;

/**
 * Action参数
 */

public class ActionParam implements Serializable {

    private String value;
    //private String desc;
    //private String askText;
    ///**
    // * 参数何来
    // */
    //@Transient
    //private int paramType = TYPE_GET;//UnUse
    //public static final int TYPE_WITH = 0;//自带
    //public static final int TYPE_GET = 1;//获取
    //
    //@Generated(hash = 1996034722)
    //public ActionParam(Long id, String desc, String askText) {
    //    this.id = id;
    //    this.desc = desc;
    //    this.askText = askText;
    //}

    //public int getParamType() {
    //    return paramType;
    //}
    //
    //public void setParamType(int paramType) {
    //    this.paramType = paramType;
    //}
    //
    //public ActionParam(String desc, String askText) {
    //    this.desc = desc;
    //    this.askText = askText;
    //}

    //public ActionParam(String askText) {
    //    this.askText = askText;
    //}

    //@Generated(hash = 2002329870)
    public ActionParam() {
    }

    //public Long getId() {
    //    return this.id;
    //}
    //
    //public void setId(Long id) {
    //    this.id = id;
    //}

    /**
     * 取后 清空值
     * 防止GreenDao缓存
     * @return value
     */

    public String getValueWithClear() {
        String s = this.value;
        this.value = null;
        return s;
    }
    public String getValue() {
        return this.value;
    }


    public void setValue(String value) {
        this.value = value;
    }

    //public String getDesc() {
    //    return this.desc;
    //}
    //
    //public void setDesc(String desc) {
    //    this.desc = desc;
    //}

    //public String getAskText() {
    //    return this.askText;
    //}

    //public void setAskText(String askText) {
    //    this.askText = askText;
    //}

    @Override
    public String toString() {
        return "ActionParam{" +
                "value='" + value + '\'' +
                //(desc == null ? "" : ", desc='" + desc + '\'') +
                //(askText == null ? "" : ", askText='" + askText + '\'') +
                //(paramType == null ? "" : ", paramType=" + paramType) +
                '}';
    }
}