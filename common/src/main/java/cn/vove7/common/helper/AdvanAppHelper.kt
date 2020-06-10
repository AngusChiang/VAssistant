package cn.vove7.common.helper

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.model.MatchedData
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
import cn.vove7.vtp.weaklazy.WeakLazy
import kotlin.reflect.jvm.isAccessible

/**
 * # AdvanAppHelper
 * - 匹配App名
 * Created by Vove on 2018/6/15
 */
object AdvanAppHelper {

    //记录 pkg -> AppInfo
    val ALL_APP_LIST = hashMapOf<String, AppInfo>()

    //    private val MARKED_APP_LIST = hashMapOf<String, AppInfo>()
    private var limitRate = 0.75f
    val context: Context get() = GlobalApp.APP

    /**
     * 标记应用列表
     */
    val MARKED_APP_PKG
        get() = DAO.daoSession.markedDataDao.queryBuilder()
                .where(MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_APP))
                .list().filter {
                    //过滤未安装
                    getAppInfo(it.value) != null
                }

    fun getAppInfo(pkg: String): AppInfo? = ALL_APP_LIST[pkg]

    /**
     * 刷新标记应用
     */
    fun initMarkedAppInfo() {

    }

    /**
     * name -> pkg
     * 匹配机制：标识 -> 按匹配率排序
     * 不进行预解析
     * @param excludeUnstartable 是否排除不可启动
     */
    fun matchPkgByName(name: String, excludeUnstartable: Boolean = true): List<MatchedData<AppInfo>> {
        val matchList = mutableListOf<MatchedData<AppInfo>>()
        //匹配别名
        MARKED_APP_PKG.forEach {
            val rate = if (it.rawRegex.matches(name)) {
                Vog.d("匹配成功 from 标记 ---> ${it.regStr} $name")
                0.9f
            } else 0f

            if (rate >= limitRate) {
                getAppInfo(it.value)?.also { info ->
                    matchList.add(MatchedData(rate, info))
                }
            }
        }
        synchronized(ALL_APP_LIST) {
            ALL_APP_LIST.values.forEach eachApp@{
                if (excludeUnstartable && !it.startable) return@eachApp
                val rate = try {
                    val appName = it.name ?: ""
                    if (appName.startsWith(name, ignoreCase = true)) {//计算概率
                        (name.length.toFloat() / appName.length).also { f ->
                            Vog.d("matchPkgByName startsWith ---> $f")
                        }
                    } else {
                        TextHelper.compareSimilarityWithPinyin(AdvanAppHelper.context, name,
                                appName, replaceNumberWithPinyin = true).also { f ->
                            Vog.v("matchAppName 拼音匹配 $name ${it.name} $f")
                        }
                    }
                } catch (e: Exception) {
                    GlobalLog.err(e.message) //记录
                    e.printStackTrace()
                    0f
                }
                //筛选
                if (rate >= limitRate) {
                    matchList.add(MatchedData(rate, it))
                }
            }
        }
        Vog.d("matchAppName ---> 匹配数 ${matchList.size}")
        matchList.sort()
        return matchList
    }

    /**
     * 更新AppList
     */
    private fun updateAppList() {
        val context = GlobalApp.APP
        Vog.v("更新App列表")
        synchronized(ALL_APP_LIST) {
            ALL_APP_LIST.clear()
            AppHelper.getAllInstallApp(context, false).forEach {
                ALL_APP_LIST[it.packageName] = it
            }
            GlobalLog.log("APP更新后 size: ${ALL_APP_LIST.size}")
        }
    }

    @Synchronized
    private fun checkAppList() {
        if (ALL_APP_LIST.isEmpty()) updateAppList()
    }

    fun getPkgList(): ArrayList<String> {
        checkAppList()
        val li = arrayListOf<String>()
        synchronized(ALL_APP_LIST) {
            ALL_APP_LIST.forEach {
                li.add(it.value.packageName)
            }
        }
        return li
    }

    fun getAppName(): Array<String> {
        checkAppList()
        val li = arrayListOf<String>()
        synchronized(ALL_APP_LIST) {
            ALL_APP_LIST.forEach {
                li.add(it.value.name ?: "")
            }
        }
        return li.toTypedArray()
    }

    /**
     * 当移除/卸载应用 删除缓存
     * @param pkg String
     */
    fun removeAppCache(pkg: String) {
        ALL_APP_LIST.remove(pkg)
    }

    /**
     * 当安装新应用 安装添加缓存
     * @param pkg String
     */
    fun addNewApp(pkg: String) {
        val appInfo = try {
            AppHelper.getAppInfo(context, "", pkg)
        } catch (e: Exception) {
            GlobalLog.err(e)
            return
        } ?: return
        ALL_APP_LIST[pkg] = appInfo
    }

    fun trimMem() {
        ALL_APP_LIST.forEach { (t, u) ->
            u::icon.let {
                it.isAccessible = true
                (it.getDelegate() as WeakLazy<*>).clearWeakValue()
            }
        }
    }

}

/**
 * 是否可启动
 * @receiver AppInfo
 */
val AppInfo.startable: Boolean
    get() = SystemBridge.getLaunchIntent(packageName) != null
