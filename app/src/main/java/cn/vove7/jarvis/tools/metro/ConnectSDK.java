package cn.vove7.jarvis.tools.metro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import cn.vove7.common.app.GlobalLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ConnectSDK {

    private static ConnectSDK mConnectSDK;
    private String App_Secret;
    private Context mContext;
    CommonParams mCommonParams;

    private boolean isInitialized = false;
    /* access modifiers changed from: private */
    public RequestCallback mRequestOpenNet;
    public int speedTestTime = 10;

    private ConnectSDK() {
    }

    public static ConnectSDK getInstance() {
        if (mConnectSDK == null) {
            synchronized (ConnectSDK.class) {
                if (mConnectSDK == null) {
                    mConnectSDK = new ConnectSDK();
                }
            }
        }
        return mConnectSDK;
    }

    @SuppressLint("WrongConstant")
    public boolean onInitialSDK(Context context, CommonParams commonParams) {
        if (context != null) {
            this.mContext = context;
            if (!this.isInitialized) {
                if (TextUtils.isEmpty(commonParams.getAppKey()) || TextUtils.isEmpty(commonParams.getAppSecret())) {
                    this.isInitialized = false;
                    return this.isInitialized;
                }
                this.isInitialized = true;
                this.App_Secret = commonParams.getAppSecret();
                this.mCommonParams = commonParams;
                SDKTools.getInstance().initSDKTools(context);
            }
        }
        return this.isInitialized;
    }

    OkHttpClient client = new OkHttpClient();

    private void requestServer(final int i, String url, Map<String, String> data) {
        FormBody.Builder fb = new FormBody.Builder();

        for (Map.Entry<String, String> d : data.entrySet()) {
            fb.add(d.getKey(), d.getValue());
        }

        okhttp3.Request req = new okhttp3.Request.Builder().url(url).post(fb.build())
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                GlobalLog.INSTANCE.log("开网失败 网络错误 " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
                JSONObject jSONObject;// .get();
                try {
                    jSONObject = new JSONObject(response.body().string());
                } catch (JSONException e) {
                    e.printStackTrace();
                    requestFailed(i, -101, "_", response);
                    return;
                }

                try {
                    int i2 = jSONObject.getJSONObject("_hdata").getInt("errcode");
                    if (i2 == 0) {
                        JSONObject jSONObject2 = jSONObject.getJSONObject("_ddata");
                        int i3 = jSONObject2.getInt("code");
                        if (i3 == 100) {
                            try {
                                if (jSONObject2.optString("data").length() > 0) {
                                    ConnectSDK.this.requestSuccess(i, jSONObject2.optString("data"), jSONObject2.optString("cityinfo"), response);
                                } else if (jSONObject2.optString("dlist").length() > 0) {
                                    ConnectSDK.this.requestSuccess(i, jSONObject2.optString("dlist"), jSONObject2.optString("cityinfo"), response);
                                } else {
                                    ConnectSDK.this.requestSuccess(i, "", jSONObject2.optString("cityinfo"), response);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (i3 == 102) {
                        } else if (i3 != 103) {
                            try {
                                ConnectSDK connectSDK = ConnectSDK.this;
                                connectSDK.requestFailed(i, -101, "_" + i2, response);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                //SDKTools.infoLog("http请求", "解析数据异常");
                            }
                        } else if (ConnectSDK.this.mRequestOpenNet != null) {
                            ConnectSDK.this.mRequestOpenNet.requestCallback(1003);
                        }
                    } else {
                        try {
                            ConnectSDK connectSDK2 = ConnectSDK.this;
                            connectSDK2.requestFailed(i, ResultCode.COMMON_ERROR, "_" + i2, response);
                        } catch (Exception e3) {
                            e3.printStackTrace();
                            SDKTools.infoLog("http请求", "解析数据异常");
                        }
                    }
                } catch (JSONException e4) {
                    try {
                        ConnectSDK.this.requestFailed(i, ResultCode.JSON_EXCEPTION, "", response);
                    } catch (Exception e5) {
                        e5.printStackTrace();
                        SDKTools.infoLog("http请求", "解析数据异常");
                    }
                    e4.printStackTrace();
                }

            }
        });

        SDKTools.infoLog("requestServer add queue");
    }


    @SuppressLint("WrongConstant")
    public void requestSuccess(int i, String str, String str2, Response response) {
        //SDKTools.infoLog(Constants.REQ_OPEN_NET_VALUE, Constants.OPEN_NET_SUCCESS_VALUE);
        @SuppressLint("WrongConstant") ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null && Build.VERSION.SDK_INT >= 23) {
            Network[] allNetworks = connectivityManager.getAllNetworks();
            int length = allNetworks.length;
            //SDKTools.infoLog("allNetworks.length: " + length);
            int i2 = 0;
            while (i2 < length) {
                Network network = allNetworks[i2];
                if (connectivityManager.getNetworkCapabilities(network).hasTransport(1)) {
                    connectivityManager.reportNetworkConnectivity(network, true);
                }
                i2++;
            }
        }
        requestLogUploadNow(Constants.OPEN_NET_SUCCESS_KEY, Constants.OPEN_NET_SUCCESS_VALUE);
        this.mRequestOpenNet.requestCallback(1001);
        GlobalLog.INSTANCE.log(Constants.OPEN_NET_SUCCESS_VALUE);
    }

    private void requestLogUploadNow(String key, String value) {
        SDKTools.infoLog("requestLogUploadNow: " + key + "  " + value);
    }

    public void requestFailed(int i, int i2, String str, okhttp3.Response response) {

        SDKTools.infoLog(response.toString());
        String str2;
        String str3;
        if (i == 1) {
            this.mRequestOpenNet.requestCallback(1002);
            if (i2 == -101) {
                str3 = Constants.OPEN_NET_DEVICE_FAILED_KEY;
                str2 = Constants.OPEN_NET_DEVICE_FAILED_VALUE;
            } else if (i2 == -301) {
                str3 = Constants.OPEN_NET_PROTOCOL_FAILED_KEY;
                str2 = Constants.OPEN_NET_PROTOCOL_FAILED_VALUE;
            } else {
                if (str.equals(ResultCode.REQUEST_TIME_OUT)) {
                    str3 = Constants.OPEN_NET_FAILED_TIMEOUT_KEY;
                    str2 = Constants.OPEN_NET_FAILED_TIMEOUT_VALUE;
                } else {
                    str3 = Constants.OPEN_NET_HTTP_FAILED_KEY;
                    str2 = Constants.OPEN_NET_HTTP_FAILED_VALUE + str;
                }
                //SDKTools.getInstance().openNetFailed();
            }
            SDKTools.infoLog(str3, str2);
            requestLogUploadNow(str3, str2);
            GlobalLog.INSTANCE.log("请求开网失败 " + str);
        }
    }


    @NonNull
    private Map<String, String> getRequest(JSONObject jSONObject) {
        Map<String, String> createJsonObjectRequest = new HashMap<>();// NoHttp.createJsonObjectRequest(str, RequestMethod.POST);
        String requestParams = getRequestParams(getCommonParams(this.mCommonParams), jSONObject);
        createJsonObjectRequest.put("_sdata", requestParams);
        createJsonObjectRequest.put("_sign", MD5Tool.encodeToMD5(requestParams + this.App_Secret));
        SDKTools.infoLog("buildRequest: " + createJsonObjectRequest.toString());
        return createJsonObjectRequest;
    }

    private JSONObject getCommonParams(CommonParams commonParams) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("sid", commonParams.getSid());
            jSONObject.put("appkey", commonParams.getAppKey());
            jSONObject.put("pname", "com.hskj.palmmetro");
            jSONObject.put("acode", "1.3.2");
            jSONObject.put("bcode", "16");
            jSONObject.put("mcode", Build.SERIAL);
            jSONObject.put("uuid", Build.ID);
            jSONObject.put("idfv", "");
            jSONObject.put("mac", SDKTools.getInstance().getMacAddress());
            jSONObject.put("imei", "");
            jSONObject.put("imsi", "");
            jSONObject.put("model", Build.MODEL != null ? Build.MODEL.trim().replaceAll("\\s*", "") : "");
            jSONObject.put("manufacture", "HUAWEI");
            jSONObject.put("system", "android" + Build.VERSION.RELEASE);
            jSONObject.put("longitude", commonParams.getLongitude());
            jSONObject.put("latitude", commonParams.getLatitude());
            jSONObject.put("altitude", commonParams.getAltitude());
            jSONObject.put("ostype", "1");
            jSONObject.put("uphone", commonParams.getPhone());
            jSONObject.put("ucity", commonParams.getCity());
            jSONObject.put("timestamp", (int) (System.currentTimeMillis() / 1000));
            jSONObject.put("noncestr", System.currentTimeMillis() + "000");
            jSONObject.put("resv", "");
            jSONObject.put("channo", commonParams.getChannel());
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return jSONObject;
    }

    private String getRequestParams(JSONObject jSONObject, JSONObject jSONObject2) {
        try {
            JSONObject jSONObject3 = new JSONObject();
            jSONObject3.put("_hdata", jSONObject);
            jSONObject3.put("_ddata", jSONObject2);
            return jSONObject3.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void openNet(String str, String phone, RequestCallback requestCallback) {
        openNet(true, str, phone, requestCallback);
    }

    public void openNet(boolean z, String str, String phone, RequestCallback requestCallback) {
        if (SDKTools.getInstance().isXiaoMi()) {
            if (SDKTools.getInstance().isXiaoMi() && SDKTools.getInstance().getCurrNetworkState() != 9) {
                wifiTranSport();
            }
            openNetArgs(z, str, phone, requestCallback);
        } else if (!SDKTools.getInstance().isMetroWiFi()) {
            requestCallback.requestCallback(ResultCode.IS_NOT_METRO_WIFI);
            SDKTools.infoLog("非地铁WiFi", "非地铁WiFi开网");
        } else if (SDKTools.getInstance().isXiaoMi() && SDKTools.getInstance().getCurrNetworkState() != 9) {
            wifiTranSport();
        }
        openNetArgs(z, str, phone, requestCallback);
    }

    private void openNetArgs(boolean z, String str, String phone, RequestCallback requestCallback) {

        this.mCommonParams.setPhone(phone);
        this.mCommonParams.setSid(1);
        if (z) {
            SDKTools.infoLog(Constants.REQ_OPEN_NET_KEY, Constants.REQ_OPEN_NET_VALUE);
        }
        if (requestCallback != null) {
            this.mRequestOpenNet = requestCallback;
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("nflag", 1);
                jSONObject.put("ugrade", str);
                jSONObject.put("ssid", SDKTools.getInstance().getWiFiSSID());
                if (z) {
                    requestServer(1, ServiceApi.NET_CONTROL, getRequest(jSONObject));
                } else {
                    requestServer(256, ServiceApi.NET_CONTROL, getRequest(jSONObject));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                SDKTools.errorLog("请求参数异常", e);
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void wifiTranSport() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        try {
            if (Build.VERSION.SDK_INT >= 21 && ContextCompat.checkSelfPermission(this.mContext, "android.permission.WRITE_SETTINGS") == 0 && connectivityManager != null) {
                try {
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addTransportType(1);
                    connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                        public void onAvailable(@NotNull Network network) {
                            try {
                                if (Build.VERSION.SDK_INT >= 23) {
                                    connectivityManager.bindProcessToNetwork(network);
                                } else {
                                    ConnectivityManager.setProcessDefaultNetwork(network);
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

}
