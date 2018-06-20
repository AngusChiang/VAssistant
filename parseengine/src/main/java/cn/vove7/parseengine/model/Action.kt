package cn.vove7.parseengine.model

/**
 *
 * Action集合
 * Created by Vove on 2018/6/18
 */
class Action(
//        val actionCode: Int = 0,
        var matchWord: String = "",
        /**
         * 执行优先级
         */
        private val priority: Int = 0,
        /**
         * 格式：
         * openApp:$var
         * clickText:$var
         * clickId:$var
         * back
         * recent
         * pullNotification
         * call
         *
         */

        val actionScript: String,
        /**
         * 操作参数
         */
        var param: Param? = null,
        /**
         * 获取中途参数结果
         */
        var voiceOk: Boolean = true

) : Comparable<Action> {
    override fun compareTo(other: Action): Int {
        return priority - other.priority
    }

    companion object {
        /**
         * 启动App，其他
         */
        const val ACTION_OPEN = 1
        /**
         * 拨打电话
         */
        const val ACTION_CALL = 2
        const val ACTION_CLICK = 3
    }
}