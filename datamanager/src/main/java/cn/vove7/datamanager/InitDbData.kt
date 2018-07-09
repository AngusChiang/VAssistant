package cn.vove7.datamanager

import android.util.Pair
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.MarkedOpen.MARKED_TYPE_APP
import cn.vove7.datamanager.executor.entity.MarkedOpen.MARKED_TYPE_SYS_FUN
import cn.vove7.datamanager.executor.entity.ServerContact
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.datamanager.parse.model.ActionScope
import cn.vove7.datamanager.parse.model.Param
import cn.vove7.datamanager.parse.statusmap.MapNode
import cn.vove7.datamanager.parse.statusmap.MapNode.*
import cn.vove7.datamanager.parse.statusmap.Reg
import cn.vove7.vtp.log.Vog

/**
 * 初始化服务数据/.
 * Created by Vove on 2018/6/23
 */
object InitDbData {
    fun init() {
        val serverContactDao = DAO.daoSession.serverContactDao
        if (serverContactDao.queryBuilder().count() == 0L) {
            arrayOf(
                    Pair("中国移动", Pair("(中国)?移动(客服)?", "10086")),
                    Pair("中国联通", Pair("(中国)?联通(客服)?", "10010")),
                    Pair("中国电信", Pair("(中国)?电信(客服)?", "10000"))
            ).forEach {
                val data = ServerContact()
                data.key = it.first
                data.regexStr = it.second.first
                data.value = it.second.second
                serverContactDao.insert(data)
            }
        } else Vog.d(this, "存在数据")

        val mapDao = DAO.daoSession.mapNodeDao

        if (mapDao.queryBuilder().count() <= 10L) {
            Vog.d(this, "更新数据")
            mapDao.deleteAll()
            DAO.daoSession.regDao.deleteAll()
            DAO.daoSession.actionDao.deleteAll()
            DAO.daoSession.paramDao.deleteAll()
            /**
             * action
             */
            val a1 = Action(0, "open")
            val a3 = Action("clickByDesc(返回消息,ds)\n" +
                    "waitForText(消息)\n" +
                    "dClickByIdAndText(name,消息)\n" +
                    "clickById(et_search_keyword)\n" +
                    "sleep(100)\n" +
                    "setTextById(et_search_keyword,%,1)\n" +
                    "sleep(700)\n" +
                    "clickByIdAndText(title,%)")
            val a4 = Action(2,
                    "waitForId(input)\n" +
                    "setTextById(input,%)\n" +
                    "alert(确认发送?)\n" +
                    "sleep(500)\n" +
                    "clickById(fun_btn)")
            val a2 = Action("call")

            //操作
            val a5 = Action("back")
            val a6 = Action("home")
            val a7 = Action("recent")
            val a8 = Action("notifications")

            val a9 = Action("clickByText")
            //脚本用
            val a10 = Action("clickById")
            val a11 = Action("waitForDesc(快捷入口)\r\nclickByDesc(快捷入口)\r\nclickByDesc(扫一扫 按钮)")
            val a12 = Action("waitForId(saoyisao_tv)\r\nclickById(saoyisao_tv)")

            arrayOf(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12).forEach {
                DAO.daoSession.actionDao.insert(it)
            }

            /**
             * 参数
             */
            val p1 = Param("****", "打开什么")
            val p2 = Param("联系人姓名或手机号", "想要打给谁")
            val p3 = Param("QQ操作用户名", "想要发给谁")
            val p4 = Param("消息内容", "发送什么")
            val p9 = Param("点击文本", "点击什么")

            arrayOf(p1, p2, p3, p4, p9).forEach {
                DAO.daoSession.paramDao.insert(it)
            }
            /**
             * 正则
             */
            arrayOf(//App操作 正则前加%  -> %扫一扫
                    Reg("(用|打开|启动)%", Reg.PARAM_POS_END, 1)
                    , Reg("打(电话)?给%", Reg.PARAM_POS_END, 2)
                    , Reg("给%打电话", Reg.PARAM_POS_1, 2)
                    , Reg("%给%发消息", Reg.PARAM_POS_2, 3)
                    , Reg("%发消息给%", Reg.PARAM_POS_END, 3)
                    , Reg("(内容)?(为|是)?%", Reg.PARAM_POS_END, 4)
                    , Reg("返回", Reg.PARAM_NO, 5)
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
                    MapNode(1L, a1.id, p1.id, NODE_TYPE_GLOBAL)//打开
                    , MapNode(3L, a3.id, p3.id, scrope_qq.id, "4", NODE_TYPE_IN_APP)//聊天人
                    , MapNode(4L, a4.id, p4.id, NODE_TYPE_IN_APP_2)//内容

                    , MapNode(2L, a2.id, p2.id, NODE_TYPE_GLOBAL)//电话
                    , MapNode(5L, a5.id, NODE_TYPE_GLOBAL)//返回
                    , MapNode(6L, a6.id, NODE_TYPE_GLOBAL)//主页
                    , MapNode(7L, a7.id, NODE_TYPE_GLOBAL)//通知
                    , MapNode(8L, a8.id, NODE_TYPE_GLOBAL)
                    , MapNode(9L, a9.id, p9.id, NODE_TYPE_GLOBAL)

                    , MapNode(11L, a11.id, 0, scrope_qq.id, NODE_TYPE_IN_APP)//QQ扫一扫
                    , MapNode(12L, a12.id, 0, scrope_alipay.id, NODE_TYPE_IN_APP)//支付宝扫一扫
            ).forEach {
                mapDao.insert(it)
            }
        } else Vog.d("mapDao", "mapDao存在数据")

        val markedOpenDAO = DAO.daoSession.markedOpenDao
        if (markedOpenDAO.queryBuilder().count() != 0L) {
            arrayOf(
                    MarkedOpen("手电", MARKED_TYPE_SYS_FUN, "((手电(筒)?)|(闪光灯)|(照明(灯)))", "openFlash")
                    , MarkedOpen("网易云", MARKED_TYPE_APP, "网易云音乐", "com.netease.cloudmusic")
            ).forEach {
                markedOpenDAO.insert(it)
            }
        }
        //openActivity(com.tencent.mobileqq,com.tencent.mobileqq.activity.SplashActivity)


    }

}