package cn.vove7.common.datamanager

import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.*
import cn.vove7.common.datamanager.parse.statusmap.Reg
import cn.vove7.vtp.log.Vog

/**
 * 初始化服务数据/.
 * Created by Vove on 2018/6/23
 */
object InitLuaDbData : InitDbData() {
    override fun init() {
        super.init()
        val mapDao = DAO.daoSession.actionNodeDao
        val scriptType = Action.SCRIPT_TYPE_LUA

//        if (mapDao.queryBuilder().count() <= 12L) {
        Vog.d(this, "更新数据")
        mapDao.deleteAll()
        DAO.daoSession.regDao.deleteAll()
        DAO.daoSession.actionDao.deleteAll()
        DAO.daoSession.actionScopeDao.deleteAll()
        /**
         * action
         */
        val a1 = Action(0, "local args = { ... }\n" +
                "if (#args >= 1) then\n" +
                "    smartOpen(args[1])\n" +
                "else\n" +
                "    speak(\"打开什么呦\")\n" +
                "end\n" +
                "\n", scriptType)
        val a3 = Action(
                "require 'accessibility'\n" +
                        "ViewFinder().desc('返回消息').tryClick()\n" +
                        "sleep(110)\n" +
                        "msg = ViewFinder().id('name').equalsText('消息').await()\n" +
                        "msg.doubleClick()\n" +
                        "s = ViewFinder().id('et_search_keyword')\n" +
                        "s.tryClick()\n" +
                        "sleep(110)\n" +
                        "s.setTextWithInitial(args[1])\n" +
                        "sleep(110)\n" +
                        "a = ViewFinder().id('title').similaryText(args[1]).tryClick()\n" +
                        "if (not a) then\n" +
                        "    toast('没找到哦')\n" +
                        "end\n", scriptType)

        val a4 = Action(2,
                "require 'accessibility'\n" +
                        "i = ViewFinder().id('input').waitFor()\n" +
                        "i.setText(args[1])\n" +
                        "if (alert('确认发送?', '')) then\n" +
                        "    sleep(300)\n" +
                        "    clickById('fun_btn')\n" +
                        "end", scriptType)
        val a2 = Action("local args = { ... }\n" +
                "if (#args >= 1) then\n" +
                "    executor.smartCallPhone(args[1])\n" +
                "end\n", scriptType)

        //操作
        val a5 = Action(
                "require 'accessibility'\n" + "back()", scriptType)
        val a6 = Action(
                "require 'accessibility'\n" + "home()", scriptType)
        val a7 = Action(
                "require 'accessibility'\n" + "recents()", scriptType)
        val a8 = Action(
                "require 'accessibility'\n" + "notifications()", scriptType)


        val a9 = Action("require 'accessibility'\n" + "clickText(args[1])", scriptType)
        //脚本用
//            val a10 = Action("clickById(args[1])")

        val a11 = Action(
                "require 'accessibility'\n" +
                        "\n" +
                        "k=waitForDesc('快捷入口')\n" +
                        "k.tryClick()\n" +
                        "s=waitForDesc('扫一扫 按钮')\n" +
                        "s.tryClick()", scriptType)
        val a12 = Action(
                "require 'accessibility'\n" +
                        "ViewFinder().equalsText('首页').id('tab_description').tryClick()\n" +
                        "ViewFinder().equalsText('Home').id('tab_description').tryClick()\n" +
                        "sacn = ViewFinder().id('saoyisao_tv')\n" +
                        "\n" +
                        "sacn.waitFor().tryClick()\n", scriptType)
        val a13 = Action("system.volumeUp()", scriptType)
        val a14 = Action("system.volumeDown()", scriptType)
        val a15 = Action("system.mediaNext()", scriptType)
        val a16 = Action("system.mediaPre()", scriptType)
        val a17 = Action("system.mediaPause()", scriptType)
        val a18 = Action("system.mediaResume()", scriptType)
        val a19 = Action("system.mediaStop()", scriptType)

        val a20 = Action("\n" +
                "local weeks = { '日', '一', '二', '三', '四', '五', '六' }\n" +
                "local c = Calendar.getInstance()\n" +
                "local w = c.get(Calendar.DAY_OF_WEEK)\n" +
                "\n" +
                "speak('今天是星期' .. weeks[w])", scriptType)

        arrayOf(a1, a2, a3, a4, a5, a6, a7, a8, a9,
                a11, a12, a13, a14, a15, a16, a17, a18, a19, a20).forEach {
            DAO.daoSession.actionDao.insert(it)
        }

        /**
         * 正则
         */
        arrayOf(//App操作 正则前加%  -> %扫一扫
                Reg("%(用|打开|启动|开启)%", Reg.PARAM_POS_END, 1)
                , Reg("打(电话)?给%", Reg.PARAM_POS_END, 2)
                , Reg("呼叫%", Reg.PARAM_POS_END, 2)
                , Reg("%给%打电话", Reg.PARAM_POS_2, 2)
                , Reg("%给%发消息", Reg.PARAM_POS_2, 3)
                , Reg("%发消息给%", Reg.PARAM_POS_END, 3)
                , Reg("(内容)?(为|是)?%", Reg.PARAM_POS_END, 4)
                , Reg("%返回", Reg.PARAM_NO, 5)
                , Reg("(返回|回)?(到)?主页", Reg.PARAM_NO, 6)
                , Reg("(显示)?最近(应用|任务)?", Reg.PARAM_NO, 7)
                , Reg("(打开|显示|下拉)?通知栏", Reg.PARAM_NO, 8)
                , Reg("点击%", Reg.PARAM_POS_END, 9)
                , Reg("%扫一扫", Reg.PARAM_NO, 11)
                , Reg("%扫一扫", Reg.PARAM_NO, 12)
                , Reg("%大(一)?点声", Reg.PARAM_NO, 13)
                , Reg("%加大音量", Reg.PARAM_NO, 13)
                , Reg("%小(一)?点声", Reg.PARAM_NO, 14)
                , Reg("%减[小少]音量", Reg.PARAM_NO, 14)
                , Reg("%(播放)?下一首%", Reg.PARAM_NO, 15)
                , Reg("%(播放)?上一首%", Reg.PARAM_NO, 16)
                , Reg("暂停(播放)?", Reg.PARAM_NO, 17)
                , Reg("继续(播放)?", Reg.PARAM_NO, 18)
                , Reg("停止(播放)?", Reg.PARAM_NO, 19)
                , Reg("(今天)?是?(星期|周)几(了)?", Reg.PARAM_NO, 20)
        ).forEach {
            DAO.daoSession.regDao.insert(it)
        }

        /**
         * 作用域
         */
        val scrope_qq = ActionScope("com.tencent.mobileqq", "SplashActivity")
        val scrope_alipay = ActionScope("com.eg.android.AlipayGphone", "AlipayLogin")
        val scrope_netease = ActionScope("com.netease.cloudmusic", "")
        arrayOf(scrope_qq, scrope_alipay).forEach {
            DAO.daoSession.actionScopeDao.insert(it)
        }
        arrayOf(
                ActionNode("打开...", 1L, a1.id, NODE_SCOPE_GLOBAL)//打开
                , ActionNode("QQ选择聊天人", 3L, a3.id, scrope_qq.id, NODE_SCOPE_IN_APP)//QQ选择聊天人
                , ActionNode("QQ消息内容", 4L, a4.id, NODE_SCOPE_IN_APP_2, 3L)//QQ消息内容

                , ActionNode("拨打电话", 2L, a2.id, NODE_SCOPE_GLOBAL)//电话
                , ActionNode("返回", 5L, a5.id, NODE_SCOPE_GLOBAL)//返回
                , ActionNode("主页", 6L, a6.id, NODE_SCOPE_GLOBAL)//主页
                , ActionNode("最近任务", 7L, a7.id, NODE_SCOPE_GLOBAL)//最近任务
                , ActionNode("通知栏", 8L, a8.id, NODE_SCOPE_GLOBAL)//通知栏
                , ActionNode("点击文本", 9L, a9.id, NODE_SCOPE_GLOBAL)//点击文本
                , ActionNode("加大音量", 13L, a13.id, NODE_SCOPE_GLOBAL)
                , ActionNode("减少音量", 14L, a14.id, NODE_SCOPE_GLOBAL)
                , ActionNode("播放上一首", 15L, a15.id, NODE_SCOPE_GLOBAL)
                , ActionNode("播放下一首", 16L, a16.id, NODE_SCOPE_GLOBAL)
                , ActionNode("暂停播放", 17L, a17.id, NODE_SCOPE_GLOBAL)
                , ActionNode("继续播放", 18L, a18.id, NODE_SCOPE_GLOBAL)
                , ActionNode("停止播放", 19L, a19.id, NODE_SCOPE_GLOBAL)

                , ActionNode("QQ扫一扫", 11L, a11.id, scrope_qq.id, NODE_SCOPE_IN_APP)//QQ扫一扫
                , ActionNode("支付宝扫一扫", 12L, a12.id, scrope_alipay.id, NODE_SCOPE_IN_APP)//支付宝扫一扫
                , ActionNode("今天星期几", 20L, a20.id, NODE_SCOPE_GLOBAL)
        ).forEach {
            mapDao.insert(it)
        }
//        } else Vog.d("mapDao", "mapDao存在数据")
    }
}