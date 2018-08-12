package cn.vove7.parseengine.engine

import android.util.Log
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.greendao.MapNodeDao
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.datamanager.parse.statusmap.MapNode
import cn.vove7.datamanager.parse.statusmap.MapNode.*
import cn.vove7.datamanager.parse.statusmap.Reg
import cn.vove7.datamanager.parse.statusmap.Reg.*
import cn.vove7.parseengine.model.ParseResult
import java.util.*

/**
 * # ParseEngine
 * - 提取变量值 [%cmd] [%var%]
 * -
 * Created by Vove on 2018/6/15
 */
object ParseEngine {
    var GlobalActionNodes: List<MapNode>? = null
    var AppActionNodes: List<MapNode>? = null
    fun getById(nodes: List<MapNode>, id: Long): MapNode? {
        nodes.forEach {
            if (it.id == id)
                return it
        }
        return null
    }

    var i = 0

    /**
     * 根据匹配 ，返回操作
     * @return @see [ParseResult]
     * 语音命令转执行步骤顺序问题，方便排序
     * 0>1>..>9
     *
     * 命令    ↓
     * 全局命令 ↓ 一级命令
     * App内   ↓ 二级命令
     *
     */
    fun parseGlobalAction(cmdWord: String, pkg: String): ParseResult {
        i = 0
        val actionQueue = PriorityQueue<Action>()
        val action = globalActionMatch(cmdWord)
        return if (action != null) {
            actionQueue.add(action)
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
    private fun globalActionMatch(cmd: String): Action? {
        if (GlobalActionNodes == null)
            GlobalActionNodes = DAO.daoSession.mapNodeDao.queryBuilder()
                    .where(MapNodeDao.Properties.NodeType.eq(NODE_TYPE_GLOBAL)).list()
        GlobalActionNodes?.forEach {
            it.regs.forEach { reg ->
                val result = reg.regex.matchEntire(cmd)
                if (result != null && result.groups.isNotEmpty()) {//匹配成功
                    val action = it.action
                    extractParam(action, reg, result)
                    return action
                }
            }
        }
        return null
    }

    /**
     * 全局命令解析失败
     * 或在执行打开应用后，解析跟随指令
     * 网易云 音乐 播放
     * QQ扫一扫
     * App内指令
     * 深度搜索
     */
    fun matchAppAction(cmd: String, currentAppPkg: String): PriorityQueue<Action> {
        Log.d("Debug :", "matchAppAction  ----> $currentAppPkg")
        if (AppActionNodes == null) {
            AppActionNodes = DAO.daoSession.mapNodeDao.queryBuilder()
                    .where(MapNodeDao.Properties.NodeType.`in`(NODE_TYPE_IN_APP, NODE_TYPE_IN_APP_2))
                    .list()
        }
        val actionQueue = PriorityQueue<Action>()
        AppActionNodes?.filter {
            //筛选当前App
            it.actionScope != null && it.actionScope.packageName.startsWith(currentAppPkg)
                    && it.nodeType == NODE_TYPE_IN_APP
        }?.forEach {
            it.regs.forEach { reg ->
                val result = reg.regex.matchEntire(cmd)
                if (result != null) {
                    it.action.param = it.param
                    extractParam(it.action, reg, result)//提取参数
                    it.action.matchWord = result.groupValues[0]
                    actionQueue.add(it.action)
                    appActionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], it)
                    return actionQueue
                }
            }
        }
        return actionQueue
    }

    /**
     * App内指令
     * 深度搜索
     */
    private fun appActionDsMatch(actionQueue: PriorityQueue<Action>, node: MapNode,
                                 sufWord: String, preNode: MapNode? = null): Boolean {
        if (sufWord.isEmpty()) return true
//        println("${i++}. 匹配：$sufWord")
        node.follows.split(',').filter { it.trim() != "" }.forEach { fs ->
            val it = getById(AppActionNodes!!, fs.toLong())
            it?.regs?.forEach { reg ->
                val result = reg.regex.matchEntire(sufWord)
                if (result != null && result.groups.isNotEmpty()) {//深搜
//                    println("--匹配成功")
                    //匹配成功
                    if (preNode != null) {//修剪上一个匹配结果参数,第一个%即为上一个参数
                        if (result.groupValues[0].startsWith(preNode.param?.value ?: "--")) {
                            val preParamLen = if (preNode.param != null) result.groupValues[1].length else 0
                            val thisMatchLen = result.groupValues[0].length
                            preNode.param?.value = preNode.param?.value?.substring(0, preParamLen)
                            val allLen = preNode.action.matchWord.length
                            preNode.action.matchWord = preNode.action.matchWord
                                    .substring(0, allLen - (thisMatchLen - preParamLen))
                        }
                    }
                    extractParam(it.action, reg, result)//提取参数
                    //println("--临时提取参数：$param -${it.param!!.desc}")
                    it.action.matchWord = result.groupValues[0]
                            .substring(preNode?.param?.value?.length ?: 0)//
                    actionQueue.add(it.action)
                    return if (it.follows.isNotEmpty()) {//不空
                        appActionDsMatch(actionQueue, it, result.groupValues[result.groupValues.size - 1], it)//递归匹配
                    } else true
                }
            }
        }
        return preNode?.param != null
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
        }
    }

    fun getLastParam(colls: MatchGroupCollection): String {
        colls.reversed().forEach {
            if (it != null && it.value != "") {
                return it.value
            }
        }
        return ""
    }

}


// val GlobalActionNodes = hashMapOf<Int, MapNode>()
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
