package cn.vove7.executorengine.helper

import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.MatchedData
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

/**
 * # AdvanAppHelper
 * - 匹配App名
 * Created by Vove on 2018/6/15
 */
object AdvanAppHelper {
    //    companion object {
    //记录 pkg -> AppInfo
    private val APP_LIST = hashMapOf<String, AppInfo>()
    private var limitRate = 0.8f

    var lastUpdateTime = 0L
    const val updateInterval = 30 * 60 * 1000
//    }

    fun getAppInfo(pkg: String): AppInfo? {
        val s = APP_LIST[pkg]
        val now = System.currentTimeMillis()
        return if (s == null && (now - lastUpdateTime > updateInterval)) {
            lastUpdateTime = now
            updateAppList()
            APP_LIST[pkg]
        } else s
    }

    /**
     * appWord -> pkg
     * 匹配机制：标识 -> 按匹配率排序，若无匹配，更新app列表再次匹配 -> 搜索历史匹配
     */
    fun matchAppName(appWord: String, update: Boolean = true): List<MatchedData<AppInfo>> {
        val context = GlobalApp.APP
        val matchList = mutableListOf<MatchedData<AppInfo>>()
        APP_LIST.values.forEach {
            val rate = when {
                appWord.startsWith(it.name, ignoreCase = true) -> 1f
                else -> try {
                    Vog.v(this, "matchAppName $appWord ${it.name}")
                    TextHelper.compareSimilarityWithPinyin(context, appWord, it.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                    0f
                }
            }
            if (rate > limitRate) {
                matchList.add(MatchedData(rate, it))
            }
        }

        val now = System.currentTimeMillis()
        //未匹配到，距上次刷新时间，
        return if (matchList.size == 0 && (now - lastUpdateTime > updateInterval) && update) {
            lastUpdateTime = now
            updateAppList()
            matchAppName(appWord, false)
        } else {
            matchList.sort()
            matchList
        }
    }

    fun updateAppList() {
        val context = GlobalApp.APP
        Vog.v(this, "更新App列表")
        APP_LIST.clear()
        AppHelper.getAllInstallApp(context).forEach {
            APP_LIST[it.packageName] = it
        }
        Vog.v(this, "更新后 size: ${APP_LIST.size}")
    }

}