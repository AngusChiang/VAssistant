package cn.vove7.common.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by Administrator on 9/26/2018
 */
public class InstSettingItem {
    @SerializedName(value = "type", alternate = {"t"})
    private String type;//checkbox switch, string ,int
    @SerializedName("default")
    private Object defaultValue;
    private String title;
    private String summary;
    private Integer[] range;//'1,10'
    private String[] items;//'1,10'

    public static final String TYPE_CHECK_BOX = "checkbox";
    public static final String TYPE_SWITCH = "switch";
    public static final String TYPE_TEXT = "string";
    public static final String TYPE_INT = "int";
    public static final String TYPE_SINGLE_CHOICE = "single_choice";

    @Override
    public String toString() {
        return "{" +
                "type='" + type + '\'' +
                ", defaultValue=" + defaultValue +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", range=" + Arrays.toString(range) +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer[] getRange() {
        return range;
    }

    public void setRange(Integer[] range) {
        this.range = range;
    }

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }
}
