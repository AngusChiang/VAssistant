package cn.vove7.executorengine.helper

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.model.MatchedData
import cn.vove7.executorengine.parse.ParseEngine
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
    val APP_LIST = hashMapOf<String, AppInfo>()
    private var limitRate = 0.75f

    private var lastUpdateTime = 0L
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
     * 预解析跟随操作 ：  QQ扫一扫  QQ浏览器
     */
    val context: Context get() = GlobalApp.APP

    fun matchAppName(appWord: String, update: Boolean = true): List<MatchedData<AppInfo>> {
        val matchList = mutableListOf<MatchedData<AppInfo>>()
        APP_LIST.values.forEach {
            val rate = try {
                if (appWord.startsWith(it.name, ignoreCase = true)) {
                    val follow = appWord.substring(it.name.length)
                    Vog.d(this, "预解析---> $follow")
                    val aq = ParseEngine.matchAppAction(follow, ActionScope(it.packageName), false)
                    if (aq.second.isEmpty()) {//无匹配
                        Vog.d(this, "预解析---> 无匹配")
                        TextHelper.compareSimilarityWithPinyin(context, appWord, it.name, replaceNumberWithPinyin = true)
                    } else {//匹配ok
                        1f
                    }
                } else {
                    val f = TextHelper.compareSimilarityWithPinyin(context, appWord, it.name, replaceNumberWithPinyin = true)
                    Vog.v(this, "matchAppName $appWord ${it.name} $f")
                    f
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0f
            }

            if (rate >= limitRate) {
                matchList.add(MatchedData(rate, it))
            }
        }

        val now = System.currentTimeMillis()
        //未匹配到，距上次刷新时间，重新匹配
        return if (matchList.size == 0 && (now - lastUpdateTime > updateInterval) && update) {
            lastUpdateTime = now
            updateAppList()
            matchAppName(appWord, false)
        } else {
            matchList.sort()
            matchList
        }
    }

    /**
     * 更新AppList
     */
    fun updateAppList() {
        val context = GlobalApp.APP
        Vog.v(this, "更新App列表")
        APP_LIST.clear()
        AppHelper.getAllInstallApp(context).filter {
            context.packageManager.getLaunchIntentForPackage(it.packageName) != null
        }.forEach {
            APP_LIST[it.packageName] = it
        }
        Vog.v(this, "更新后 size: ${APP_LIST.size}")
    }

    fun getPkgList(): ArrayList<String> {
        val li = arrayListOf<String>()
        APP_LIST.forEach {
            li.add(it.value.packageName)
        }
        return li
    }

    fun getAppName(): Array<String> {
        if (APP_LIST.isEmpty()) updateAppList()
        val li = arrayListOf<String>()
        APP_LIST.forEach {
            li.add(it.value.name)
        }
        return li.toTypedArray()
    }

}