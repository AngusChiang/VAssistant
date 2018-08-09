package cn.vove7.common.view.notifier

import cn.vove7.common.ShowListener
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.viewnode.ViewNode
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep

/**
 * 通知View/Activity出现 基类 -- id  desc  pkg  通知器
 *
 *
 */
open class ViewShowNotifier(private val locks: MutableMap<ViewShowListener, ViewFinder>) : ShowListener {

    private fun find(finder: ViewFinder): ViewNode? {
        val r = finder.findFirst()
        if (r != null) Vog.i(this, " find it")
        return r
    }

//    fun logTag(): String = "ViewShowNotifier"

    /**
     * 检查列表
     */
    override fun notifyIfShow() {
        val removeList = mutableListOf<ViewShowListener>()
        sleep(500)
        if (locks.isNotEmpty())
            Vog.d(this, "search ${locks.size}")
        else return
        synchronized(locks) {
            /*.filter {filter(it.value)}*/
            locks.forEach {
                Vog.d(this, " ${it.value}")
                val node = find(it.value)
                if (node != null) {
                    Vog.d(this, " ${it.value} successful")
                    it.key.notifyShow(node)
                    removeList.add(it.key)
                }
            }
            if (removeList.isNotEmpty()) Vog.d(this, "notifyShow remove ${removeList.size}")
            removeList.forEach { locks.remove(it) }
            removeList.clear()
        }
    }
}