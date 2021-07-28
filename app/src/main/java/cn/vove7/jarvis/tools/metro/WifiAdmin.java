package cn.vove7.jarvis.tools.metro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WifiAdmin {


    private ConnectivityManager connManager;
    private ArrayList<ScanResult> m24GData;
    private ArrayList<ScanResult> m5GData;
    private ArrayList<ScanResult> mAllData;
    private Context mContext;

    private WifiInfo mWifiInfo;


    private WifiManager mWifiManager;

    @SuppressLint("WrongConstant")
    public WifiAdmin(Context context) {
        try {
            this.mWifiManager = (WifiManager) context.getSystemService("wifi");
            if (this.mWifiManager != null) {
                this.mWifiInfo = this.mWifiManager.getConnectionInfo();
            }
            this.connManager = (ConnectivityManager) context.getSystemService("connectivity");
            this.m24GData = new ArrayList<>();
            this.m5GData = new ArrayList<>();
            this.mAllData = new ArrayList<>();
            this.mContext = context;
        } catch (Exception e) {
            e.printStackTrace();
            SDKTools.infoLog("获取WifiAdmin", "权限异常");
        }
    }


    public void closeWifi() {
        if (this.mWifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiEnabled(false);
        }
    }


    public int checkState() {
        return this.mWifiManager.getWifiState();
    }


    public void startScan() {
        try {
            this.mWifiManager.startScan();
            this.m24GData.clear();
            this.m24GData = null;
            this.m24GData = new ArrayList<>();
            this.m5GData.clear();
            this.m5GData = null;
            this.m5GData = new ArrayList<>();
            this.mAllData.clear();
            this.mAllData = null;
            this.mAllData = new ArrayList<>();
            List<ScanResult> scanResults = this.mWifiManager.getScanResults();
            if (scanResults != null) {
                for (ScanResult next : scanResults) {
                    WifiManager.calculateSignalLevel(next.level, 100);
                    int i = next.frequency;
                    if (i <= 2400 || i >= 2500) {
                        this.m5GData.add(next);
                    } else {
                        this.m24GData.add(next);
                    }
                    this.mAllData.add(next);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SDKTools.infoLog("扫描WiFi", "权限异常");
        }
        //this.mWifiConfiguration = this.mWifiManager.getConfiguredNetworks();
    }


    public List<ScanResult> getAllWifiData() {
        startScan();
        return this.mAllData;
    }

    public boolean containName(List<ScanResult> list, ScanResult scanResult) {
        for (ScanResult next : list) {
            if (!TextUtils.isEmpty(next.SSID) && next.SSID.equals(scanResult.SSID) && next.capabilities.equals(scanResult.capabilities)) {
                return true;
            }
        }
        return false;
    }


    public String getBSSID() {
        WifiInfo wifiInfo = this.mWifiInfo;
        return wifiInfo == null ? "NULL" : wifiInfo.getBSSID();
    }


    public int getRssid() {
        WifiInfo connectionInfo;
        try {
            if (this.mWifiManager == null || (connectionInfo = this.mWifiManager.getConnectionInfo()) == null) {
                return 0;
            }
            return connectionInfo.getRssi();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getNetworkId() {
        WifiInfo wifiInfo = this.mWifiInfo;
        if (wifiInfo == null) {
            return 0;
        }
        return wifiInfo.getNetworkId();
    }


    @SuppressLint("WrongConstant")
    public String getConnectWifiSSID(Context context) {
        if (!((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1).isConnected()) {
            return "";
        }
        String ssid = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getSSID();
        return !TextUtils.isEmpty(ssid) ? ssid.substring(1, ssid.length() - 1) : ssid;
    }

    @SuppressLint("WrongConstant")
    public int getConnectWifiNetWorkID(Context context) {
        if (((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1).isConnected()) {
            return ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getNetworkId();
        }
        return -1;
    }

    @SuppressLint("WrongConstant")
    public boolean isWifiConnected(Context context, String str) {
        if (!((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1).isConnected() || !str.equals(((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getSSID())) {
            return false;
        }
        return true;
    }

    public boolean hasPwd(ScanResult scanResult) {
        return scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("PSK") || scanResult.capabilities.contains("EAP");
    }

    public WifiConfiguration isExsits(String str) {
        @SuppressLint("MissingPermission") List<WifiConfiguration> configuredNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configuredNetworks == null) {
            return null;
        }
        for (WifiConfiguration next : configuredNetworks) {
            String str2 = next.SSID;
            if (str2.equals("\"" + str + "\"")) {
                return next;
            }
        }
        return null;
    }

}
