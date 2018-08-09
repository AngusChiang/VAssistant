package cn.vove7.common.view.notifier

import cn.vove7.common.viewnode.ViewNode

/**
 * # ViewShowListener
 *
 * @author Vove
 * 2018/8/4
 */
interface ViewShowListener {

    fun notifyShow(node: ViewNode)
}