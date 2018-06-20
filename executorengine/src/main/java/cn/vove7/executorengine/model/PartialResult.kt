package cn.vove7.executorengine.model

/**
 *
 *
 * Created by Vove on 2018/6/18
 */
data class PartialResult(val isSuccess: Boolean,
                         val needTerminal: Boolean = false,
                         val msg: String = "",
                         val repeat: Boolean = false)