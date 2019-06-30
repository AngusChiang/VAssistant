package cn.vove7.common.model;

/**
 * Created by Administrator.
 * Date: 2018/9/20
 */
public class VipPrice {
    private String durationText;
    private Double price = 999.0;

    public VipPrice(String durationText, Double price) {
        this.durationText = durationText;
        this.price = price;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
