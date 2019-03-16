package cn.vove7.executorengine.model

import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.executorengine.parse.OpenAppAction
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.log.Vog
import java.util.*

/**
 * 解析结果
 * Created by Vove on 2018/6/18
 */
class ActionParseResult(
        val isSuccess: Boolean = false,
        var actionQueue: PriorityQueue<Action>? = null,
        var msg: String? = null,
        val appinfo: AppInfo? = null
) {
    fun insertOpenAppAction(currentScope: ActionScope): ActionParseResult {
        if (!isSuccess) return this

        appinfo?.packageName?.also {
            val appScope = actionQueue?.peek()?.scope//action入口
            val ina = appScope?.inActivity(currentScope.activity) ?: true

            if (!ina) {//Activity 空 or Activity 不等 => 不同页面
                Vog.d(this, "parseAction ---> 应用内不同页")
                //插入跳转代码
                val openAction by OpenAppAction(appScope?.packageName ?: "")
                actionQueue?.add(openAction)
            }//else 位于当前界面 不用添加跳转代码
        }
        return this
    }
}