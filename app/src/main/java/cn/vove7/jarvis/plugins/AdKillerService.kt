package cn.vove7.jarvis.plugins

import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.component.AbsAccPluginService
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.statusbar.RemoveAdAnimation
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
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
object AdKillerService : AbsAccPluginService() {

    private val removeAdAnimation: RemoveAdAnimation by lazy { RemoveAdAnimation() }
    /**
     * 缓存
     */
    private val finderCaches = ConcurrentHashMap<ActionScope, MutableSet<ViewFinder>>()

    override fun onUnBind() {
        GlobalLog.log("去广告服务下线")
    }

    override fun onBind() {
        GlobalLog.log("去广告服务上线")
        runOnPool {
            if (!AccessibilityApi.isBaseServiceOn) return@runOnPool
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
            Vog.d("AdOnBind ---> ${finderCaches.size}")
        }
    }

    private fun onSkipAd(node: ViewNode) {
        var clickResult = node.tryClick()
        if (!clickResult) {
            Vog.d("onSkipAd ---> 发现广告点击失败 使用globalClick")
            clickResult = node.globalClick()
        }
        Vog.i("Ad click ---> $clickResult")

        if (clickResult) {
            Vog.d("onSkipAd ---> 发现广告，清除成功")
            AppConfig.plusAdKillCount()//+1

            if (AppConfig.isToastWhenRemoveAd) {
                removeAdAnimation.begin()
                removeAdAnimation.hideDelay(3500)
            }
        } else
            Vog.d("onSkipAd ---> 发现广告，清除失败")
    }

    //搜索线程
    private val sthreads = mutableListOf<Thread>()

    /**
     * 使用标记数据查找
     */
    private fun startFind(appInfo: AppInfo?) {
        finders?.forEach {
            thread {
                try {
                    it.waitFor((AppConfig.adWaitSecs * 1000).toLong())?.also { node ->
                        Vog.i(" ${Thread.currentThread()} 发现广告 ---> ${appInfo?.name}  $it")
                        onSkipAd(node)
                    }
                } catch (e: Throwable) {
                    GlobalLog.err(e)
                }
            }.also { t ->
                synchronized(sthreads){
                    sthreads.add(t)
                }
            }
        }
    }

    /**
     * 更新数剧时，刷新缓存
     */
    fun update(/*s: Array<String>*/) {
        if (opened) {
            GlobalLog.log("ad cache update")
            onBind()
        }
    }

    fun gc() {
        Vog.d("gc finderCaches.size ---> ${finderCaches.size}")
        finderCaches.clear()
    }

    private var finders: MutableSet<ViewFinder>? = null
    //    private var appInfo: AppInfo? = null
    private var lastPkg = ""

    override fun onAppChanged(appScope: ActionScope) {
        if (!AppConfig.haveAdKillSurplus()) //用户去广告权限
            return
        val appInfo = AdvanAppHelper.getAppInfo(appScope.packageName)

//        finders = if (finderCaches.containsKey(appScope)) {
//            finderCaches[appScope]!!
//        } else null /*else buildAdFinders(appInfo, appScope)*/
        finders = null
        kotlin.run {
            finderCaches.forEach {
                if (appScope.equalsActivityNullable(it.key)) {
                    finders = it.value
                    return@run
                }
            }
        }
        Vog.v("当前界面广告数--->${finders?.size} $appScope $finders")
        if (finders?.isNotEmpty() == true)
            startFind(appInfo)
        else {//停止未结束的搜索
            stopSearchThreads()
            //smart skip ad
            if (AppConfig.smartKillAd && lastPkg != appScope.packageName
                    && AdvanAppHelper.getAppInfo(appScope.packageName)?.isUserApp == true)//切换页面
                smartSkipAppSwitchAd()
            else Vog.d("onAppChanged smartSkipApp ---> 系统应用")
        }
        lastPkg = appScope.packageName
    }

    //智能识别广告 页面缓存
//    val appAdCache by lazy { mutableListOf<ActionScope>() } //??

    /**
     * 当App切换时搜索 包含‘跳过’,‘skip’ 点击
     * 页面广告标记缓存? 一个页面5次未出现广告，标记无广告，出现过，必搜索。
     */
    @Synchronized
    private fun smartSkipAppSwitchAd() {
        Vog.i("smartSkipAppSwitchAd ---> 寻找App切换广告")
        synchronized(sthreads) {
            sthreads.add(thread {
                //寻找1.5s 频繁切换 耗电量?
                ViewFindBuilder()
                        .textLengthLimit(8)
                        .containsText("跳过", "skip")
                        .waitFor(1500)?.also {
                            onSkipAd(it)
                        } ?: Vog.i("smartSkipAppSwitchAd ---> 未发现广告")
            })
        }
    }

    /**
     * 关闭搜索线程
     */
    @Synchronized
    private fun stopSearchThreads() {
        synchronized(sthreads) {
            try {
                sthreads.forEach {
                    Vog.d("onAppChanged ---> 关闭搜索 $it")
                    it.interrupt()
                }
            } catch (e: Exception) {
            }
            sthreads.clear()
        }
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
                )).list()
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
        it.type.also {
            if (it != null)
                finderBuilder.type(it)
        }
        val des = it.depthArr
        if (des != null) {
            finderBuilder.depths(des)
            return finderBuilder.finder
        }

        if (it.descs != null) {
            finderBuilder.desc(*it.descArray)
        }
        if (it.viewId != null)
            finderBuilder.id(it.viewId!!)
        if (it.textArray != null) {
            finderBuilder.containsText(*it.textArray)
        }

        return finderBuilder.finder
    }
}