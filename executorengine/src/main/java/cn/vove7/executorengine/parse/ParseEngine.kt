package cn.vove7.executorengine.parse

import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
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
import cn.vove7.executorengine.model.ActionParseResult
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import java.util.*
import kotlin.reflect.KProperty

/**
 * # ParseEngine
 * - 提取变量值 [%cmd] [%var%]
 * -
 * Created by Vove on 2018/6/15
 */
object ParseEngine {
    private val GlobalActionNodes: List<ActionNode>
        get() = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ActionScopeType
                        .eq(NODE_SCOPE_GLOBAL /*,NODE_SCOPE_GLOBAL_2*/))
                .orderDesc(ActionNodeDao.Properties.Priority)//按优先级
                .list()
    private val AppActionNodes: List<ActionNode>
        get() = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ActionScopeType
                        .eq(NODE_SCOPE_IN_APP/*, NODE_SCOPE_IN_APP_2*/))
                .orderDesc(ActionNodeDao.Properties.Priority)
                .list()

    var i = 0

    //TODO 效率测试
//    fun updateInApp() {
//        AppActionNodes = DAO.daoSession.actionNodeDao.queryBuilder()
//                .where(ActionNodeDao.Properties.ActionScopeType
//                        .eq(NODE_SCOPE_IN_APP/*, NODE_SCOPE_IN_APP_2*/))
//                .orderDesc(ActionNodeDao.Properties.Priority)
//                .list()
//    }
//
//    fun updateGlobal() {
//        GlobalActionNodes = DAO.daoSession.actionNodeDao.queryBuilder()
//                .where(ActionNodeDao.Properties.ActionScopeType
//                        .eq(NODE_SCOPE_GLOBAL /*,NODE_SCOPE_GLOBAL_2*/))
//                .orderDesc(ActionNodeDao.Properties.Priority)//按优先级
//                .list()
//    }
    /**
     * 同步后，更新数据
     */
    fun updateNode() {
//        runOnPool {
//            updateInApp()
//            updateGlobal()
//        }
    }


    /**
     * 匹配 ，返回操作
     * @return @see [ActionParseResult]
     * 语音命令转执行步骤顺序问题，方便排序
     * 0>1>..>9
     *
     * 命令    ↓
     * 全局命令 ↓ 一级命令 -> ...
     * 使用打开 ->
     * App内   ↓ 二级命令 -> 扫一扫/ 不在指定Activity -> （有跟随指令）跳至首页
     * 点击操作
     *
     * todo 顺序 小 -> 大
     * @param scope 当前手机界面信息
     */
    fun parseAction(cmdWord: String, scope: ActionScope?): ActionParseResult {
        i = 0
        val globalResult = globalActionMatch(cmdWord)
        if (globalResult.isSuccess) {
            return globalResult
        }
        //smartOpen
        Vog.d("globalAction --无匹配")
        if (SpHelper(GlobalApp.APP).getBoolean("use_smartopen_if_parse_failed", true)) {//智能识别打开操作
            Vog.d("开启 -- smartOpen")
            if (cmdWord != "") {//使用smartOpen
//                    val q = PriorityQueue<Action>()
                //设置command
                val engine = MultiExecutorEngine()
                val result = engine.use {
                    it.command = cmdWord
                    it.smartOpen(cmdWord)
                }
                if (result) {//成功打开
                    Vog.d("parseAction ---> MultiExecutorEngine().smartOpen(cmdWord) 成功打开")
                    return ActionParseResult(true, PriorityQueue(), "smartOpen $cmdWord")
                }
            }
            Vog.d("smartOpen --无匹配")
        }

        //APP内
        val appResult = parseAppActionWithScope(cmdWord, scope)
        if (appResult.isSuccess) return appResult

        //点击
        if (SpHelper(GlobalApp.APP).getBoolean("use_smartopen_if_parse_failed",
                        true) && AccessibilityApi.isBaseServiceOn) {//失败,默认点击
            if (ViewFindBuilder().similaryText(cmdWord).findFirst()?.tryClick() == true)
                return ActionParseResult(true, PriorityQueue(), "smart点击 $cmdWord")
        }
        //失败
        return ActionParseResult(false)
    }

    /**
     * 从指令解析应用内操作
     *
     * @param cmd String 指令
     * @param scope ActionScope? 指定应用
     * @return ActionParseResult
     */
    fun parseAppActionWithScope(cmd: String, scope: ActionScope?, isFollow: Boolean = false): ActionParseResult {
        if (scope == null) {
            Vog.d("scope is null 无障碍未打开")
            return ActionParseResult(false, null, "无匹配(未匹配应用内操作)")
        }

        val inAppQue = matchAppAction(cmd, scope, isFollow)
        val actionQueue = inAppQue.second //匹配应用内时
        // 根据第一个action.scope 决定是否进入首页
        if (actionQueue.isNotEmpty()) {//自动执行打开
            //TODO check
//                AppBus.post(AppBus.EVENT_HIDE_FLOAT)//关闭助手dialog
            return ActionParseResult(true, actionQueue, inAppQue.first,
                    SystemBridge.getAppInfo(scope.packageName))
                    .insertOpenAppAction(scope)
        }
        return ActionParseResult(false)
    }

    /**
     * 匹配App内指令  根据[包名]
     * 或在执行打开应用后，解析跟随指令
     * eg:
     * 网易云 音乐 播放
     * QQ扫一扫
     * App内指令
     * 深度搜索
     * @param isFollow 此时匹配应用内指令，指明是前面是否有指令，true: 有父级操作 false: 首个操作
     * 用于判断是否加前缀%
     * @return  Pair<String?, PriorityQueue<Action> 匹配的actionTitle 运行队列
     */
    fun matchAppAction(cmd: String, matchScope: ActionScope, isFollow: Boolean = false): Pair<String?, PriorityQueue<Action>> {

        val actionQueue = PriorityQueue<Action>()
        AppActionNodes.filter {
            //筛选当前App  根据pkg
            val sc = it.actionScope
            sc?.eqPkg(matchScope) == true
        }.forEach {
            val r = regSearch(cmd, it, actionQueue, isFollow)
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
    private fun regSearch(cmd: String, it: ActionNode, actionQueue: PriorityQueue<Action>,
                          isFollow: Boolean): Boolean {
        it.regs.forEach { reg ->
            val result = (if (isFollow) reg.followRegex else reg.regex).matchEntire(cmd)//？？？？
            if (result != null) {
                val ac = it.action
                ac.scope = it.actionScope
                ac.param = it.param
                extractParam(ac, reg, result)//提取参数
                ac.matchWord = result.groupValues[0]
                actionQueue.add(ac)
                //深搜命令
//                actionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], ac)
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
        node.follows.forEach FollowsForEach@{ it ->
            it?.regs?.forEach RegForEach@{ reg ->
                val result = reg.followRegex.matchEntire(sufWord)
                if (result != null && result.groups.isNotEmpty()) {//深搜
//                    println("--匹配成功")
                    //匹配成功
                    if (preAction != null) {//修剪上一个匹配结果参数,第一个%即为上一个参数
                        preAction.param?.value?.withIndex()?.forEach f@{ kv ->
                            val p = kv.value ?: return@f
                            if (result.groupValues[0].startsWith(p)) {//end
                                val preParamLen = if (preAction.param == null) 0
                                else result.groupValues[1].length
                                val thisMatchLen = result.groupValues[0].length
                                preAction.param?.value!![kv.index] = p.substring(0, preParamLen)
                                val allLen = preAction.matchWord.length
                                preAction.matchWord = preAction.matchWord
                                        .substring(0, allLen - (thisMatchLen - preParamLen))
                                return@RegForEach
                            }
                        }
                    }
                    val itsAction = it.action
                    extractParam(itsAction, reg, result)//提取参数
                    //println("--临时提取参数：$param -${it.param!!.desc}")

//                    itsAction.matchWord = result.groupValues[0]
//                            .substring(preAction?.param?.value?.length ?: 0)//
                    actionQueue.add(itsAction)
                    return if (it.follows.isNotEmpty()) {//不空
                        actionDsMatch(actionQueue, it, result.groupValues[result.groupValues.size - 1], itsAction)//递归匹配
                    } else true
                }
            }
        }
        return preAction?.param != null
    }

    /**
     * 全局命令
     * 一级匹配
     * 全局不存在follows
     */
    private fun globalActionMatch(cmd: String): ActionParseResult {
        val actionQueue = PriorityQueue<Action>()
        GlobalActionNodes.forEach {
            val r = regSearch(cmd, it, actionQueue, false)
            if (r) return ActionParseResult(true, actionQueue, it.actionTitle)
        }
        return ActionParseResult(false)
    }


    //提取参数
    private fun extractParam(it: Action, reg: Reg, result: MatchResult) {
        val param = it.param
        if (param != null) {//设置参数
            param.value = Array<String?>(reg.paramPosArray?.size ?: 0, init = { null })
            reg.paramPosArray?.withIndex()?.forEach {
                when (it.value) {
                    PARAM_POS_END -> {
                        param.value[it.index] = getLastParam(result.groups)
                    }
                    PARAM_POS_1, PARAM_POS_2, PARAM_POS_3 ->
                        param.value[it.index] = result.groups[it.value]?.value
                }
            }
            it.param = param
//            Vog.d("extractParam $param")
        }
    }

    fun getLastParam(colls: MatchGroupCollection): String? {
        return colls.reversed()[0]?.value
//        colls.reversed().withIndex().forEach { iv ->
//            val it = iv.value
//            if (it != null && it.value != "" && iv.index != colls.size - 1) {//不是第一个
//                return it.value
//            }
//        }
//        return null
    }


    /**
     * 解析测试
     * @param testWord String
     * @param node ActionNode 节点
     * @return ActionParseResult
     */
    fun testParse(testWord: String, node: ActionNode): ActionParseResult {
        val actionQueue = PriorityQueue<Action>()
        regSearch(testWord, node, actionQueue, false)
        return ActionParseResult(actionQueue.isNotEmpty(), actionQueue)
    }

}

class OpenAppAction(val pkg: String) {

    //重置App
    val PRE_OPEN = "openAppByPkg('%s',true)\n"

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Action {
        return Action(-999, String.format(PRE_OPEN, pkg)
                , Action.SCRIPT_TYPE_LUA)
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
