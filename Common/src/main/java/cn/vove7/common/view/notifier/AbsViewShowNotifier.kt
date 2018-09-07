package cn.vove7.common.view.notifier

import cn.vove7.common.ShowListener
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.log.Vog

/**
 * 通知View/Activity出现 基类 -- id  desc  pkg  通知器
 *
 *
 */
abstract class AbsViewShowNotifier(private val finders: MutableSet<ViewFinder>) : ShowListener {

    /**
     * 检查列表
     *
     * @return Int the count of was notified
     */
//    @Synchronized
    override fun notifyIfShow(): Int {
        val removeList = mutableListOf<ViewFinder>()
        if (finders.isNotEmpty())
            Vog.d(this, "search View: ${finders.size}")
        else return 0
//        sleep(500)
        synchronized(finders) {
            kotlin.run out@{
                /*.filter {filter(it.value)}*/
                finders.forEach {
                    if (Thread.currentThread().isInterrupted) {
                        Vog.d(this, "AbsViewShowNotifier isInterrupted")
                        return@out
                    }
                    Vog.d(this, " $it")
                    val node = it.findFirst()
                    if (Thread.currentThread().isInterrupted) {
                        Vog.d(this, "AbsViewShowNotifier isInterrupted")
                        return@out
                    }
                    if (node != null) {
                        Vog.i(this, "find $it successful")
                        onShow(it, node)
                        removeList.add(it)
                    }
                }
            }
            val num = removeList.size
            onFinish(removeList)
            return num
        }
    }

    abstract fun onShow(finder: ViewFinder, node: ViewNode)

    abstract fun onFinish(removeList: MutableList<ViewFinder>)
}