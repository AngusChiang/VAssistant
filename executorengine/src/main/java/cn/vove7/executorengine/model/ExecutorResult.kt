package cn.vove7.executorengine.model

/**
 *
 * 执行结果
 * Created by Vove on 2018/6/18
 */
data class ExecutorResult(
        val isSuccess: Boolean,
        val msg: String? = null,
        val wrongMsg: String? = msg
)