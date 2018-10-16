package cn.vove7.executorengine.parse

import java.util.PriorityQueue

import cn.vove7.common.datamanager.parse.model.Action

/**
 * 解析结果
 * Created by Vove on 2018/6/18
 */
class ParseResult {
    var isSuccess = false
    var actionQueue: PriorityQueue<Action>? = null

    var msg: String? = null

    constructor(isSuccess: Boolean?) {
        this.isSuccess = isSuccess!!
    }

    constructor(isSuccess: Boolean?, actionQueue: PriorityQueue<Action>) {
        this.isSuccess = isSuccess!!
        this.actionQueue = actionQueue
    }

    constructor(isSuccess: Boolean, actionQueue: PriorityQueue<Action>, msg: String?) {
        this.isSuccess = isSuccess
        this.actionQueue = actionQueue
        this.msg = msg
    }

    constructor(isSuccess: Boolean, msg: String?) {
        this.isSuccess = isSuccess
        this.msg = msg
    }
}