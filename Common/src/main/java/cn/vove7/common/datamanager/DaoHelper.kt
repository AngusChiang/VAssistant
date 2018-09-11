package cn.vove7.common.datamanager

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.greendao.RegDao
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import java.util.*

/**
 * # DaoHelper
 *
 * @author 17719247306
 * 2018/8/25
 */
object DaoHelper {
    fun deleteActionNodes(ids: Array<Long>) {
        ids.forEach {
            deleteActionNode(it)
        }
    }

    fun deleteActionNode(nodeId: Long): Boolean {
        //TODO 删除记录 Action Reg ActionScope 判断follow
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
                    DAO.daoSession.regDao.queryBuilder().where(RegDao.Properties.NodeId.eq(p.id))
                            .list().forEach {
                                DAO.daoSession.regDao.delete(it)
                            }
                    val scope = p.actionScope
                    if (scope != null) {
                        DAO.daoSession.actionScopeDao.delete(scope)
                    }
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

}