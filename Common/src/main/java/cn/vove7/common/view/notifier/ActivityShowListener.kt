package cn.vove7.common.view.notifier

import cn.vove7.common.datamanager.parse.model.ActionScope

/**
 * # ActivityShowListener
 *
 * @author 17719
 * 2018/8/7
 */
interface ActivityShowListener {
    fun notifyShow(scope: ActionScope)
}