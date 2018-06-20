package cn.vove7.parseengine.statusmap

import cn.vove7.parseengine.model.Action
import cn.vove7.parseengine.model.ActionScope
import cn.vove7.parseengine.model.Param
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_1
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_2
import cn.vove7.parseengine.statusmap.Reg.Companion.PARAM_POS_END

/**
 *
 * 生成测试图
 * Created by Vove on 2018/6/17
 */
object BuildTest {

    val MapNodes = hashMapOf<Int, MapNode>()

    init {
        val startNode = MapNode(
                id = 0,
                regs = listOf(),
                action = Action(actionScript = ""),
                follows = listOf(1, 2), param = Param())

        MapNodes[0] = startNode
        MapNodes[1] = MapNode(
                id = 1,
                regs = listOf(Reg("(用|打开|启动)%", PARAM_POS_END)),
                action = Action(actionScript = "open\n"),
                follows = listOf(3),
                param = Param(desc = "应用名")
        )
        MapNodes[3] = MapNode(
                id = 3,
                regs = listOf(
                        Reg("%给%发消息", PARAM_POS_2),
                        Reg("%发消息给%", PARAM_POS_END)
                ),
                action = Action(actionScript = ""),
                follows = listOf(4),
                param = Param(desc = "QQ操作用户名"),
                actionScope = ActionScope("QQ", "SplashActivity")
        )
        MapNodes[4] = MapNode(
                id = 4,
                regs = listOf(
                        Reg("%内容(为|是)?%", PARAM_POS_END)
                ),
                action = Action(priority = 2, actionScript = ""),
                follows = listOf(),
                param = Param(desc = "消息内容"),
                actionScope = ActionScope("QQ", "Message")
        )
        MapNodes[2] = MapNode(
                id = 2,
                regs = listOf(
                        Reg("打(电话)?给%", PARAM_POS_END),
                        Reg("给%打电话", PARAM_POS_1)
                ),
                action = Action(actionScript = "call"),
                follows = listOf(),
                param = Param(desc = "联系人姓名或手机号")
        )

    }

    fun getTest(): MapNode {
        return MapNodes[0]!!
    }

}