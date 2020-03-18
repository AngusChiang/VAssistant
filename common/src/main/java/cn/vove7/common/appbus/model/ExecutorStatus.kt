package cn.vove7.common.appbus.model

/**
 * # ExecutorStatus
 *
 * @author Vove
 * 2020/3/18
 */
data class ExecutorStatus(
        val what: Int,
        val data: Any?
) {
    companion object {
        fun begin(tag: String?): ExecutorStatus = ExecutorStatus(ON_EXECUTE_START, tag)
        fun finish(code: Int): Any = ExecutorStatus(ON_EXECUTE_FINISHED, code)

        const val ON_EXECUTE_START = 0
        const val ON_EXECUTE_FINISHED = 1
    }
}