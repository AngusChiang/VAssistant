package cn.vove7.vtp.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager

/**
 *
 *
 * Created by Vove on 2018/6/24
 */
object SystemHelper {
    /**
     * 获取剪切板最新内容
     */
    fun getClipBoardContent(context: Context): CharSequence {
        val data = getClipData(context)
        return data.getItemAt(data.itemCount - 1).text
    }

    /**
     * 获取剪切板数据
     * @return ClipData
     */

    fun getClipData(context: Context): ClipData {
        val cs = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return cs.primaryClip
    }

    /**
     * @return 亮屏 true else false
     */
    fun isScreenOn(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }

    /**
     * 打开链接
     */
    private fun openLinkBySystem(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    /**
     * 打开应用商店
     */
    fun openApplicationMarket(context: Context, packageName: String) {
        try {
            val str = "market://details?id=$packageName"
            val localIntent = Intent(Intent.ACTION_VIEW)
            localIntent.data = Uri.parse(str)
            context.startActivity(localIntent)
        } catch (e: Exception) {
            // 打开应用商店失败 可能是没有手机没有安装应用市场
            e.printStackTrace()
            // 调用系统浏览器进入商城
            val url = COOLAPK_URL + packageName
            openLinkBySystem(context, url)
        }

    }

    private const val COOLAPK_URL = "https://www.coolapk.com/apk/"
}