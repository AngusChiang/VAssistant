package cn.vove7.jarvis.tools.metro;

import java.io.Serializable;

public class TargetSSIDList implements Serializable {
    private String begin;
    private String contain;
    private String exact;

    public String getBegin() {
        return this.begin;
    }

    public void setBegin(String str) {
        this.begin = str;
    }

    public String getContain() {
        return this.contain;
    }

    public void setContain(String str) {
        this.contain = str;
    }

    public String getExact() {
        return this.exact;
    }

    public void setExact(String str) {
        this.exact = str;
    }

    public String toString() {
        return "TargetSSIDList{begin='" + this.begin + '\'' + ", contain='" + this.contain + '\'' + ", exact='" + this.exact + '\'' + '}';
    }
}
