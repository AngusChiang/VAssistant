package cn.vove7.executorengine

import android.content.Context
import cn.vove7.appbus.Bus
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.executor.entity.MarkedContact
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.MarkedOpen.TYPE_APP
import cn.vove7.datamanager.executor.entity.MarkedOpen.TYPE_SYS_FUN
import cn.vove7.datamanager.greendao.MarkedOpenDao
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.executorengine.bridge.*
import cn.vove7.executorengine.bridge.ShowDialogEvent.Companion.WHICH_SINGLE
import cn.vove7.executorengine.model.PartialResult
import cn.vove7.executorengine.utils.contact.ContactHelper
import cn.vove7.vtp.contact.ContactInfo
import cn.vove7.vtp.log.Vog
import kotlin.concurrent.thread

/**
 *
 *
 * Created by Vove on 2018/6/20
 */
open class Executor(
        val context: Context,
        val getAccessibilityBridge: GetAccessibilityBridge,
        private val systemBridge: SystemBridge,
        private val serviceBridge: ServiceBridge
) {
    var accessBridge: AccessibilityBridge? = null
    var lock = Object()
    var currentAction: Action? = null
    private val contactHelper = ContactHelper(context)
    val markedOpenDao = DAO.daoSession.markedOpenDao

    /**
     * 打开: 应用 -> 其他额外
     */
    fun openSomething(data: String): PartialResult {
        return if (systemBridge.openAppByWord(data)) {
            PartialResult(true)
        } else {//其他操作,打开网络,手电，网页
            parseOpenAction(data)
        }
    }

    /**
     * 解析打开动作
     */
    private fun parseOpenAction(p: String): PartialResult {
        val marked = markedOpenDao.queryBuilder().where(MarkedOpenDao.Properties.Key.like(p)).unique()
        return if (marked != null) {//通过Key查询到
            openByIdentifier(marked)
        } else {
            markedOpenDao.loadAll().forEach {
                if (it.regStr.toRegex().matches(p))
                    openByIdentifier(it)
            }
            PartialResult(false)
        }
    }

    /**
     * 解析标识符
     */
    private fun openByIdentifier(it: MarkedOpen): PartialResult {
        return when (it.type) {
            TYPE_APP -> {
                PartialResult(systemBridge.openAppByPkg(it.value))
            }
            TYPE_SYS_FUN -> {
                when (it.value) {
                    "openFlash" -> {
                        systemBridge.openFlashlight()
                    }
                    else -> {
                        PartialResult(false)
                    }
                }
            }
            else -> {
                Vog.d(this, "parseOpenAction -> 未知Type")
                PartialResult(false)
            }
        }
    }


    /**
     * 线程同步通知
     */

    fun notifySync() {
        synchronized(lock) {
            lock.notify()
        }
    }

    /**
     * 得到语音参数
     */
    fun onGetVoiceParam(param: String?) {
        //设置参数
        if (param == null) {
            currentAction?.responseResult = false
        } else {
            currentAction?.param?.value = param
        }
        //继续执行
        notifySync()
    }

    /**
     * 锁定线程
     */
    private fun waitFor() {
        synchronized(lock) {
            //等待结果
            try {
                lock.wait()
                Vog.d(this, "执行器-解锁")
            } catch (e: InterruptedException) {
                Vog.d(this, "被迫强行停止")
            }
        }
    }

    /**
     * 等待解锁
     */
    fun waitForParam(action: Action): PartialResult {
        serviceBridge.getVoiceParam(action)
        waitFor()
        //得到结果 -> action.param
        return if (!action.responseResult) {
            PartialResult(false, true, msg = "获取语音参数失败")
        } else
            PartialResult(true, msg = "等到参数重新执行")//重复执行
    }

    /**
     * 等待单选结果
     * @return if 调用成功 true else false
     */
    private fun waitForSingleChoice(askTitle: String, choiceData: List<ChoiceData>): Boolean {
        Bus.post(ShowDialogEvent(WHICH_SINGLE, currentAction!!, askTitle, choiceData))
        waitFor()
        return if (currentAction!!.responseResult) {
            Vog.d(this, "结果： have data")
            true
        } else {
            Vog.d(this, "结果： 取消")
            false
        }
    }

    /**
     * 返回操作
     */
    fun pressBack(): Boolean = accessBridge?.back() ?: false

    /**
     * 最近界面
     */
    fun openRecent(): Boolean = accessBridge?.recentInterface() ?: false

    fun goHome(): Boolean = accessBridge?.home() ?: false
    /**
     * 点击文本View
     */
    fun clickByText(text: String): PartialResult {
        val node = accessBridge?.findFirstNodeByText(text)
        return if (node != null)
            if (node.click()) PartialResult(true)
            else PartialResult(false, true, "点击失败")
        else PartialResult(false, true, "未找到该文本")
    }

    /**
     * 根据viewId点击View
     */
    fun clickById(id:String):PartialResult{
        val node = accessBridge?.findFirstNodeById(id)
        return if (node != null)
            if (node.click()) PartialResult(true)
            else PartialResult(false, true, "点击失败")
        else PartialResult(false, true, "未找到该id")
    }


    /**
     * 拨打
     */
    fun callPhone(s: String): PartialResult {
        val result = systemBridge.call(s)
        return if (!result.isSuccess) {
            //标识联系人
            if (waitForSingleChoice("选择要标识的联系人", contactHelper.getChoiceData())) {
                val bundle = currentAction!!.responseBundle
                val choiceData = bundle.getSerializable("data") as ChoiceData
                //开启线程
                thread {
                    val data = choiceData.originalData as ContactInfo
                    val marked = MarkedContact()
                    marked.key = s
                    marked.contactName = data.contactName
                    marked.phone = choiceData.subtitle
                    contactHelper.addMark(marked)//保存
                }
                systemBridge.call(choiceData.subtitle!!)
            } else {
                PartialResult(false, true, msg = "取消")
            }
        } else result
    }

    companion object {
        /**
         * @return 参数可用
         */
        fun checkParam(p: String?): Boolean = (p != null && p.trim() != "")

    }
}