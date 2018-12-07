package cn.vove7.common.view.notifier

import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.view.finder.ViewFinder
import cn.vove7.vtp.log.Vog

/**
 * # UiViewShowNotifier
 *
 * @author 17719247306
 * 2018/9/3
 */
@Deprecated("弃用")
class UiViewShowNotifier(private val locks: MutableMap<ViewFinder, ViewShowListener>)
    : AbsViewShowNotifier(locks.keys) {

    override fun onShow(finder: ViewFinder, node: ViewNode): Boolean {
        locks[finder]?.notifyViewShow(node)
        return true
    }

    override fun onFinish(removeList: MutableList<ViewFinder>) {
        if (removeList.isNotEmpty()) Vog.d(this, "UiViewShowNotifier remove ${removeList.size}")
        removeList.forEach { locks.remove(it) }
        removeList.clear()
    }
}