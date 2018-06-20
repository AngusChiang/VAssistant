package cn.vove7.executorengine

import cn.vove7.executorengine.bridge.SpeechBridge
import cn.vove7.executorengine.bridge.SystemBridge
import cn.vove7.executorengine.model.PartialResult
import cn.vove7.parseengine.model.Action
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by Vove on 2018/6/20
 */
open class Executor(
        val getAccessibilityBridge: GetAccessibilityBridge,
        private val systemBridge: SystemBridge,
        private val speechBridge: SpeechBridge
) {
    var lock: Object? = null
    /**
     * 打开: 应用 -> 其他额外
     */
    fun openSomething(action: Action, data: String?): PartialResult {
        if (checkParam(data)) {
            if (systemBridge.openAppByWord(data!!)) {
                return PartialResult(true)
            } else {//其他操作,打开网络,手电，网页
                parseOpenAction(data)

            }
        } else {//询问参数
            speechBridge.getUnsetParam(action)
            synchronized(lock!!) {
                //等待结果
                try {
                    lock!!.wait()
                } catch (e: InterruptedException) {
                    Vog.d(this, "被迫强行停止")
                }
                return if (!action.voiceOk) {
                    PartialResult(false, true, msg = "获取语音参数失败")
                } else
                    PartialResult(false, msg = "等到参数重新执行", repeat = true)
            }
        }
        Vog.d(this, "openSomething failed -> ${action.actionScript} $data")
        return PartialResult(false, msg = "执行失败")
    }


    companion object {
        /**
         * @return 参数可用
         */
        fun checkParam(p: String?): Boolean {
            return (p != null && p != "")
        }

        /**
         * 解析打开动作
         */
        fun parseOpenAction(p: String) {


        }
    }
}