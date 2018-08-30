package cn.vove7.parseengine.engine

import android.util.Log
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.*
import cn.vove7.common.datamanager.parse.statusmap.Reg
import cn.vove7.common.datamanager.parse.statusmap.Reg.*
import cn.vove7.parseengine.model.ParseResult
import cn.vove7.vtp.log.Vog
import java.util.*

/**
 * # ParseEngine
 * - 提取变量值 [%cmd] [%var%]
 * -
 * Created by Vove on 2018/6/15
 */
object ParseEngine {
    var GlobalActionNodes: List<ActionNode>? = null
    var AppActionNodes: List<ActionNode>? = null
    private fun getNodeById(nodes: List<ActionNode>, id: Long): ActionNode? {
        nodes.forEach {
            if (it.id == id)
                return it
        }
        return null
    }

    var i = 0

    /**
     * 匹配 ，返回操作
     * @return @see [ParseResult]
     * 语音命令转执行步骤顺序问题，方便排序
     * 0>1>..>9
     *
     * 命令    ↓
     * 全局命令 ↓ 一级命令
     * App内   ↓ 二级命令
     *
     */
    fun parseAction(cmdWord: String, pkg: String): ParseResult {
        i = 0
        val actionQueue = PriorityQueue<Action>()
        actionQueue.addAll(globalActionMatch(cmdWord))
        return if (actionQueue.isNotEmpty()) {
            ParseResult(true, actionQueue)
        } else {
            println("globalAction--无匹配")
            if (pkg == "") {
                ParseResult(false, "pkg null")
            }
            actionQueue.addAll(matchAppAction(cmdWord, pkg))
            ParseResult(actionQueue.isNotEmpty(), actionQueue)
        }
    }

    /**
     * 全局命令
     * 一级匹配
     * 全局不存在follows
     */
    private fun globalActionMatch(cmd: String): PriorityQueue<Action> {
        val actionQueue = PriorityQueue<Action>()
        if (GlobalActionNodes == null)
            GlobalActionNodes = DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.NodeType
                            .`in`(NODE_SCOPE_ALL, NODE_SCOPE_GLOBAL, NODE_SCOPE_GLOBAL_2))
                    .orderDesc(ActionNodeDao.Properties.Priority)
                    .list()
        GlobalActionNodes?.forEach {

            val r = search(cmd, it, actionQueue, GlobalActionNodes!!)
            if (r) return actionQueue
//
//            it.regs.forEach { reg ->
//                val result = reg.regex.matchEntire(cmd)
//                if (result != null) {
//                    it.action.param = it.param
//                    extractParam(it.action, reg, result)//提取参数
//                    it.action.matchWord = result.groupValues[0]
//                    actionQueue.add(it.action)
//                    //深搜命令
//                    actionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], it)
//                    return actionQueue
//                }
//            }
//            it.regs.forEach { reg ->
//                val result = reg.regex.matchEntire(cmd)
//                if (result != null && result.groups.isNotEmpty()) {//匹配成功
//                    val action = it.action
//                    extractParam(action, reg, result)
//                    return action
//                }
//            }
        }
        return actionQueue
    }

    /**
     * 全局命令解析失败
     * 或在执行打开应用后，解析跟随指令
     * eg:
     * 网易云 音乐 播放
     * QQ扫一扫
     * App内指令
     * 深度搜索
     */
    fun matchAppAction(cmd: String, currentAppPkg: String): PriorityQueue<Action> {
        Log.d("Debug :", "matchAppAction  ----> $currentAppPkg")
        if (AppActionNodes == null) {
            AppActionNodes = DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.NodeType
                            .`in`(NODE_SCOPE_ALL, NODE_SCOPE_IN_APP, NODE_SCOPE_IN_APP_2))
                    .orderDesc(ActionNodeDao.Properties.Priority)
                    .list()
        }
        val actionQueue = PriorityQueue<Action>()
        AppActionNodes?.filter {
            //筛选当前App
            it.actionScope != null && it.actionScope.packageName.startsWith(currentAppPkg)
                    && it.actionScopeType == NODE_SCOPE_IN_APP
        }?.forEach {
            val r = search(cmd, it, actionQueue, AppActionNodes!!)
            if (r) return actionQueue
//
//            it.regs.forEach { reg ->
//                val result = reg.regex.matchEntire(cmd)
//                if (result != null) {
//                    it.action.param = it.param
//                    extractParam(it.action, reg, result)//提取参数
//                    it.action.matchWord = result.groupValues[0]
//                    actionQueue.add(it.action)
//                    //深搜命令
//                    actionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], it)
//                    return actionQueue
//                }
//            }
        }
        return actionQueue
    }

    //提取公共搜索函数
    private fun search(cmd: String, it: ActionNode, actionQueue: PriorityQueue<Action>, allNodes: List<ActionNode>): Boolean {
        it.regs.forEach { reg ->
            val result = reg.regex.matchEntire(cmd)
            if (result != null) {
                val ac = it.action// 防止重复执行getAction
                ac.param = it.param
                extractParam(ac, reg, result)//提取参数
                ac.matchWord = result.groupValues[0]
                actionQueue.add(ac)
                //深搜命令
                actionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], ac, allNodes)
                return true
            }
        }
        return false
    }

    /**
     * 指令深度搜索
     * 沿follows路径搜索
     */
    private fun actionDsMatch(actionQueue: PriorityQueue<Action>, node: ActionNode,
                              sufWord: String, preAction: Action? = null, actionNodes: List<ActionNode>): Boolean {
        if (sufWord.isEmpty()) return true
//        println("${i++}. 匹配：$sufWord")
        node.follows.split(',').filter { it.trim() != "" }.forEach { fs ->
            val it = getNodeById(actionNodes, fs.toLong())
            it?.regs?.forEach { reg ->
                val result = reg.regex.matchEntire(sufWord)
                if (result != null && result.groups.isNotEmpty()) {//深搜
//                    println("--匹配成功")
                    //匹配成功
                    if (preAction != null) {//修剪上一个匹配结果参数,第一个%即为上一个参数
                        if (result.groupValues[0].startsWith(preAction.param?.value ?: "--")) {
                            val preParamLen = if (preAction.param != null) result.groupValues[1].length else 0
                            val thisMatchLen = result.groupValues[0].length
                            preAction.param?.value = preAction.param?.value?.substring(0, preParamLen)
                            val allLen = preAction.matchWord.length
                            preAction.matchWord = preAction.matchWord
                                    .substring(0, allLen - (thisMatchLen - preParamLen))
                        }
                    }
                    val itsAction = it.action
                    extractParam(itsAction, reg, result)//提取参数
                    //println("--临时提取参数：$param -${it.param!!.desc}")

                    itsAction.matchWord = result.groupValues[0]
                            .substring(preAction?.param?.value?.length ?: 0)//
                    actionQueue.add(itsAction)
                    return if (it.follows.isNotEmpty()) {//不空
                        actionDsMatch(actionQueue, it, result.groupValues[result.groupValues.size - 1], itsAction, actionNodes)//递归匹配
                    } else true
                }
            }
        }
        return preAction?.param != null
    }


    //提取参数
    private fun extractParam(it: Action, reg: Reg, result: MatchResult) {
        val param = it.param
        if (param != null) {//设置参数
            when (reg.paramPos) {
                PARAM_POS_END -> {
                    param.value = getLastParam(result.groups)
                }
                PARAM_POS_1, PARAM_POS_2, PARAM_POS_3 ->
                    param.value = result.groups[reg.paramPos]?.value
            }
            it.param = param
            Vog.d(this,"extractParam $param")
        }
    }

    fun getLastParam(colls: MatchGroupCollection): String? {
        colls.reversed().forEach {
            if (it != null && it.value != "") {
                return it.value
            }
        }
        return null
    }


    /**
     * 解析测试
     * @param testWord String
     * @param nodes List<ActionNode> 默认第一个节点为
     * @return ParseResult
     */
    fun testParse(testWord: String, nodes: List<ActionNode>): ParseResult {
        val actionQueue = PriorityQueue<Action>()
        nodes.forEach {
            val r = search(testWord, it, actionQueue, nodes)
            if (r) return ParseResult(true, actionQueue)
        }
        return ParseResult(actionQueue.isNotEmpty(), actionQueue)
    }


}


// val GlobalActionNodes = hashMapOf<Int, ActionNode>()
//        println("${i++}. 匹配：$sufWord")
//        startNode.follows.split(',').forEach { fs ->
//            val it = GlobalActionNodes[fs.toInt()]
//            it.regs.forEach { reg ->
//                val result = reg.regex.matchEntire(sufWord)
//
//                if (result != null && result.groups.isNotEmpty()) {//深搜
////                    println("--匹配成功")
//                    //匹配成功
//
//                    if (preNode != null) {//修剪上一个匹配结果参数,第一个%即为上一个参数
//                        val preParamLen = if (preNode.param != null) result.groupValues[1].length else 0
//                        val thisMatchLen = result.groupValues[0].length
//                        preNode.param?.value = preNode.param?.value?.substring(0, preParamLen)
//                        val allLen = preNode.action.matchWord.length
//                        preNode.action.matchWord = preNode.action.matchWord.substring(0, allLen - (thisMatchLen - preParamLen))
//                    }
//
//                    extractParam(it, reg, result)
//
//                    //赋值
//                    //println("--临时提取参数：$param -${it.param!!.desc}")
//                    it.action.matchWord = result.groupValues[0]
//                            .substring(preNode?.param?.value?.length ?: 0)//
//                    resList.add(it.action)
//                    return if (it.follows.isNotEmpty()) {//不空
//                        globalActionMatch(resList, it, result.groupValues[result.groupValues.size - 1], it)
//                    } else {
//                        true
//                    }
//                }
//            }
//        }
//        return preNode?.param != null
//    }
