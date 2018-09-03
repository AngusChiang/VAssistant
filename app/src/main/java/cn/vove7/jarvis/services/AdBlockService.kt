package cn.vove7.jarvis.services

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.notifier.AppAdBlockNotifier
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * # AdBlockService
 * 去广告服务
 *
 * pkg Activity
 *  ↓
 * id text -> finder
 *
 * @author 17719247306
 * 2018/9/3
 */
object AdBlockService : AccPluginsService() {
    /**
     * 缓存
     */
    private val finderCaches = ConcurrentHashMap<ActionScope, MutableSet<ViewFinder>>()

    private var searchThread: Thread? = null
    override fun onUiUpdate(root: AccessibilityNodeInfo?) {
        searchThread?.interrupt()
        searchThread = Thread.currentThread()
        if (finders.isNotEmpty()) {
            val s = AppAdBlockNotifier(appInfo, finders)
            s.notifyIfShow()
        }
    }

    private fun gcIfNeed() {
        Vog.d(this, "finderCaches.size ---> ${finderCaches.size}")
        if (finderCaches.size > 100) //TODO test
            for (i in 0..30)
                finderCaches.remove(finderCaches.keys.first())
    }

    private var finders = mutableSetOf<ViewFinder>()
    private var appInfo: AppInfo? = null
    override fun onAppChanged(appScope: ActionScope) {
        appInfo = AdvanAppHelper.getAppInfo(appScope.packageName)

        finders = if (finderCaches.containsKey(appScope)) {
            finderCaches[appScope]!!
        } else buildAdFinders(appInfo, appScope)

        thread { gcIfNeed() }

        onUiUpdate(null)
    }

    /**
     * 根据App 界面 获取Finders
     * TODO caches??
     * @param appInfo AppInfo?
     * @param appScope ActionScope
     * @return MutableSet<ViewFinder>
     */
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
            if (it.versionCode != null && appInfo != null && it.versionCode != appInfo.versionCode)
                return@forEach//continue

            val finderBuilder = ViewFindBuilder()
            if (it.descs != null)
                finderBuilder.desc(*it.descs.split("###").toTypedArray())
            if (it.viewId != null)
                finderBuilder.id(it.viewId!!)
            if (it.texts != null) {
                finderBuilder.containsText(*it.texts.split("###").toTypedArray())
            }
            finders.add(finderBuilder.viewFinderX)
        }
        if (finders.isNotEmpty())
            finderCaches[appScope] = finders

        Vog.d(this, "当前界面广告数---> $appScope $finders")


        return finders
    }
}