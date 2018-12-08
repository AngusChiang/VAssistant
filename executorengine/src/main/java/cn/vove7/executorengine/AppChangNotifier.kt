package cn.vove7.executorengine

import cn.vove7.common.accessibility.component.AbsAccPluginService
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.view.notifier.ActivityShowListener
import cn.vove7.vtp.log.Vog

/**
 * # AppChangNotifier
 * Activity 变化通知
 * @author Administrator
 * 2018/11/12
 */
class AppChangNotifier(private val locksWaitForActivity: MutableMap<ActivityShowListener, ActionScope>)
    : AbsAccPluginService() {

    private var appScope: ActionScope? = null

    private fun fill(data: ActionScope): Boolean {
        Vog.v(this, "fill $appScope - $data")
        return appScope == data
    }

    override fun onAppChanged(appScope: ActionScope) {
        synchronized(locksWaitForActivity) {
            this.appScope = appScope
            val removes = mutableListOf<ActivityShowListener>()
            kotlin.run out@{
                locksWaitForActivity.filter { fill(it.value) }.forEach { it ->
                    it.key.notifyActivityShow(appScope)
                    removes.add(it.key)
//                    if (Thread.currentThread().isInterrupted) {
//                        Vog.d(this, "activityNotifier 线程关闭")
//                        return@out
//                    }
                }
            }
            removes.forEach { locksWaitForActivity.remove(it) }
            removes.clear()
        }
    }
}