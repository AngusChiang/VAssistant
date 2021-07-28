package cn.vove7.jarvis.tools.metro;

import java.io.Serializable;

public class NetConfig implements Serializable {
    final static long serialVersionUID = 6553620582728367464L;

    private String c_pingtest_host;
    private String c_pingtest_hostlist;
    private String c_speedtest_time;
    private String c_speedtest_url;
    private String c_statreach_num;
    private String c_systimestamp;
    private String c_wifi_metrolist;
    private String m_c_ping_count;
    private String m_c_ping_lost_threshold;
    private String m_c_ping_time_threshold;
    private String m_c_sampling_max_count;
    private String m_c_sampling_nini_interval;
    private String m_c_switch;
    private String m_f_calc_byte_threshold;
    private String m_f_sampling_max_count;
    private String m_f_sampling_nini_interval;
    private String m_f_switch;
    private String m_s_calc_connect_threshold;
    private String m_s_calc_strenth_threshold_android;
    private String m_s_calc_strenth_threshold_ios;
    private String m_s_sampling_max_count;
    private String m_s_sampling_mini_interval;
    private String m_s_switch;

    public String getM_s_switch() {
        return this.m_s_switch;
    }

    public void setM_s_switch(String str) {
        this.m_s_switch = str;
    }

    public String getM_s_sampling_mini_interval() {
        return this.m_s_sampling_mini_interval;
    }

    public void setM_s_sampling_mini_interval(String str) {
        this.m_s_sampling_mini_interval = str;
    }

    public String getM_s_sampling_max_count() {
        return this.m_s_sampling_max_count;
    }

    public void setM_s_sampling_max_count(String str) {
        this.m_s_sampling_max_count = str;
    }

    public String getM_s_calc_strenth_threshold_ios() {
        return this.m_s_calc_strenth_threshold_ios;
    }

    public void setM_s_calc_strenth_threshold_ios(String str) {
        this.m_s_calc_strenth_threshold_ios = str;
    }

    public String getM_s_calc_strenth_threshold_android() {
        return this.m_s_calc_strenth_threshold_android;
    }

    public void setM_s_calc_strenth_threshold_android(String str) {
        this.m_s_calc_strenth_threshold_android = str;
    }

    public String getM_s_calc_connect_threshold() {
        return this.m_s_calc_connect_threshold;
    }

    public void setM_s_calc_connect_threshold(String str) {
        this.m_s_calc_connect_threshold = str;
    }

    public String getM_f_switch() {
        return this.m_f_switch;
    }

    public void setM_f_switch(String str) {
        this.m_f_switch = str;
    }

    public String getM_f_sampling_nini_interval() {
        return this.m_f_sampling_nini_interval;
    }

    public void setM_f_sampling_nini_interval(String str) {
        this.m_f_sampling_nini_interval = str;
    }

    public String getM_f_sampling_max_count() {
        return this.m_f_sampling_max_count;
    }

    public void setM_f_sampling_max_count(String str) {
        this.m_f_sampling_max_count = str;
    }

    public String getM_f_calc_byte_threshold() {
        return this.m_f_calc_byte_threshold;
    }

    public void setM_f_calc_byte_threshold(String str) {
        this.m_f_calc_byte_threshold = str;
    }

    public String getM_c_switch() {
        return this.m_c_switch;
    }

    public void setM_c_switch(String str) {
        this.m_c_switch = str;
    }

    public String getM_c_sampling_nini_interval() {
        return this.m_c_sampling_nini_interval;
    }

    public void setM_c_sampling_nini_interval(String str) {
        this.m_c_sampling_nini_interval = str;
    }

    public String getM_c_sampling_max_count() {
        return this.m_c_sampling_max_count;
    }

    public void setM_c_sampling_max_count(String str) {
        this.m_c_sampling_max_count = str;
    }

    public String getM_c_ping_count() {
        return this.m_c_ping_count;
    }

    public void setM_c_ping_count(String str) {
        this.m_c_ping_count = str;
    }

    public String getM_c_ping_lost_threshold() {
        return this.m_c_ping_lost_threshold;
    }

    public void setM_c_ping_lost_threshold(String str) {
        this.m_c_ping_lost_threshold = str;
    }

    public String getM_c_ping_time_threshold() {
        return this.m_c_ping_time_threshold;
    }

    public void setM_c_ping_time_threshold(String str) {
        this.m_c_ping_time_threshold = str;
    }

    public String getC_speedtest_url() {
        return this.c_speedtest_url;
    }

    public void setC_speedtest_url(String str) {
        this.c_speedtest_url = str;
    }

    public String getC_speedtest_time() {
        return this.c_speedtest_time;
    }

    public void setC_speedtest_time(String str) {
        this.c_speedtest_time = str;
    }

    public String getC_pingtest_host() {
        return this.c_pingtest_host;
    }

    public void setC_pingtest_host(String str) {
        this.c_pingtest_host = str;
    }

    public String getC_systimestamp() {
        return this.c_systimestamp;
    }

    public void setC_systimestamp(String str) {
        this.c_systimestamp = str;
    }

    public String getC_statreach_num() {
        return this.c_statreach_num;
    }

    public void setC_statreach_num(String str) {
        this.c_statreach_num = str;
    }

    public String getC_pingtest_hostlist() {
        return this.c_pingtest_hostlist;
    }

    public void setC_pingtest_hostlist(String str) {
        this.c_pingtest_hostlist = str;
    }

    public String getC_wifi_metrolist() {
        return this.c_wifi_metrolist;
    }

    public void setC_wifi_metrolist(String str) {
        this.c_wifi_metrolist = str;
    }

    public NetConfig(String c_pingtest_host, String c_pingtest_hostlist, String c_speedtest_time, String c_speedtest_url, String c_statreach_num, String c_systimestamp, String c_wifi_metrolist, String m_c_ping_count, String m_c_ping_lost_threshold, String m_c_ping_time_threshold, String m_c_sampling_max_count, String m_c_sampling_nini_interval, String m_c_switch, String m_f_calc_byte_threshold, String m_f_sampling_max_count, String m_f_sampling_nini_interval, String m_f_switch, String m_s_calc_connect_threshold, String m_s_calc_strenth_threshold_android, String m_s_calc_strenth_threshold_ios, String m_s_sampling_max_count, String m_s_sampling_mini_interval, String m_s_switch) {
        this.c_pingtest_host = c_pingtest_host;
        this.c_pingtest_hostlist = c_pingtest_hostlist;
        this.c_speedtest_time = c_speedtest_time;
        this.c_speedtest_url = c_speedtest_url;
        this.c_statreach_num = c_statreach_num;
        this.c_systimestamp = c_systimestamp;
        this.c_wifi_metrolist = c_wifi_metrolist;
        this.m_c_ping_count = m_c_ping_count;
        this.m_c_ping_lost_threshold = m_c_ping_lost_threshold;
        this.m_c_ping_time_threshold = m_c_ping_time_threshold;
        this.m_c_sampling_max_count = m_c_sampling_max_count;
        this.m_c_sampling_nini_interval = m_c_sampling_nini_interval;
        this.m_c_switch = m_c_switch;
        this.m_f_calc_byte_threshold = m_f_calc_byte_threshold;
        this.m_f_sampling_max_count = m_f_sampling_max_count;
        this.m_f_sampling_nini_interval = m_f_sampling_nini_interval;
        this.m_f_switch = m_f_switch;
        this.m_s_calc_connect_threshold = m_s_calc_connect_threshold;
        this.m_s_calc_strenth_threshold_android = m_s_calc_strenth_threshold_android;
        this.m_s_calc_strenth_threshold_ios = m_s_calc_strenth_threshold_ios;
        this.m_s_sampling_max_count = m_s_sampling_max_count;
        this.m_s_sampling_mini_interval = m_s_sampling_mini_interval;
        this.m_s_switch = m_s_switch;
    }

    public static NetConfig getInstance() {

        return new NetConfig(
                "180.76.76.76",
                "180.76.76.76#180.76.76.76",
                "10",
                "http://cesu.wifi8.com/download/QQDoctor.apk",
                "0",
                "1605018265",
                "花生地铁WiFi#广州地铁WiFi#上海地铁WiFi#武汉地铁WiFi#MyWifiTestgz#花生黑卡专享WiFi#黑卡专享#贵阳地铁WiFi#Xiaomi_803",
                "2",
                "0.7",
                "500",
                "4",
                "30",
                "0",
                "50",
                "5",
                "10",
                "0",
                "0.7",
                "-75",
                "0.5",
                "3",
                "15",
                "0"
        );

    }
    @Override
    public String toString() {
        return "NetConfig{" +
                "\nc_pingtest_host='" + c_pingtest_host + '\'' +
                ",\n c_pingtest_hostlist='" + c_pingtest_hostlist + '\'' +
                ",\n c_speedtest_time='" + c_speedtest_time + '\'' +
                ",\n c_speedtest_url='" + c_speedtest_url + '\'' +
                ",\n c_statreach_num='" + c_statreach_num + '\'' +
                ",\n c_systimestamp='" + c_systimestamp + '\'' +
                ",\n c_wifi_metrolist='" + c_wifi_metrolist + '\'' +
                ",\n m_c_ping_count='" + m_c_ping_count + '\'' +
                ",\n m_c_ping_lost_threshold='" + m_c_ping_lost_threshold + '\'' +
                ",\n m_c_ping_time_threshold='" + m_c_ping_time_threshold + '\'' +
                ",\n m_c_sampling_max_count='" + m_c_sampling_max_count + '\'' +
                ",\n m_c_sampling_nini_interval='" + m_c_sampling_nini_interval + '\'' +
                ",\n m_c_switch='" + m_c_switch + '\'' +
                ",\n m_f_calc_byte_threshold='" + m_f_calc_byte_threshold + '\'' +
                ",\n m_f_sampling_max_count='" + m_f_sampling_max_count + '\'' +
                ",\n m_f_sampling_nini_interval='" + m_f_sampling_nini_interval + '\'' +
                ",\n m_f_switch='" + m_f_switch + '\'' +
                ",\n m_s_calc_connect_threshold='" + m_s_calc_connect_threshold + '\'' +
                ",\n m_s_calc_strenth_threshold_android='" + m_s_calc_strenth_threshold_android + '\'' +
                ",\n m_s_calc_strenth_threshold_ios='" + m_s_calc_strenth_threshold_ios + '\'' +
                ",\n m_s_sampling_max_count='" + m_s_sampling_max_count + '\'' +
                ",\n m_s_sampling_mini_interval='" + m_s_sampling_mini_interval + '\'' +
                ",\n m_s_switch='" + m_s_switch + '\'' +
                "\n}";
    }
}
