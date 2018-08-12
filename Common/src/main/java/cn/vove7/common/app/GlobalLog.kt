package cn.vove7.common.app

import cn.vove7.vtp.log.Vog

/**
 * # GlobalLog
 *
 * @author 17719
 * 2018/8/11
 */
object GlobalLog {

    private val logList = mutableListOf<Pair<Int, String>>()
    fun log(msg: String) {
        write(LEVEL_INFO, msg)
    }

    fun err(msg: String) {
        write(LEVEL_ERROR, msg)
    }

    fun clear() {
        logList.clear()
    }

    private fun write(level: Int, msg: String) {
        synchronized<Unit>(logList) {
            Vog.d(this, "write $level $msg")
            logList.add(Pair(level, msg))
        }
    }

    const val LEVEL_INFO = 0
    const val LEVEL_ERROR = 1
}