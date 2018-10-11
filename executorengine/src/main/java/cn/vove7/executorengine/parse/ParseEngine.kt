package cn.vove7.executorengine.parse

import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_GLOBAL
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.common.datamanager.parse.statusmap.Reg
import cn.vove7.common.datamanager.parse.statusmap.Reg.*
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.executorengine.exector.MultiExecutorEngine
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import java.util.*
import kotlin.concurrent.thread

/**
 * # ParseEngine
 * - 提取变量值 [%cmd] [%var%]
 * -
 * Created by Vove on 2018/6/15
 */
object ParseEngine {
    var GlobalActionNodes: List<ActionNode>? = null
    var AppActionNodes: List<ActionNode>? = null

    //重置App
    val PRE_OPEN = "openAppByPkg('%s',true)\n"
    var i = 0

    /**
     * 同步后，更新数据
     */
    fun updateNode() {
        thread {
            updateInApp()
            updateGlobal()
        }
    }

    fun updateInApp() {
        AppActionNodes = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ActionScopeType
                        .eq(NODE_SCOPE_IN_APP/*, NODE_SCOPE_IN_APP_2*/))
                .orderDesc(ActionNodeDao.Properties.Priority)
                .list()
    }

    fun updateGlobal() {
        GlobalActionNodes = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ActionScopeType
                        .eq(NODE_SCOPE_GLOBAL /*,NODE_SCOPE_GLOBAL_2*/))
                .orderDesc(ActionNodeDao.Properties.Priority)
                .list()
    }

    /**
     * 匹配 ，返回操作
     * @return @see [ParseResult]
     * 语音命令转执行步骤顺序问题，方便排序
     * 0>1>..>9
     *
     * 命令    ↓
     * 全局命令 ↓ 一级命令 -> ...
     * 使用打开 ->
     * App内   ↓ 二级命令 -> 扫一扫/ 不在指定Activity -> （有跟随指令）跳至首页
     *
     *
     *
     */
    fun parseAction(cmdWord: String, scope: ActionScope?): ParseResult {
        i = 0
        val actionQueue = PriorityQueue<Action>()
        val gaq = globalActionMatch(cmdWord)
        actionQueue.addAll(gaq.second)
        return if (actionQueue.isNotEmpty()) {
            ParseResult(true, actionQueue, gaq.first)
        } else {
            Vog.d(this, "globalAction --无匹配")
            if (SpHelper(GlobalApp.APP).getBoolean("use_smartopen_if_parse_failed", true)) {
                Vog.d(this, "开启 -- smartOpen")
                if (cmdWord != "") {//使用smartOpen
//                    val q = PriorityQueue<Action>()
                    val result = MultiExecutorEngine().smartOpen(cmdWord)
                    if (result) {//成功打开
                        return ParseResult(true, PriorityQueue(), "smartOpen $cmdWord")
                    }
                }
                Vog.d(this, "smartOpen --无匹配")
            }//结果?

            if (scope == null) {
                Vog.d(this, "scope is null 无障碍未打开")
                return ParseResult(false, "无匹配")
            }
            val inAppQue = matchAppAction(cmdWord, scope)
            actionQueue.addAll(inAppQue.second) //匹配应用内时
            // 根据第一个action.scope 决定是否进入首页
            if (actionQueue.isNotEmpty()) {//自动执行打开
                val appScope = actionQueue.peek().scope
                if (appScope.activity ?: "" == "" || appScope.activity != scope.activity) {//Activity 空 or Activity 不等 => 不同页面
                    Vog.d(this, "parseAction ---> 应用内不同页")
                    actionQueue.add(Action(-999,
                            String.format(PRE_OPEN, scope.packageName)
                            , Action.SCRIPT_TYPE_LUA))
                }
            } else if (SpHelper(GlobalApp.APP).getBoolean("use_smartopen_if_parse_failed",
                            true) && AccessibilityApi.isOpen()) {//失败,默认点击
                if(ViewFindBuilder().similaryText(cmdWord).tryClick())
                    return ParseResult(true, PriorityQueue(), "smart点击 $cmdWord")
            }
            ParseResult(actionQueue.isNotEmpty(), actionQueue, inAppQue.first)
        }
    }

    /**
     * 全局命令
     * 一级匹配
     * 全局不存在follows
     */
    private fun globalActionMatch(cmd: String): Pair<String?, PriorityQueue<Action>> {
        val actionQueue = PriorityQueue<Action>()
        if (GlobalActionNodes == null)
            updateGlobal()
        GlobalActionNodes?.forEach {
            val r = regSearch(cmd, it, actionQueue)
            if (r) return Pair(it.actionTitle, actionQueue)
        }
        return Pair(null, actionQueue)
    }

    /**
     * 全局命令解析失败 匹配App内指令  根据[包名]
     * 或在执行打开应用后，解析跟随指令
     * eg:
     * 网易云 音乐 播放
     * QQ扫一扫
     * App内指令
     * 深度搜索
     * @return  Pair<String?, PriorityQueue<Action> 匹配的标题
     */
    fun matchAppAction(cmd: String, matchScope: ActionScope): Pair<String?, PriorityQueue<Action>> {

//        Log.d("Debug :", "matchAppAction  ----> $currentAppPkg")
        if (AppActionNodes == null) {
            updateInApp()
        }
        val actionQueue = PriorityQueue<Action>()
        AppActionNodes?.filter {
            //筛选当前App  根据pkg
            it.actionScope != null && matchScope.eqPkg(it.actionScope)
        }?.forEach {
            val r = regSearch(cmd, it, actionQueue)
            if (r) return Pair(it.actionTitle, actionQueue)
        }
        return Pair(null, actionQueue)
    }


    /**
     * ActionNode正则匹配
     * @param cmd String
     * @param it ActionNode
     * @param actionQueue PriorityQueue<Action>
     * @return Boolean
     */
    private fun regSearch(cmd: String, it: ActionNode, actionQueue: PriorityQueue<Action>): Boolean {
        it.regs.forEach { reg ->
            val result = reg.followRegex.matchEntire(cmd)
            if (result != null) {
                val ac = it.action
                ac.scope = it.actionScope
                ac.param = it.param
                extractParam(ac, reg, result)//提取参数
                ac.matchWord = result.groupValues[0]
                actionQueue.add(ac)
                //深搜命令
                actionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], ac)
                return true
            }
        }
        return false
    }

    /**
     * 指令深度搜索
     * 沿follows路径搜索
     */
    private fun actionDsMatch(actionQueue: PriorityQueue<Action>, node: ActionNode, sufWord: String,
                              preAction: Action? = null): Boolean {
        if (sufWord.isEmpty()) return true
//        println("${i++}. 匹配：$sufWord")
        node.follows.forEach { it ->
            it?.regs?.forEach { reg ->
                val result = reg.followRegex.matchEntire(sufWord)
                if (result != null && result.groups.isNotEmpty()) {//深搜
//                    println("--匹配成功")
                    //匹配成功
                    if (preAction != null) {//修剪上一个匹配结果参数,第一个%即为上一个参数
                        val old = preAction.param?.value
                        if (result.groupValues[0].startsWith(old ?: "--")) {
                            val preParamLen = if (preAction.param != null) result.groupValues[1].length else 0
                            val thisMatchLen = result.groupValues[0].length
                            preAction.param?.value = old?.substring(0, preParamLen)
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
                        actionDsMatch(actionQueue, it, result.groupValues[result.groupValues.size - 1], itsAction)//递归匹配
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
//            Vog.d(this, "extractParam $param")
        }
    }

    fun getLastParam(colls: MatchGroupCollection): String? {
        colls.reversed().withIndex().forEach { iv ->
            val it = iv.value
            if (it != null && it.value != "" && iv.index != colls.size - 1) {//不是第一个
                return it.value
            }
        }
        return null
    }


    /**
     * 解析测试
     * @param testWord String
     * @param node ActionNode 节点
     * @return ParseResult
     */
    fun testParse(testWord: String, node: ActionNode): ParseResult {
        val actionQueue = PriorityQueue<Action>()
        val r = regSearch(testWord, node, actionQueue)
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
