package cn.vove7.vtp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import cn.vove7.vtp.runtimepermission.PermissionUtils


/**
 *
 * 网络相关
 * Created by Vove on 2018/6/14
 */
object NetUtil {

    private val requirePermission = arrayOf(
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.INTERNET"
    )

    /**
     * 使用前请检查权限
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     * <uses-permission android:name="android.permission.INTERNET"/>
     * @return 网络是否可用
     */
    fun isNetworkAvailable(context: Context): Boolean {
        if(!PermissionUtils.isAllGranted(context, requirePermission)){
            throw Exception("无权限")
        }
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null && info.isConnected) {
            // 当前网络是连接的
            if (info.state == NetworkInfo.State.CONNECTED) {
                // 当前所连接的网络可用
                return true
            }
        }
        return false
    }
    /**
     * 获取当前网络状态
     */


    /**
     * 获取当前网络信息
     */
}