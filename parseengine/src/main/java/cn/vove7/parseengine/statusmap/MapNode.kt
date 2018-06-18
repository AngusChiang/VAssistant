package cn.vove7.parseengine.statusmap

import cn.vove7.parseengine.model.Action
import cn.vove7.parseengine.model.ActionScope
import cn.vove7.parseengine.model.Param

/**
 * 状态图节点
 * - 1 Node对应1参数
 * Created by Vove on 2018/6/17
 */
data class MapNode(
        /**
         * primary key
         */
        val id: Int,
        /**
         * 一个操作对应多种"说法"
         */
        val regs: List<Reg>,
        /**
         * 一波操作
         */
        var action: Action,
        /**
         * 后续节点id
         */
        var follows: List<Int>,
        /**
         * 操作参数
         */
        var param: Param? = null,
        /**
         * APP作用域
         */
        val actionScope: ActionScope? = null
)

/**
 * 次节点以%开始
 * % -> [\S]*
 */
class Reg(
        /**
         * 正则表达式
         */
        regStr: String,
        /**
         * 参数位置，提取用
         */
        val paramPos: Int = -1
) {
    var regex: Regex

    init {
        //结尾加上% ， 防止有[后续节点操作]匹配失败
        val r = (if (!regStr.endsWith('%')) "$regStr%" else regStr)
                .replace("%", "([\\S]*)")
        regex = r.toRegex()
    }

    companion object {
        const val PARAM_POS_END = 0
        const val PARAM_POS_1 = 1
        const val PARAM_POS_2 = 2
        const val PARAM_POS_3 = 3
    }
}