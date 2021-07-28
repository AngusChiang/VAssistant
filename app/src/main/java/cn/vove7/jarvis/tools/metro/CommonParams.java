package cn.vove7.jarvis.tools.metro;

public class CommonParams {
    private String altitude;
    private String appKey;
    private String appSecret;
    private String channel;
    private String city;
    private String latitude;
    private String longitude;
    private String phone;
    private int sid;

    private CommonParams() {
        this.city = "";
        this.longitude = "0";
        this.latitude = "0";
        this.altitude = "0";
        this.phone = "";
        this.sid = 0;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public String getAppSecret() {
        return this.appSecret;
    }

    public String getCity() {
        return this.city;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getAltitude() {
        return this.altitude;
    }

    public void setPhone(String str) {
        this.phone = str;
    }

    public String getPhone() {
        return this.phone;
    }

    public int getSid() {
        return this.sid;
    }

    public void setSid(int i) {
        this.sid = i;
    }

    public void setLongitude(String str) {
        this.longitude = str;
    }

    public void setLatitude(String str) {
        this.latitude = str;
    }

    public void setAltitude(String str) {
        this.altitude = str;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String str) {
        this.channel = str;
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public String altitude = "0";
        /* access modifiers changed from: private */
        public String appKey;
        /* access modifiers changed from: private */
        public String appSecret;
        /* access modifiers changed from: private */
        public String channel;
        /* access modifiers changed from: private */
        public String city = "";
        /* access modifiers changed from: private */
        public String latitude = "0";
        /* access modifiers changed from: private */
        public String longitude = "0";
        /* access modifiers changed from: private */
        public String phone = "0";
        /* access modifiers changed from: private */
        public int sid = 0;

        public Builder(String str, String str2) {
            this.appKey = str;
            this.appSecret = str2;
        }

        public Builder phone(String str) {
            this.phone = str;
            return this;
        }

        public Builder city(String str) {
            this.city = str;
            return this;
        }

        public Builder lot(double d) {
            this.longitude = String.valueOf(d);
            return this;
        }

        public Builder lat(double d) {
            this.latitude = String.valueOf(d);
            return this;
        }

        public Builder alt(double d) {
            this.altitude = String.valueOf(d);
            return this;
        }

        public Builder sid(int i) {
            this.sid = i;
            return this;
        }

        public Builder channel(String str) {
            this.channel = str;
            return this;
        }

        public CommonParams build() {
            return new CommonParams(this);
        }
    }

    private CommonParams(Builder builder) {
        this.city = "";
        this.longitude = "0";
        this.latitude = "0";
        this.altitude = "0";
        this.phone = "";
        this.sid = 0;
        this.appKey = builder.appKey;
        this.appSecret = builder.appSecret;
        this.channel = builder.channel;
        this.phone = builder.phone;
        this.city = builder.city;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.altitude = builder.altitude;
        this.sid = builder.sid;
    }
}
