package cn.vove7.parseengine.engine

import cn.vove7.parseengine.model.ParseResult
import cn.vove7.parseengine.model.Action
import cn.vove7.parseengine.statusmap.BuildTest
import cn.vove7.parseengine.statusmap.BuildTest.MapNodes
import cn.vove7.parseengine.statusmap.MapNode
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_1
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_2
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_3
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_END
import java.util.*

/**
 * # ParseEngine
 * - 提取变量值 [%var] [%var%]
 * -
 * Created by Vove on 2018/6/15
 */
object ParseEngine {

    var i = 0
    /**
     * 根据匹配 ，返回操作
     * @return List<List<Action>> 执行顺序问题，方便排序
     * 0>1>..>9
     */
    fun parseAction(cmdWord: String): ParseResult {
        i = 0
        val startNode = BuildTest.getTest()
        val resList = PriorityQueue<Action>()
        val has = dsMatch(resList, startNode, cmdWord)
        if (resList.isEmpty()) {
            println("--无匹配")
        }
        return ParseResult(has, resList)
    }

    /**
     * 深度匹配
     */
    private fun dsMatch(resList: PriorityQueue<Action>, startNode: MapNode,
                        sufWord: String, preNode: MapNode? = null): Boolean {
        if (sufWord.isEmpty()) {
            return true
        }
//        println("${i++}. 匹配：$sufWord")
        startNode.follows.forEach { fs ->
            val it = MapNodes[fs]!!
            it.regs.forEach { reg ->
                val result = reg.regex.matchEntire(sufWord)

                if (result != null && result.groups.isNotEmpty()) {//深搜
//                    println("--匹配成功")
                    //匹配成功

                    if (preNode != null) {//修剪上一个匹配结果参数,第一个%即为上一个参数
                        val preParamLen = if (preNode.param != null) result.groupValues[1].length else 0
                        val thisMatchLen = result.groupValues[0].length
                        preNode.param?.value = preNode.param?.value?.substring(0, preParamLen)
                        val allLen = preNode.action.matchWord.length
                        preNode.action.matchWord = preNode.action.matchWord.substring(0, allLen - (thisMatchLen - preParamLen))
                    }

                    //提取参数
                    var thisNode: MapNode? = null
                    var param: String? = null
                    if (it.param != null) {//设置参数
                        when (reg.paramPos) {
                            PARAM_POS_END -> {
                                param = result.groups[result.groups.size - 1]?.value
                                thisNode = it//
                            }
                            PARAM_POS_1, PARAM_POS_2, PARAM_POS_3 ->
                                param = result.groups[reg.paramPos]?.value
                        }
                        //赋值
//                        println("--临时提取参数：$param -${it.param!!.desc}")
                    } else {
//                        println("--无需参数")
                    }
                    it.param?.value = param
                    it.action.param = it.param
                    it.action.matchWord = result.groupValues[0]
                            .substring(preNode?.param?.value?.length ?: 0)//
                    resList.add(it.action)
                    return if (it.follows.isNotEmpty()) {//不空
                        dsMatch(resList, it, result.groupValues[result.groupValues.size - 1], it)
                    } else {
                        true
                    }
                }
            }
        }
        return preNode?.param != null
    }

}