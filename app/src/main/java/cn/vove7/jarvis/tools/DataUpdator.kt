package cn.vove7.jarvis.tools

import android.app.Activity
import android.graphics.Color
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.OnUpdate
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.model.ResultBox
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.net.model.LastDateInfo
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.jarvis.R
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper

/**
 * # DataUpdator
 *
 * @author Administrator
 * 2018/10/15
 */
object DataUpdator {

    /**
     * 启动时检查更新
     * @param activity Activity
     */
    private var lastCheck = 0L

    fun checkUpdate(activity: Activity, back: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastCheck < 30 * 60 * 1000) {//防止频繁请求
            back.invoke()
            return
        }
        lastCheck = now

        WrapperNetHelper.postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE) {
            success { _, b ->
                val it = b.data
                if (b.isOk() && it != null) {
                    val v = check(it)
                    if (v.first.isNotEmpty()) {
                        notifyUpdate(activity, v.first, v.second, back)
                    } else {
                        back.invoke()
                    }
                }
                fail { _, e ->
                    GlobalLog.err(e)
                    Vog.d("checkDataUpdate ---> 检查数据失败")
                    back.invoke()
                }
            }
        }
    }

    /**
     * 弹出更新对话框，更新内容
     * @param activity Activity
     * @param needUpdateTypes List<Int>
     * @param s String
     */
    private fun notifyUpdate(activity: Activity, needUpdateTypes: List<Int>, s: String, back: () -> Unit) {
        if (activity.isFinishing) return
        oneKeyUpdate(activity, needUpdateTypes, back, s)
    }

    class Updater(val textDialog: ProgressTextDialog) : OnUpdate {
        override fun invoke(p1: Int, p2: String) {
            when (p1) {
                Color.GREEN ->
                    textDialog.appendlnGreen(p2)
                Color.RED -> {
                    textDialog.appendlnRed(p2)
                }
                Color.YELLOW ->
                    textDialog.appendlnAmber(p2)

                else ->
                    textDialog.appendln(p2)
            }
        }
    }

    /**
     * 根据type更新数据
     * @param types List<Int>
     */
    fun oneKeyUpdate(activity: Activity, types: List<Int>,
                     back: (() -> Unit)? = null, t: String) {
        val textDialog = ProgressTextDialog(activity, "正在更新", false, autoScroll = true)
        runOnPool {
            textDialog.appendlnBold("更新：\n$t\n")
            types.forEach {
                val result = ResultBox<Boolean>()
                val updater = Updater(textDialog)
                when (it) {
                    0 -> {
                        textDialog.appendlnGreen("正在获取：全局指令")
                        syncGlobalInst(updater) { b ->
                            result.setAndNotify(b)
                        }
                        if (result.blockedGet() == true) {
                            textDialog.appendlnGreen("成功")
                        } else {
                            textDialog.appendlnRed("失败,可至帮助进行反馈")
                        }
                    }
                    1 -> {
                        textDialog.appendlnGreen("正在获取：应用内指令")
                        syncInAppInst(updater) { b -> result.setAndNotify(b) }
                        if (result.blockedGet() == true) {
                            textDialog.appendlnGreen("成功")
                        } else {
                            textDialog.appendlnRed("失败,可至帮助进行反馈")
                        }
                    }
                    2, 3, 4 -> {//marked data
                        textDialog.appendlnGreen("正在获取：" + arrayOf("标记联系人", "标记应用", "标记功能")[it - 2])
                        syncMarkedData(updater, getTypes(it), markedLastKeyId[it - 2]) { b ->
                            result.setAndNotify(b)
                        }
                        if (result.blockedGet() == true) {
                            textDialog.appendlnGreen("成功")
                        } else {
                            textDialog.appendlnRed("失败,可至帮助进行反馈")
                        }
                    }
                    5 -> {//ad
                        textDialog.appendlnGreen("正在获取：标记广告")
                        syncMarkedAd(updater) { b -> result.setAndNotify(b) }
                        if (result.blockedGet() == true) {
                            textDialog.appendlnGreen("成功")
                        } else {
                            textDialog.appendlnRed("失败,可至帮助进行反馈")
                        }
                    }
                }
            }
            textDialog.appendlnGreen("更新完成")
            textDialog.finish()
            back?.invoke()
        }
    }


    private val markedLastKeyId = arrayOf(
            R.string.key_last_sync_marked_contact_date,
            R.string.key_last_sync_marked_app_date,
            R.string.key_last_sync_marked_open_date
    )

    private fun getTypes(t: Int): Array<String> {
        return when (t) {
            2 -> arrayOf(MarkedData.MARKED_TYPE_CONTACT)
            3 -> arrayOf(MarkedData.MARKED_TYPE_APP)
            4 -> arrayOf(MarkedData.MARKED_TYPE_SCRIPT_JS, MarkedData.MARKED_TYPE_SCRIPT_LUA)
            else -> arrayOf()
        }

    }

    private fun check(lastDateInfo: LastDateInfo): Pair<List<Int>, String> {
        val sp = SpHelper(GlobalApp.APP)
        val needUpdateTypes = mutableListOf<Int>()
        val needUpdateS = StringBuilder()
        arrayOf(arrayOf("全局指令", lastDateInfo.instGlobal, R.string.key_last_sync_global_date)//1
                , arrayOf("应用内指令", lastDateInfo.instInApp, R.string.key_last_sync_in_app_date)//2
                , arrayOf("标记通讯录", lastDateInfo.markedContact, R.string.key_last_sync_marked_contact_date)//3
                , arrayOf("标记应用", lastDateInfo.markedApp, R.string.key_last_sync_marked_app_date)//4
                , arrayOf("标记功能", lastDateInfo.markedOpen, R.string.key_last_sync_marked_open_date)//5
                , arrayOf("标记广告", lastDateInfo.markedAd, R.string.key_last_sync_marked_ad_date) //6
        ).withIndex().forEach { kv ->
            val it = kv.value
            val lastDate = it[1] as Long? ?: -1L
            val lastUpdate = sp.getLong(it[2] as Int)
            val isOutDate = lastDate > lastUpdate
            if (isOutDate) {
                needUpdateTypes.add(kv.index)
                needUpdateS.appendln(it[0])
            }
        }
        return Pair(needUpdateTypes, needUpdateS.toString())
    }


    /**
     * 同步全局命令
     * @param back (Boolean) -> Unit
     */
    fun syncGlobalInst(onUpdate: OnUpdate? = null, back: (Boolean) -> Unit) {
        WrapperNetHelper.postJson<List<ActionNode>>(ApiUrls.SYNC_GLOBAL_INST) {
            success { _, bean ->
                if (bean.isOk()) {
                    val list = bean.data
                    if (list != null) {
                        DaoHelper.updateGlobalInst(list, onUpdate).also {
                            if (it) {
                                SpHelper(GlobalApp.APP).set(R.string.key_last_sync_global_date, System.currentTimeMillis())
                                DAO.clear()
                                ParseEngine.updateGlobal()
                                back.invoke(true)
                            } else {
                                GlobalApp.toastError("同步失败")
                                back.invoke(false)
                            }
                        }
                    } else {
                        GlobalLog.err("code: GI57")
                        GlobalApp.toastError(R.string.text_error_occurred)
                        back.invoke(false)
                    }
                } else {
                    GlobalApp.toastError(R.string.text_net_err)
                    back.invoke(false)
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                GlobalApp.toastError(R.string.text_net_err)
                back.invoke(false)
            }
        }
    }

    /**
     * 同步App内指令
     * @param back (Boolean)->Unit
     */
    fun syncInAppInst(onUpdate: OnUpdate? = null, back: (Boolean) -> Unit) {
        WrapperNetHelper.postJson<List<ActionNode>>(ApiUrls.SYNC_IN_APP_INST,
                AdvanAppHelper.getPkgList()) {
            success { _, bean ->
                if (bean.isOk()) {
                    val list = bean.data
                    if (list != null) {
                        DaoHelper.updateInAppInst(list, onUpdate).also {
                            if (it) {
                                SpHelper(GlobalApp.APP).set(R.string.key_last_sync_in_app_date,
                                        System.currentTimeMillis())
                                DAO.clear()
                                ParseEngine.updateInApp()
                                back.invoke(true)
                            } else {
                                GlobalApp.toastError("同步失败")
                                back.invoke(false)
                            }
                        }
                    } else {
                        GlobalLog.err("code: GI57")
                        GlobalApp.toastError(R.string.text_error_occurred)
                        back.invoke(false)
                    }
                } else {
                    if(bean.code == 1) {
                        back.invoke(true)
                    } else {
                        GlobalApp.toastError(bean.message)
                        back.invoke(false)
                    }
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                back.invoke(false)
            }
        }
    }

    /**
     * 同步MarkedData
     * @param types Array<String>
     * @param lastKeyId Int
     * @param back (Boolean) -> Unit
     */
    fun syncMarkedData(onUpdate: OnUpdate? = null, types: Array<String>, lastKeyId: Int, back: (Boolean) -> Unit) {
        val syncData = TextHelper.arr2String(types)

        WrapperNetHelper.postJson<List<MarkedData>>(ApiUrls.SYNC_MARKED, syncData) {
            success { _, bean ->
                if (bean.isOk()) {
                    DaoHelper.updateMarkedData(onUpdate, types, bean.data ?: emptyList())
                    SpHelper(GlobalApp.APP).set(lastKeyId, System.currentTimeMillis())
                    DAO.clear()
                    back.invoke(true)
                } else {
                    GlobalApp.toastError(bean.message)
                    back.invoke(false)
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                back.invoke(false)
            }
        }
    }

    /**
     * 同步标记广告
     * @param back (Boolean) -> Unit
     */
    fun syncMarkedAd(onUpdate: OnUpdate? = null, back: (Boolean) -> Unit) {

        val syncPkgs = AdvanAppHelper.getPkgList()

        WrapperNetHelper.postJson<List<AppAdInfo>>(ApiUrls.SYNC_APP_AD, syncPkgs) {
            success { _, bean ->
                if (bean.isOk()) {
                    //
                    DaoHelper.updateAppAdInfo(bean.data ?: emptyList(), onUpdate)
                    SpHelper(GlobalApp.APP).set(R.string.key_last_sync_marked_ad_date, System.currentTimeMillis())
                    AdKillerService.update()
                    DAO.clear()
                    back.invoke(true)
                } else {
                    GlobalApp.toastInfo(bean.message)
                    back.invoke(false)
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                GlobalApp.toastInfo(e.message ?: "")
                back.invoke(false)
            }
        }
    }

}
