package cn.vove7.jarvis.tools.metro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import cn.vove7.android.common.LoggerKt;

public class SDKTools {
    private static final String EAP = "EAP";
    public static final String METRO_WIFI_NAME = "地铁";
    private static final String OPEN = "[ESS]";
    public static final String VIP_WIFI_NAME = "黑卡";
    private static final String WEP = "WEP";
    private static final String WPA = "WPA";
    private static final String WPA2 = "WPA2";
    @SuppressLint("StaticFieldLeak")
    private static SDKTools mSDKTools;

    private boolean isPW = false;

    private ConnectivityManager mConnectivityManager;

    public Context mContext;

    private WifiAdmin mWifiAdmin;
    private WifiManager mWifiManager;
    private NetConfig netConfig;

    public static void errorLog(String str, Throwable th) {
        LoggerKt.loge(th, 1);
    }

    public static void infoLog(String str) {
        infoLog(str, "");
    }

    public static void infoLog(String str, String str2) {
        LoggerKt.logi("SDKTOOLS：  " + str + " " + str2, 1);
    }


    public static SDKTools getInstance() {
        if (mSDKTools == null) {
            synchronized (ConnectSDK.class) {
                if (mSDKTools == null) {
                    mSDKTools = new SDKTools();
                }
            }
        }
        return mSDKTools;
    }

    @SuppressLint("WrongConstant")
    public void initSDKTools(Context context) {
        //Logger.i("initSDKTools");
        try {
            this.mContext = context;
            this.mWifiAdmin = new WifiAdmin(context);
            //this.build = new WifiConnector(context);
            this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
            this.mWifiAdmin.startScan();
            this.netConfig = (NetConfig.getInstance());
            if (this.netConfig != null) {
                Constants.OUTER_NET = this.netConfig.getC_pingtest_host();
                try {
                    if (!TextUtils.isEmpty(this.netConfig.getC_statreach_num())) {
                        //this.arrivenDistance = Integer.parseInt(this.netConfig.getC_statreach_num());
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }


    public boolean isMetroWiFi() {
        try {
            if (this.mConnectivityManager != null) {
                NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == 1) {
                    String ssid2 = this.mWifiManager.getConnectionInfo().getSSID();
                    if (ssid2.contains("\"")) {
                        int indexOf = ssid2.indexOf("\"");
                        int lastIndexOf = ssid2.lastIndexOf("\"");
                        if (!(indexOf == -1 || lastIndexOf == -1)) {
                            ssid2 = ssid2.substring(indexOf + 1, lastIndexOf);
                        }
                    }
                    TargetSSIDList targetSSIDList2 = getTagetSSIDList();
                    if (targetSSIDList2 != null) {
                        String begin2 = targetSSIDList2.getBegin();
                        boolean isEmpty = TextUtils.isEmpty(targetSSIDList2.getBegin());
                        String contain2 = targetSSIDList2.getContain();
                        boolean isEmpty2 = TextUtils.isEmpty(targetSSIDList2.getContain());
                        String exact2 = targetSSIDList2.getExact();
                        boolean isEmpty3 = TextUtils.isEmpty(targetSSIDList2.getExact());
                        if (!isEmpty || !isEmpty2 || !isEmpty3) {
                            if (!isEmpty) {
                                this.isPW = isPW(ssid2, begin2);
                            }
                            if (!this.isPW && !isEmpty2) {
                                this.isPW = isPW(ssid2, contain2);
                            }
                            if (!this.isPW && !isEmpty3) {
                                this.isPW = isPW(ssid2, exact2);
                            }
                        } else {
                            int i = 0;
                            while (true) {
                                if (i >= Constants.DEFAULT_SSIDS.length) {
                                    break;
                                } else if (ssid2.startsWith(Constants.DEFAULT_SSIDS[i])) {
                                    this.isPW = true;
                                    break;
                                } else {
                                    i++;
                                }
                            }
                        }
                    } else if (ssid2.contains(METRO_WIFI_NAME) || ssid2.contains(VIP_WIFI_NAME)) {
                        this.isPW = true;
                    }
                }
                return this.isPW;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public TargetSSIDList getTagetSSIDList() {
        TargetSSIDList targetSSIDList = new TargetSSIDList();
        targetSSIDList.setBegin("花生地铁");
        targetSSIDList.setContain("花生地铁");
        targetSSIDList.setExact(NetConfig.getInstance().getC_wifi_metrolist());
        return targetSSIDList;
    }

    private boolean isPW(String str, String str2) {
        if (str2.contains("#")) {
            String[] split = str2.split("#");
            for (String trim : split) {
                if (str.startsWith(trim.trim())) {
                    return true;
                }
            }
            return false;
        } else if (str.startsWith(str2)) {
            return true;
        } else {
            return false;
        }
    }

    public String getBssid() {
        try {
            return this.mWifiManager.getConnectionInfo().getBSSID();
        } catch (Exception e) {
            e.printStackTrace();
            infoLog("获取bssid", "权限异常");
            return "";
        }
    }

    public String getWiFiSSID() {
        WifiAdmin wifiAdmin = this.mWifiAdmin;
        return wifiAdmin != null ? wifiAdmin.getConnectWifiSSID(this.mContext) : "";
    }


    public boolean isXiaoMi() {
        try {
            return Build.MANUFACTURER.equals(ResultCode.XIAOMI_MANUFACTURER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private int wifiEnabled() {
        try {
            if (this.mWifiManager.isWifiEnabled()) {
                return isXiaoMi() ? 10 : 4;
            }
            return 2;
        } catch (Exception unused) {
            return 0;
        }
    }

    public boolean isWifiEnabled() {
        try {
            return this.mWifiManager.isWifiEnabled();
        } catch (Exception unused) {
            return false;
        }
    }

    private int sercurityType(String str) {
        if (str.toUpperCase().equals(OPEN)) {
            return 0;
        }
        if (str.contains(WPA) || str.contains(WPA.toLowerCase())) {
            return 1;
        }
        if (str.contains(WEP) || str.contains(WEP.toLowerCase())) {
            return 2;
        }
        return (str.contains(EAP) || str.contains(EAP.toLowerCase())) ? 3 : 4;
    }


    public String getMacAddress() {
        return "00:00:00:00:00:00";
    }

    public int getRssid() {
        return this.mWifiAdmin.getRssid();
    }

    public int getCurrNetworkState() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            return 0;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return wifiEnabled();
        }
        if (!activeNetworkInfo.isConnected()) {
            return wifiEnabled();
        }
        int type = activeNetworkInfo.getType();
        if (1 == type) {
            return 9;
        }
        if (type != 0) {
            return wifiEnabled();
        }
        if (this.mWifiManager.isWifiEnabled()) {
            return isXiaoMi() ? 10 : 3;
        }
        return 1;
    }


}
