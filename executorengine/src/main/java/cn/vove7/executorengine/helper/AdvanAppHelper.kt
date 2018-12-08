package cn.vove7.executorengine.helper

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.model.MatchedData
import cn.vove7.common.utils.isHomeApp
import cn.vove7.common.utils.isUserApp
import cn.vove7.executorengine.bridges.SystemBridge
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
    private val ALL_APP_LIST = hashMapOf<String, AppInfo>()
    private val MARKED_APP_LIST = hashMapOf<String, AppInfo>()
    private var limitRate = 0.75f
    val context: Context get() = GlobalApp.APP

//    private var lastUpdateTime = 0L
//    const val updateInterval = 30 * 60 * 1000
//    }

    fun getAppInfo(pkg: String): AppInfo? = ALL_APP_LIST[pkg]

    /**
     * 刷新标记应用
     */
    fun initMarkedAppInfo() {

    }

    /**
     * appWord -> pkg
     * 匹配机制：标识 -> 按匹配率排序，若无匹配，更新app列表再次匹配 -> 搜索历史匹配
     * 预解析跟随操作 ：  QQ扫一扫  QQ浏览器
     */
    fun matchAppName(appWord: String): List<MatchedData<AppInfo>> {
        val matchList = mutableListOf<MatchedData<AppInfo>>()
        //匹配别名
        DAO.daoSession.markedDataDao.queryBuilder()
                .where(MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_APP))
                .list().filter {
                    //过滤未安装
                    getAppInfo(it.value) != null
                }.forEach {
                    val rate = if (it.regex.matches(appWord)) {
                        Vog.d(this, "getPkgByWord match from marked ---> ${it.regStr} $appWord")
                        0.9f
                    } else 0f

                    if (rate >= limitRate) {
                        getAppInfo(it.value)?.also { info ->
                            matchList.add(MatchedData(rate, info))
                        }
                    }
                }
        synchronized(APP_LIST) {
            APP_LIST.values.forEach {
                val rate = try {
                    if (appWord.startsWith(it.name, ignoreCase = true)) {
                        val follow = appWord.substring(it.name.length)
                        Vog.d(this, "预解析---> $follow")
                        val aq = ParseEngine.matchAppAction(follow,
                                ActionScope(it.packageName), false)
                        if (aq.second.isEmpty()) {
                            TextHelper.compareSimilarityWithPinyin(context, appWord, it.name,
                                    replaceNumberWithPinyin = true).let { f ->
                                if (f < limitRate) limitRate
                                else f
                            }
                        } else //匹配ok
                            1f
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
        }
        Vog.d(this, "matchAppName ---> 匹配数 ${matchList.size}")
        matchList.sort()
        return matchList
    }

    /**
     * 更新AppList
     */
    fun updateAppList() {
        val context = GlobalApp.APP
        Vog.v(this, "更新App列表")
        synchronized(APP_LIST) {
            APP_LIST.clear()
            ALL_APP_LIST.clear()
            AppHelper.getAllInstallApp(context, false).filter {
                //过滤
                val f = it.isUserApp() || it.isHomeApp() || SystemBridge.getLaunchIntent(it.packageName) != null
                if (!f) {
                    ALL_APP_LIST[it.packageName] = it
                    Vog.d(this, "updateAppList ---> 过滤 ${it.name} ${it.packageName}")
                }
                f
            }.forEach {
                ALL_APP_LIST[it.packageName] = it
                APP_LIST[it.packageName] = it
            }
            Vog.v(this, "更新后 size: ${APP_LIST.size}")
        }
    }

    fun getPkgList(): ArrayList<String> {
        if (APP_LIST.isEmpty()) updateAppList()
        val li = arrayListOf<String>()
        synchronized(APP_LIST) {
            APP_LIST.forEach {
                li.add(it.value.packageName)
            }
        }
        return li
    }

    fun getAppName(): Array<String> {
        if (APP_LIST.isEmpty()) updateAppList()
        val li = arrayListOf<String>()
        synchronized(APP_LIST) {
            APP_LIST.forEach {
                li.add(it.value.name)
            }
        }
        return li.toTypedArray()
    }

}