package cn.vove7.executorengine.utils.app

import android.content.Context
import cn.vove7.parseengine.model.MatchedData
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.app.AppUtil
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
        val appMap = HashMap<String, AppInfo>()
        private val appList = mutableListOf<AppInfo>()
        private var limitRate = 0.8f

        var lastUpdateTime = 0L
        const val updateInterval = 30 * 60 * 1000
    }

    /**
     * appWord -> pkg
     * 匹配机制：按匹配率排序，若无匹配，更新app列表再次匹配
     */
    fun matchAppName(appWord: String, update: Boolean = true): List<MatchedData<AppInfo>> {
        val matchList = mutableListOf<MatchedData<AppInfo>>()
        appList.forEach {
            val rate = when (appWord) {
                it.name -> 1f
                else -> TextHelper.compareSimilarityWithPinyin(appWord, it.name)
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

    private fun updateAppList() {
        Vog.d(this, "更新App列表")
        appList.clear()
        val list = AppUtil.getAllInstallApp(context)
        val man = context.packageManager
        for (app in list) {
            appList.add(AppInfo(
                    name = app.loadLabel(man).toString(),
                    packageName = app.packageName,
                    icon = app.loadIcon(man))
            )
        }
        Vog.d(this, "更新后 size: ${appList.size}")
    }

}