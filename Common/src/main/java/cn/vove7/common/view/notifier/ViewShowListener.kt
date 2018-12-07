package cn.vove7.common.view.notifier

import cn.vove7.common.accessibility.viewnode.ViewNode

/**
 * # ViewShowListener
 *
 * @author Vove
 * 2018/8/4
 */
interface ViewShowListener {

    fun notifyViewShow(node: ViewNode)
}