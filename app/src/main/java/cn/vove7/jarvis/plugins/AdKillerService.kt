package cn.vove7.jarvis.plugins

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.notifier.AppAdBlockNotifier
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * # AdKillerService
 * 去广告服务
 *
 * pkg Activity
 *  ↓
 * id text -> finder
 *
 * @author 17719247306
 * 2018/9/3
 */
object AdKillerService : AccPluginsService() {//TO-DO fixed 猪八戒ad
    /**
     * 缓存
     */
    private val finderCaches = ConcurrentHashMap<ActionScope, MutableSet<ViewFinder>>()

    override fun onBind() {//todo load data
        thread {
            locked = true
            finderCaches.clear()
            val appAdInfoDao = DAO.daoSession.appAdInfoDao
            val appAdInfos = appAdInfoDao.loadAll()
            appAdInfos.forEach {
                val scope = ActionScope(it.pkg, it.activity)
                if (finderCaches.containsKey(scope))
                    finderCaches[scope]!!.add(buildFinder(it))
                else {
                    finderCaches[scope] = mutableSetOf(buildFinder(it))
                }
            }
            Vog.d(this, "onBind ---> ${finderCaches.size}")
            locked = false
        }
    }

    var locked = false
    var changedTime = 0L

    /**
     * 只允许一个线程操作
     * @param root AccessibilityNodeInfo?
     */
    override fun onUiUpdate(root: AccessibilityNodeInfo?) {// 浪费资源..
        val now = System.currentTimeMillis()
        if (now - changedTime > (AppConfig.adWaitSecs * 1000)) return //7s等待时间
        if (locked) {
            Vog.v(this, "onUiUpdate ---> locked")
            return
        }
        locked = true
        if (finders?.isNotEmpty() == true) {
            val s = AppAdBlockNotifier(appInfo, finders!!)
            val b = s.notifyIfShow()
            when {
                b.first == 0 -> Vog.d(this, "onUiUpdate ---> 未发现广告")
                b.second == 0 -> Vog.d(this, "onUiUpdate ---> 发现广告，清除失败")
                else -> {
                    Vog.d(this, "onUiUpdate ---> 发现广告，清除成功")
                    if (AppConfig.isToastWhenRemoveAd)
                        GlobalApp.toastShort("已为你关闭广告")
                    finders = null
                }
            }
        }
        try {
            sleep(1000)
        } catch (e: Exception) {
        }
        locked = false
    }

    /**
     * 更新数剧时，刷新缓存
     */
    fun update(/*s: Array<String>*/) {
        finderCaches.clear()
        onBind()
    }

    private fun gcIfNeed() {
        Vog.d(this, "finderCaches.size ---> ${finderCaches.size}")
        if (finderCaches.size > 100) //TODO test
            for (i in 0..30)
                finderCaches.remove(finderCaches.keys.first())
    }

    private var finders: MutableSet<ViewFinder>? = null
    private var appInfo: AppInfo? = null

    override fun onAppChanged(appScope: ActionScope) {
        locked = false
        changedTime = System.currentTimeMillis()
        appInfo = AdvanAppHelper.getAppInfo(appScope.packageName)

//        finders = if (finderCaches.containsKey(appScope)) {
//            finderCaches[appScope]!!
//        } else null /*else buildAdFinders(appInfo, appScope)*/
        finders = null
        kotlin.run {
            finderCaches.forEach {
                if (appScope == it.key) {
                    finders = it.value
                    return@run
                }
            }
        }

        Vog.d(this, "当前界面广告数--->${finders?.size} $appScope $finders")

//        thread { gcIfNeed() }

        onUiUpdate(null)
    }

    /**
     * 根据App 界面 获取Finders
     * @param appInfo AppInfo?
     * @param appScope ActionScope
     * @return MutableSet<ViewFinder>
     */
    @Deprecated("onBind")
    private fun buildAdFinders(appInfo: AppInfo?, appScope: ActionScope): MutableSet<ViewFinder> {
        val finders = mutableSetOf<ViewFinder>()
        val queryBuilder = DAO.daoSession.appAdInfoDao.queryBuilder()
        val appAdInfos = queryBuilder
                .where(queryBuilder.and(AppAdInfoDao.Properties.Pkg.like(appScope.packageName),
                        queryBuilder.or(AppAdInfoDao.Properties.Activity.isNull,
                                AppAdInfoDao.Properties.Activity.eq(appScope.activity))
                )
                ).list()
        appAdInfos.forEach {
            finders.add(buildFinder(it))
        }
        finderCaches[appScope] = finders
        return finders
    }

    /**
     * buildFinder
     * @param it AppAdInfo
     * @return ViewFinder
     */
    private fun buildFinder(it: AppAdInfo): ViewFinder {//版本检查?
//        if (it.versionCode != null && appInfo != null && it.versionCode != appInfo.versionCode)
//            return@forEach//continue

        val finderBuilder = ViewFindBuilder()
        if (it.type != null)
            finderBuilder.types(it.type)
        val des = it.depthArr
        if (des != null) {
            finderBuilder.depths(des)
            return finderBuilder.viewFinderX
        }

        if (it.descs != null) {
            finderBuilder.desc(*it.descArray)
        }
        if (it.viewId != null)
            finderBuilder.id(it.viewId!!)
        if (it.textArray != null) {
            finderBuilder.containsText(*it.textArray)
        }

        return finderBuilder.viewFinderX
    }
}