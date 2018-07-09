package cn.vove7.jarvis.view.notifier

import cn.vove7.jarvis.view.finder.ViewFinder
import cn.vove7.executorengine.Executor
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep

/**
 * 通知View/Activity出现基类 -- id  desc  pkg  通知器
 *
 *
 */
abstract class ViewShowNotifier(var viewFinder: ViewFinder? = null,
                                private val locks: MutableMap<Executor, Pair<String, String>>) {
    /**
     * 使用ViewFinder 需重写
     */
    protected open fun buildFinder(data: String): ViewFinder? = null

    /**
     * 筛选列表条件
     */
    protected open fun filter(data: Pair<String, String>): Boolean = true

    /**
     * 通知条件
     * 默认为ViewFinder.findFirst()
     */
    protected open fun notifyCondition(data: Pair<String, String>): Boolean {
        viewFinder = buildFinder(data.second)
        val r = viewFinder?.findFirst() != null
        if (r) Vog.i(this, "notifyCondition find it")
        return r
    }

    open fun logTag(): String = "ViewShowNotifier"

    /**
     * 检查列表
     */
    fun notifyIfShow() {
        val removeList = mutableListOf<Executor>()
        sleep(500)
        if (locks.isNotEmpty())
            Vog.d(this, "${logTag()} search ${locks.size}")
        synchronized(locks) {
            locks.filter {
                filter(it.value)
            }.forEach {
                Vog.d(this, "${logTag()} ${it.value.second}")
                if (notifyCondition(it.value)) {
                    Vog.d(this, "${logTag()} ${it.value} successful")
                    it.key.notifySync()
                    removeList.add(it.key)
                }
            }
            removeList.forEach {
                locks.remove(it)
            }
        }
    }
}