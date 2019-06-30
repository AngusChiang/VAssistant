package cn.vove7.common.view.notifier

import cn.vove7.common.ShowListener
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.log.Vog

/**
 * 通知View/Activity出现
 * 基类 -- id  desc  pkg  通知器
 *
 * 由无障碍 事件驱动
 * 弃用
 */
@Deprecated("ViewFinder被动通知 改用主动搜索")
abstract class AbsViewShowNotifier(private val finders: MutableSet<ViewFinder>) : ShowListener {
    /**
     * 检查列表
     *
     * @return firstly Int the count of was notified secondly Int was count of notified success
     */
//    @Synchronized
    override fun notifyIfShow(): Pair<Int, Int> {
        val removeList = mutableListOf<ViewFinder>()
        if (finders.isNotEmpty())
            Vog.d("search View: ${finders.size}")
        else return Pair(0, 0)
        var succ = 0
//        sleep(500)
        synchronized(finders) {
            kotlin.run out@{
                /*.filter {filter(it.value)}*/
                finders.forEach {
                    if (Thread.currentThread().isInterrupted) {
                        Vog.d("AbsViewShowNotifier isInterrupted")
                        return@out
                    }
                    Vog.d(" $it")
                    val node = it.findFirst()
                    if (Thread.currentThread().isInterrupted) {
                        Vog.d("AbsViewShowNotifier isInterrupted")
                        return@out
                    }
                    if (node != null) {
                        Vog.i("find $it successful")
                        if (onShow(it, node)) succ++
                        removeList.add(it)
                    }
                }
            }
            val num = removeList.size
            onFinish(removeList)
            return Pair(num, succ)
        }
    }

    abstract fun onShow(finder: ViewFinder, node: ViewNode): Boolean

    abstract fun onFinish(removeList: MutableList<ViewFinder>)
}