package cn.vove7.executorengine.helper

import android.content.Context
import cn.vove7.common.model.MatchedData
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

/**
 * # AppHelper
 * - 匹配App名
 * Created by Vove on 2018/6/15
 */
class AppHelper(private val context: Context) {
    companion object {
        //记录
        private val APP_LIST = mutableListOf<AppInfo>()
        private var limitRate = 0.8f

        var lastUpdateTime = 0L
        const val updateInterval = 30 * 60 * 1000
    }

    /**
     * appWord -> pkg
     * 匹配机制：标识 -> 按匹配率排序，若无匹配，更新app列表再次匹配 -> 搜索历史匹配
     */
    fun matchAppName(appWord: String, update: Boolean = true): List<MatchedData<AppInfo>> {

        val matchList = mutableListOf<MatchedData<AppInfo>>()
        APP_LIST.forEach {
            val rate = when {
                appWord.startsWith(it.name) -> 1f
                else -> try {
                    Vog.v(this, "matchAppName $appWord ${it.name}")
                    TextHelper.compareSimilarityWithPinyin(appWord, it.name)
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
        Vog.d(this, "更新App列表")
        APP_LIST.clear()
        APP_LIST.addAll(AppHelper.getAllInstallApp(context))
        Vog.d(this, "更新后 size: ${APP_LIST.size}")
    }

}