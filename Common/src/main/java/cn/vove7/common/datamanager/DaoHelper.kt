package cn.vove7.common.datamanager

import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.greendao.ActionScopeDao
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.datamanager.greendao.RegDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.vtp.log.Vog
import java.util.*

/**
 * # DaoHelper
 *
 * @author 17719247306
 * 2018/8/25
 */

typealias OnUpdate = (Int) -> Unit

object DaoHelper {
    fun deleteActionNodes(ids: Array<Long>) {
        ids.forEach {
            deleteActionNode(it)
        }
    }


    fun deleteActionNodeInTX(nodeId: Long): Boolean {
        return try {
            DAO.daoSession.runInTx {
                deleteActionNode(nodeId)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除指定ActionNode
     * @param nodeId Long
     * @return Boolean
     */
    @Throws(Exception::class)
    fun deleteActionNode(nodeId: Long) {
        //TODO 删除记录 Action Reg --ActionScope-- 判断follow
//        try {
//            DAO.daoSession.runInTx {
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
//            }
//        return true
//    } catch (e: Exception)
//    {
//        GlobalLog.err(e.message + "code: dh42")
//        return false
//    }
    }

    /**
     * 更新全局命令
     * 删除 scopeType is Global and not from user
     * @param nodes List<ActionNode>
     */
    fun updateGlobalInst(nodes: List<ActionNode>): Boolean {
        //
        val actionNodeDao = DAO.daoSession.actionNodeDao
        val olds = actionNodeDao.queryBuilder().where(ActionNodeDao.Properties.From.notEq(DataFrom.FROM_USER),
                ActionNodeDao.Properties.ActionScopeType.eq(ActionNode.NODE_SCOPE_GLOBAL)).list()
        try {
            DAO.daoSession.runInTx {
                olds.forEach {
                    deleteActionNode(it.id)
                }
                nodes.forEach {
                    insertNewActionNode(it)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun insertNewActionNode(newNode: ActionNode): Long? {
        var newScopeId: Long = -1
        if (newNode.actionScope != null) {
            val scope = newNode.actionScope
            val sCode = scope.genHashCode()
            val s = DAO.daoSession.actionScopeDao.queryBuilder()
                    .where(ActionScopeDao.Properties.HashCode.eq(sCode))
                    .unique()
            newScopeId = if (s == null) {
                DAO.daoSession.actionScopeDao.insert(scope)
                scope.id
            } else s.id
        }

        if (newScopeId != -1L)
            newNode.setScopeId(newScopeId)

        val actionDesc = newNode.desc
        if (actionDesc != null) {
            DAO.daoSession.actionDescDao.insert(actionDesc)
            newNode.descId = actionDesc.id
        }

        val action = newNode.action
        if (action != null) {
            DAO.daoSession.actionDao.insert(action)
            Vog.d(this, "save sid: $newScopeId")
            newNode.setActionId(action.id)
        }

        DAO.daoSession.actionNodeDao.insert(newNode)
        Vog.d(this, "save nodeId: ${newNode.id}")

        newNode.regs.forEach {
            it.nodeId = newNode.id
            DAO.daoSession.regDao.insert(it)
        }

        return newNode.id
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
                MarkedDataDao.Properties.Type.`in`(*types), MarkedDataDao.Properties.From.notIn(DataFrom.FROM_USER)).list()
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