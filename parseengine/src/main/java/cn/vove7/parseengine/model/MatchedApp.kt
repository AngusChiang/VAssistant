package cn.vove7.parseengine.model

/**
 * MatchedApp
 * @param matchRate 匹配率
 * @param app 对应AppInfo
 * Created by Vove on 2018/6/15
 */
data class MatchedApp(val matchRate: Float, val app: AppInfo) : Comparable<MatchedApp> {
    override fun compareTo(other: MatchedApp): Int {
        return if (app.priority != other.app.priority) {
            app.priority - other.app.priority
        } else
            ((matchRate - other.matchRate) * 100).toInt()
    }
}