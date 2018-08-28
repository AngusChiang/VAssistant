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
        val scripyType = Action.SCRIPT_TYPE_LUA

        if (mapDao.queryBuilder().count() <= 10L) {
            Vog.d(this, "更新数据")
            mapDao.deleteAll()
            DAO.daoSession.regDao.deleteAll()
            DAO.daoSession.actionDao.deleteAll()
            /**
             * action
             */
            val a1 = Action(0, "local args = { ... }\n" +
                    "if (#args >= 1) then\n" +
                    "    smartOpen(args[1])\n" +
                    "else\n" +
                    "    print(\"打开什么呦\")\n" +
                    "end\n" +
                    "\n", scripyType)
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
                            "end\n", scripyType)

            val a4 = Action(2,
                    "require 'accessibility'\n" +
                            "i = ViewFinder().id('input').waitFor()\n" +
                            "i.setText(args[1])\n" +
                            "if (alert('确认发送?', '')) then\n" +
                            "    sleep(300)\n" +
                            "    clickById('fun_btn')\n" +
                            "end", scripyType)
            val a2 = Action("local args = { ... }\n" +
                    "if (#args >= 1) then\n" +
                    "    executor.smartCallPhone(args[1])\n" +
                    "end\n", scripyType)

            //操作
            val a5 = Action(
                    "require 'accessibility'\n" + "back()", scripyType)
            val a6 = Action(
                    "require 'accessibility'\n" + "home()", scripyType)
            val a7 = Action(
                    "require 'accessibility'\n" + "recents()", scripyType)
            val a8 = Action(
                    "require 'accessibility'\n" + "notifications()", scripyType)


            val a9 = Action("require 'accessibility'\n" + "clickText(args[1])", scripyType)
            //脚本用
//            val a10 = Action("clickById(args[1])")

            val a11 = Action(
                    "require 'accessibility'\n" +
                            "\n" +
                            "k=waitForDesc('快捷入口')\n" +
                            "k.tryClick()\n" +
                            "s=waitForDesc('扫一扫 按钮')\n" +
                            "s.tryClick()", scripyType)
            val a12 = Action(
                    "require 'accessibility'\n" +
                            "ViewFinder().equalsText('首页').id('tab_description').tryClick()\n" +
                            "ViewFinder().equalsText('Home').id('tab_description').tryClick()\n" +
                            "sacn = ViewFinder().id('saoyisao_tv')\n" +
                            "\n" +
                            "sacn.waitFor().tryClick()\n", scripyType)

            arrayOf(a1, a2, a3, a4, a5, a6, a7, a8, a9, a11, a12).forEach {
                DAO.daoSession.actionDao.insert(it)
            }

            /**
             * 参数
             */
//            val p1 = ActionParam("****", "打开什么")
//            val p2 = ActionParam("联系人姓名或手机号", "想要打给谁")
//            val p3 = ActionParam("QQ操作用户名", "想要发给谁")
//            val p4 = ActionParam("消息内容", "发送什么")
//            val p9 = ActionParam("点击文本", "点击什么")

//            arrayOf(p1, p2, p3, p4, p9).forEach {
//                DAO.daoSession.paramDao.insert(it)
//            }
            /**
             * 正则
             */
            arrayOf(//App操作 正则前加%  -> %扫一扫
                    Reg("%(用|打开|启动)%", Reg.PARAM_POS_END, 1)
                    , Reg("打(电话)?给%", Reg.PARAM_POS_END, 2)
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
            ).forEach {
                DAO.daoSession.regDao.insert(it)
            }

            /**
             * 作用域
             */
            val scrope_qq = ActionScope("com.tencent.mobileqq", "SplashActivity")
            val scrope_alipay = ActionScope("com.eg.android.AlipayGphone", "AlipayLogin")
            arrayOf(scrope_qq, scrope_alipay).forEach {
                DAO.daoSession.actionScopeDao.insert(it)
            }
            arrayOf(
                    ActionNode("打开...", 1L, a1.id, NODE_TYPE_GLOBAL)//打开
                    , ActionNode("QQ选择聊天人", 3L, a3.id, scrope_qq.id, "4", NODE_TYPE_IN_APP)//QQ选择聊天人
                    , ActionNode("QQ消息内容", 4L, a4.id, NODE_TYPE_IN_APP_2, 3L)//QQ消息内容

                    , ActionNode("拨打电话", 2L, a2.id, NODE_TYPE_GLOBAL)//电话
                    , ActionNode("返回", 5L, a5.id, NODE_TYPE_GLOBAL)//返回
                    , ActionNode("主页", 6L, a6.id, NODE_TYPE_GLOBAL)//主页
                    , ActionNode("最近任务", 7L, a7.id, NODE_TYPE_GLOBAL)//最近任务
                    , ActionNode("通知栏", 8L, a8.id, NODE_TYPE_GLOBAL)//通知栏
                    , ActionNode("点击文本", 9L, a9.id, NODE_TYPE_GLOBAL)//点击文本

                    , ActionNode("QQ扫一扫", 11L, a11.id, scrope_qq.id, NODE_TYPE_IN_APP)//QQ扫一扫
                    , ActionNode("支付宝扫一扫", 12L, a12.id, scrope_alipay.id, NODE_TYPE_IN_APP)//支付宝扫一扫
            ).forEach {
                mapDao.insert(it)
            }
        } else Vog.d("mapDao", "mapDao存在数据")
    }
}