package cn.vove7.common.datamanager

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.greendao.RegDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import java.util.*

/**
 * # DaoHelper
 *
 * @author 17719247306
 * 2018/8/25
 */

typealias OnUpdate = (Int) -> Unit

object DaoHelper {
    fun deleteActionNodes(ids: Array<Long>, d: OnUpdate? = null) {
        var i = 0
        ids.forEach {
            deleteActionNode(it)
            d?.invoke(i++)
        }
    }

    /**
     * 删除指定ActionNode
     * @param nodeId Long
     * @return Boolean
     */
    fun deleteActionNode(nodeId: Long): Boolean {
        //TODO 删除记录 Action Reg --ActionScope-- 判断follow
        //事务开始
        try {
            DAO.daoSession.runInTx {
                val ancNode = DAO.daoSession.actionNodeDao.queryBuilder().where(ActionNodeDao.Properties.Id.eq(nodeId)).unique()
                val delFollows = LinkedList<ActionNode>()
                delFollows.add(ancNode)

                while (delFollows.isNotEmpty()) {
                    val p = delFollows.poll()
                    //添加follows至队列
                    delFollows.addAll(DAO.daoSession.actionNodeDao.queryBuilder().where(ActionNodeDao.Properties.ParentId.eq(p.id)).list())
                    //Action
                    val a = p.action
                    if (!a.isNull)
                        DAO.daoSession.actionDao.delete(a)

                    //Regs
                    DAO.daoSession.regDao.deleteInTx(
                            DAO.daoSession.regDao.queryBuilder()
                                    .where(RegDao.Properties.NodeId.eq(p.id))
                                    .list()
                    )
//                    val scope = p.actionScope
//                    if (scope != null) {
//                        DAO.daoSession.actionScopeDao.delete(scope)
//                    }
                    DAO.daoSession.actionNodeDao.delete(p)
                }
            }
            //事务结束
            return true
        } catch (e: Exception) {
            GlobalLog.err(e.message + "code: dh42")
            return false
        }
    }

    /**
     * 更新Marked数据
     * 删除来自服务器,保留用户数据
     * TODO tagid 防止删除重叠数据
     * @param types Array<String>
     * @param datas List<MarkedData>
     * @return Boolean
     */
    fun updateMarkedData(types: Array<String>, datas: List<MarkedData>): Boolean {
        val markedDao = DAO.daoSession.markedDataDao
        val l = markedDao.queryBuilder().where(
                MarkedDataDao.Properties.Type.`in`(*types),MarkedDataDao.Properties.From.notIn(DataFrom.FROM_USER)).list()
        return try {
            markedDao.deleteInTx(l)
            markedDao.insertInTx(datas)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 更新广告数据
     * 删除来自服务器,保留用户数据
     * TODO tagid 防止删除重叠数据
     * @param types Array<String>
     * @param datas List<AppAdInfo>
     * @return Boolean
     */
    fun updateAppAdInfo(datas: List<AppAdInfo>): Boolean {
        val appAdInfoDao = DAO.daoSession.appAdInfoDao
        appAdInfoDao.deleteAll()
        return try {
            appAdInfoDao.insertInTx(datas)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}