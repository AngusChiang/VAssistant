package cn.vove7.common.view.finder

import cn.vove7.datamanager.parse.model.ActionScope

/**
 * # ActivityShowListener
 *
 * @author 17719
 * 2018/8/7
 */
interface ActivityShowListener {
    fun notifyShow(scope: ActionScope)
}