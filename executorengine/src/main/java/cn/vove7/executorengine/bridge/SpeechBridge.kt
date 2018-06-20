package cn.vove7.executorengine.bridge

import cn.vove7.parseengine.model.Action

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
interface SpeechBridge {
    /**
     * 中途获取未知参数
     */
    fun getUnsetParam(action: Action)

}