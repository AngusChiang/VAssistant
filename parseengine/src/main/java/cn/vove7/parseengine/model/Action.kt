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
        val actionScript: String,
        /**
         * 操作参数
         */
        var param: Param? = null

) : Comparable<Action> {
    override fun compareTo(other: Action): Int {
        return priority - other.priority
    }

    companion object {
        /**
         * 启动App，其他
         */
        const val ACTION_OPEN_APP = 1
        /**
         * 拨打电话
         */
        const val ACTION_CALL = 2
        const val ACTION_CLICK = 3
    }
}