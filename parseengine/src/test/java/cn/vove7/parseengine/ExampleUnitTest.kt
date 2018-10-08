package cn.vove7.parseengine

import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.Reg
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.executorengine.parse.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun regexTest() {
        val a = "([\\S\\s]*)"
        val reg = "%as%%".replace("%", a).toRegex()
    }

    @Test
    fun testParse() {//通过MapNode优先级控制
        val playNode = ActionNode("网易云播放歌曲", 1, ActionNode.NODE_SCOPE_IN_APP)
        val playSingerNode = ActionNode("网易云播放歌手的歌", 2, ActionNode.NODE_SCOPE_IN_APP)

        val playSingerSongNode = ActionNode("指定歌手的歌", 3, ActionNode.NODE_SCOPE_IN_APP)

        playSingerNode.regs = listOf(Reg("播放%的歌", Reg.PARAM_POS_1))
        playNode.regs = listOf(Reg("播放%", Reg.PARAM_POS_END))
        playSingerSongNode.regs = listOf(Reg("%的%", Reg.PARAM_POS_END))
        arrayOf(//测试数据
//                "播放许嵩的歌",
//                "播放断桥残雪",
                "播放"
//                "播放许嵩的断桥残雪" //  two ways : 1.两步解析 "播放%" -> "的%"  ; 2.脚本内解析
        ).forEach { STR ->
            arrayOf(playNode//,
                    /*playSingerNode, playSingerSongNode*/).forEach {
                println("匹配: $it")
                outputParseResult(cn.vove7.executorengine.parse.ParseEngine.testParse(STR, it))

            }
        }

    }

    private fun outputParseResult(result: cn.vove7.executorengine.parse.ParseResult) {
        if (result.isSuccess) {
            poll(result.actionQueue)
        } else {
            println("匹配失败")
        }
    }

    private fun poll(actions: PriorityQueue<Action>) {
        var p: Action
        var index = 0
        while (actions.isNotEmpty()) {
            p = actions.poll()
            println("Step-${index++}: 匹配词: ${p.matchWord} 参数: ${p.param} ")
        }
        println()
        println()
        println()
    }

}