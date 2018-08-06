package cn.vove7.jarvis.view.finder

import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.common.view.finder.ViewShowListener
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep

/**
 * 通知View/Activity出现 基类 -- id  desc  pkg  通知器
 *
 *
 */
class ViewShowNotifier(private val locks: MutableMap<ViewShowListener, ViewFinder>) : ViewShowListener {
    /**
     * 使用ViewFinder 需重写
     */
//    protected open fun buildFinder(data: String): ViewFinder? = null
//
//    /**
//     * 筛选列表条件
//     */
//    protected fun filter(finder: ViewFinder): Boolean = true

    /**
     * 通知条件
     * 默认为ViewFinder.findFirst()
     */
    protected fun notifyCondition(finder: ViewFinder): Boolean {
        val r = finder.findFirst() != null
        if (r) Vog.i(this, "notifyCondition find it")
        return r
    }

    fun logTag(): String = "ViewShowNotifier"

    /**
     * 检查列表
     */
    override fun notifyShow() {
        val removeList = mutableListOf<ViewShowListener>()
        sleep(500)
        if (locks.isNotEmpty())
            Vog.d(this, "${logTag()} search ${locks.size}")
        synchronized(locks) {
            locks       /*filter {filter(it.value)}*/
                    .forEach {
                        Vog.d(this, "${logTag()} ${it.value}")
                        if (notifyCondition(it.value)) {
                            Vog.d(this, "${logTag()} ${it.value} successful")
                            it.key.notifyShow()
                            removeList.add(it.key)
                        }
                    }
            removeList.forEach { locks.remove(it) }
            removeList.clear()
        }
    }
}