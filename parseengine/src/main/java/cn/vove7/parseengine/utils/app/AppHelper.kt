package cn.vove7.parseengine.utils.app

import android.content.Context
import cn.vove7.parseengine.model.AppInfo
import cn.vove7.parseengine.model.MatchedApp
import cn.vove7.vtp.app.AppUtil
import cn.vove7.vtp.log.Vog

/**
 * # AppHelper
 * - 匹配App名
 * Created by Vove on 2018/6/15
 */
class AppHelper(val context: Context) {

    private val appList = mutableListOf<AppInfo>()
    private var limitRate = 0.7f
    /**
     * appWord -> pkg
     */

    fun matchAppName(appWord: String, update: Boolean = true): List<MatchedApp> {
        val matchList = mutableListOf<MatchedApp>()
        appList.forEach {
            val rate = shortTextSimilarity(appWord, it.name)
            if (rate > limitRate) {
                matchList.add(MatchedApp(rate, it))
            }
        }

        return if (matchList.size == 0 && update) {
            updateAppList()
            matchAppName(appWord, false)
        } else {
            matchList.sort()
            matchList
        }
    }

    private fun updateAppList() {
        Vog.d(this, "更新App列表")
        appList.clear()
        val list = AppUtil.getAllInstallApp(context)
        val man = context.packageManager
        for (app in list) {
            appList.add(AppInfo(app.loadLabel(man).toString(), app.packageName, app.loadIcon(man)))
        }
        Vog.d(this, "更新后 size: ${appList.size}")
    }

    /**
     * 短文本相似度
     */
    private fun shortTextSimilarity(appWord: String, appName: String): Float {
        return when {
            appWord == appName -> 1f
            appName.contains(appWord) -> 0.5f
            else -> 0f
        }
    }
}