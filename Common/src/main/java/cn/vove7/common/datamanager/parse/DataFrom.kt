package cn.vove7.common.datamanager.parse

/**
 * @author 17719247306
 *
 *
 * 2018/8/25
 */
interface DataFrom {
    companion object {
        const val FROM_USER = "from_user"
        //unuse
        const val FROM_SHARED = "from_shared"
        const val FROM_SERVER = "from_server"

        fun translate(from: String?): String {
            return when (from) {
                FROM_SERVER -> "服务"
                FROM_SHARED -> "分享"
                FROM_USER -> "用户"
                else -> "未知"
            }
        }
    }

}
