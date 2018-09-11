package cn.vove7.jarvis.plugins

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.notifier.AppAdBlockNotifier
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.R
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
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

    var locked = false

    var changedTime = 0L

    /**
     * 只允许一个线程操作
     * @param root AccessibilityNodeInfo?
     */
    override fun onUiUpdate(root: AccessibilityNodeInfo?) {// 浪费资源..
        val now = System.currentTimeMillis()
        if (now - changedTime > 7000) return //7s等待时间
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
                    if (SpHelper(GlobalApp.APP).getBoolean(R.string.key_show_toast_when_remove_ad, true))
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
     * 更新数剧库时，清空缓存
     */
    fun clearCache() {
        finderCaches.clear()
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

        finders = if (finderCaches.containsKey(appScope)) {
            finderCaches[appScope]!!
        } else buildAdFinders(appInfo, appScope)

        Vog.d(this, "当前界面广告数--->${finders?.size} $appScope $finders")

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
            if (it.type != null)
                finderBuilder.types(it.type)//TODO check it
            val des = it.depthArr
            if (des != null) {
                finderBuilder.depths(des)
                finders.add(finderBuilder.viewFinderX)
                return@forEach//continue
            }

            if (it.descs != null) {
                finderBuilder.desc(*it.descs.split("###").toTypedArray())
            }
            if (it.viewId != null)
                finderBuilder.id(it.viewId!!)
            if (it.texts != null) {
                finderBuilder.containsText(*it.texts.split("###").toTypedArray())
            }
            finders.add(finderBuilder.viewFinderX)
        }
        finderCaches[appScope] = finders
        return finders
    }
}