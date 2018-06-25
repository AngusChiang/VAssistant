package cn.vove7.datamanager

import android.util.Pair
import cn.vove7.datamanager.executor.entity.MarkedOpen
import cn.vove7.datamanager.executor.entity.MarkedOpen.TYPE_APP
import cn.vove7.datamanager.executor.entity.MarkedOpen.TYPE_SYS_FUN
import cn.vove7.datamanager.executor.entity.ServerContact
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.datamanager.parse.model.ActionScope
import cn.vove7.datamanager.parse.model.Param
import cn.vove7.datamanager.parse.statusmap.MapNode
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
                    Pair("中国移动", Pair("(中国)?移动", "10086")),
                    Pair("中国联通", Pair("(中国)?联通", "10010")),
                    Pair("中国电信", Pair("(中国)?电信", "10000"))
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
            val a3 = Action("")
            val a4 = Action(2, "")
            val a2 = Action("call")

            //操作
            val a5 = Action("back")
            val a6 = Action("home")
            val a7 = Action("recent")
            val a8 = Action("pullNotification")

            val a9 = Action("clickByText")
            //脚本用
            val a10 = Action("clickById")

            arrayOf(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10).forEach {
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
            arrayOf(
                    Reg("(用|打开|启动)%", Reg.PARAM_POS_END, 1)
                    , Reg("打(电话)?给%", Reg.PARAM_POS_END, 2)
                    , Reg("给%打电话", Reg.PARAM_POS_1, 2)
                    , Reg("%给%发消息", Reg.PARAM_POS_2, 3)
                    , Reg("%发消息给%", Reg.PARAM_POS_END, 3)
                    , Reg("%内容(为|是)?%", Reg.PARAM_POS_END, 4)
                    , Reg("返回", Reg.PARAM_NO, 5)
                    , Reg("(返回|回)?(到)?主页", Reg.PARAM_NO, 6)
                    , Reg("最近应用", Reg.PARAM_NO, 7)
                    , Reg("(打开|显示|下拉)?通知栏", Reg.PARAM_NO, 8)
                    , Reg("点击%", Reg.PARAM_POS_END, 9)
            ).forEach {
                DAO.daoSession.regDao.insert(it)
            }

            /**
             * 作用域
             */
            val s3 = ActionScope("QQ", "SplashActivity")
            val s4 = ActionScope("QQ", "Message")
            arrayOf(s3, s4).forEach {
                DAO.daoSession.actionScopeDao.insert(it)
            }
            arrayOf(
                    MapNode(0L, "1,2,5,6,7,8,9,10")
                    , MapNode(1L, a1.id, "3,4", p1.id)
                    , MapNode(3L, a3.id, p3.id, s3.id)
                    , MapNode(4L, a4.id, p4.id, s4.id)
                    , MapNode(2L, a2.id, p2.id)
                    , MapNode(5L, a5.id)
                    , MapNode(6L, a6.id)
                    , MapNode(7L, a7.id)
                    , MapNode(8L, a8.id)
                    , MapNode(9L, a9.id, p9.id)
                    , MapNode(10L, a10.id)
            ).forEach {
                mapDao.insert(it)
            }
        } else Vog.d("mapDao", "mapDao存在数据")

        val markedOpenDAO = DAO.daoSession.markedOpenDao
        if (markedOpenDAO.queryBuilder().count() != 0L) {
            arrayOf(
                    MarkedOpen("手电", TYPE_SYS_FUN, "((手电(筒)?)|(闪光灯)|(照明(灯)))", "openFlash")
                    , MarkedOpen("网易云", TYPE_APP, "网易云音乐", "com.netease.cloudmusic")

            ).forEach {
                markedOpenDAO.insert(it)
            }
        }


    }

}