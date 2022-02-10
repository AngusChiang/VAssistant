package cn.vove7.executorengine.parse

import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_GLOBAL
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.executorengine.model.ActionParseResult
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.*
import java.util.*
import kotlin.reflect.KProperty

/**
 * # ParseEngine
 * - 提取变量值 [%cmd] [%var%]
 * -
 * Created by Vove on 2018/6/15
 */
object ParseEngine {
    private var GlobalActionNodes: List<ActionNode> = mutableListOf()
        get() {
            if (field.isEmpty()) updateGlobal()
            return field
        }

    private var AppActionNodes: List<ActionNode> = mutableListOf()
        get() {
            if (field.isEmpty()) updateInApp()
            return field
        }

    init {
        updateNode()
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
                .orderDesc(ActionNodeDao.Properties.Priority)//按优先级
                .list()
    }

    /**
     * 同步后，更新数据
     */
    fun updateNode() {
        launch {
            updateInApp()
            updateGlobal()
        }
    }


    /**
     * 匹配 ，返回操作
     * @return @see [ActionParseResult]
     * 语音命令转执行步骤顺序问题，方便排序
     * 0>1>..>9
     *
     * 命令    ↓
     * App内   ↓ -> 扫一扫/ 不在指定Activity -> （有跟随指令）跳至首页
     * 全局命令 ↓
     * 使用打开 ->
     * 点击操作
     *
     * todo 顺序 小 -> 大
     * @param scope 当前手机界面信息
     */
    fun parseAction(cmdWord: String, scope: ActionScope?,
                    smartOpen: (String) -> ActionParseResult,
                    click: (String) -> ActionParseResult,
                    lastLocation: Int = 0
    ): ActionParseResult {
        //APP内
        val appResult = parseAppActionWithScope(cmdWord, scope)
        if (appResult.isSuccess) return appResult

        val globalResult = globalActionMatch(cmdWord, lastLocation)
        if (globalResult.isSuccess) {
            return globalResult
        }
        Vog.d("globalAction --无匹配")
        //smartOpen
        val sor = smartOpen.invoke(cmdWord)
        if (sor.isSuccess) {
            return sor
        }

        //点击
        val cr = click.invoke(cmdWord)
        if (cr.isSuccess) {
            return cr
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

        val matchedNode = matchAppAction(cmd, scope, isFollow)
        if (matchedNode != null) {
            val actionQueue = PriorityQueue<Action>()
            actionQueue.add(matchedNode.action)//匹配应用内时
            // 根据第一个action.scope 决定是否进入首页
            return ActionParseResult(true, actionQueue, matchedNode.actionTitle,
                    SystemBridge.getAppInfo(scope.packageName), 0).also {
                //自动执行打开
                if (matchedNode.autoLaunchApp) it.insertOpenAppAction(scope)
            }
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
     * @param isFollow 此时匹配应用内指令，指明是前面是否有指令，true: 有父级操作 false: 首个操作
     * 用于判断是否加前缀%
     * @return  Pair<String?, PriorityQueue<Action> 匹配的actionTitle 运行队列
     */
    fun matchAppAction(cmd: String, matchScope: ActionScope, isFollow: Boolean = false): ActionNode? = runBlocking {
        AppActionNodes.filter {
            //筛选当前App  根据pkg
            val sc = it.actionScope
            sc?.eqPkg(matchScope) == true
        }.map {
            async {
                ensureActive()
                val r = regSearch(cmd, it, isFollow)
                ensureActive()
                if (r) it else null
            }
        }.awaitAll().filterNotNull().let {
            if (it.isEmpty()) null else it.first()
        }
    }

    /**
     * ActionNode正则匹配
     * @param cmd String
     * @param it ActionNode
     * @param actionQueue PriorityQueue<Action>
     * @return Boolean
     */
    private fun regSearch(cmd: String, it: ActionNode, isFollow: Boolean): Boolean {
        it.regs.forEach { reg ->
            val result = try {//maybe 正则格式错误
                (if (isFollow) reg.followRegex else reg.regex).let {
                    Vog.d("regSearch ${reg.regStr}")
                    it.match(cmd)
                }
            } catch (e: Exception) {
                GlobalLog.err(e)
                GlobalApp.toastError("正则解析错误，请查看日志")
                null
            }
            if (result != null) {
                val ac = it.action
                ac.scope = it.actionScope
                ac.param = result
                it.param = result
                ac.matchWord = cmd
                //深搜命令
//                actionDsMatch(actionQueue, it, result.groupValues[result.groups.size - 1], ac)
                return true
            }
        }
        return false
    }

    /**
     * 全局命令
     * 一级匹配
     * 全局不存在follows
     */
    private fun globalActionMatch(cmd: String, lastLocation: Int): ActionParseResult = runBlocking {
        if (lastLocation < 0) return@runBlocking ActionParseResult(false)

        GlobalActionNodes.subList(lastLocation, GlobalActionNodes.size).mapIndexed { index, it ->
            async {
                val r = regSearch(cmd, it, false)
                if (r) {
                    val actionQueue = PriorityQueue<Action>()
                    actionQueue.add(it.action)
                    ActionParseResult(true, actionQueue, it.actionTitle, lastGlobalPosition = index + 1)
                } else null
            }
        }.awaitAll().filterNotNull().let {
            if (it.isEmpty()) ActionParseResult(false) else it[0]
        }
    }

    /**
     * 解析测试
     * @param testWord String
     * @param node ActionNode 节点
     * @return ActionParseResult
     */
    fun testParse(testWord: String, node: ActionNode): ActionParseResult {
        val r = regSearch(testWord, node, false)
        return ActionParseResult(r, PriorityQueue<Action>().also { it.add(node.action) })
    }

}

class OpenAppAction(val pkg: String) {

    //重置App
    val PRE_OPEN = "system.openAppByPkg('%s',true)\n"

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Action {
        return Action(-999, String.format(PRE_OPEN, pkg), Action.SCRIPT_TYPE_LUA)
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
