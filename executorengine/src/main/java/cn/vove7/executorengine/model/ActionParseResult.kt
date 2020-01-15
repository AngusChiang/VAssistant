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
        val appinfo: AppInfo? = null,
        //上次全局指令匹配位置
        val lastGlobalPosition: Int = -1
) {
    fun insertOpenAppAction(currentScope: ActionScope): ActionParseResult {
        if (!isSuccess) return this
        val q = actionQueue ?: return this
        val pkg = appinfo?.packageName ?: return this
        Companion.insertOpenAppAction(pkg, currentScope, q)
        return this
    }

    companion object {
        fun insertOpenAppAction(pkg: String, currentScope: ActionScope, q: PriorityQueue<Action>) {
            pkg.also {
                val appScope = q?.peek()?.scope//action入口
                val ina = appScope?.inActivity(currentScope.activity) ?: false

                if (!ina) {//Activity 空 or Activity 不等 => 不同页面
                    Vog.d("parseAction ---> 应用内不同页")
                    //插入跳转代码
                    val openAction by OpenAppAction(pkg)
                    q.add(openAction)
                }//else 位于当前界面 不用添加跳转代码
            }
        }
    }
}