package cn.vove7.common.datamanager

import cn.vove7.common.R
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.*
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.vtp.log.Vog

/**
 * # DaoHelper
 *
 * @author 17719247306
 * 2018/8/25
 */

typealias OnUpdate = (Int) -> Unit

object DaoHelper {
    fun deleteActionNodesInTX(ids: Array<Long>): Boolean {

        return try {
            DAO.daoSession.runInTx {
                ids.forEach {
                    deleteActionNode(it)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     *
     * @param node ActionNode
     * @return Int
     */
    fun insertNewActionNodeInTx(node: ActionNode): Int {
        try {
            DAO.daoSession.runInTx {
                insertNewActionNode(node)
            }
        } catch (e: Exception) {
            GlobalLog.err("${e.message} code:dh48")
            return R.string.text_an_err_happened
        }
        return R.string.text_have_done
    }


    fun deleteActionNodeInTX(nodeId: Long?): Boolean {
        if (nodeId == null) return false
        return try {
            DAO.daoSession.runInTx {
                deleteActionNode(nodeId)
            }
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    /**
     * 删除指定ActionNode
     * child优先
     * @param nodeId Long
     * @return Boolean
     */
    @Throws(Exception::class)
    fun deleteActionNode(nodeId: Long) {
        GlobalLog.log("deleteActionNode ：$nodeId")

        // 删除记录 Action Reg --ActionScope-- 判断follow 保留scope
        val ancNode = DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.Id.eq(nodeId)).unique()
//        val delFollows = LinkedList<ActionNode>()
//        delFollows.add(ancNode)

//        while (delFollows.isNotEmpty()) {
        val p = ancNode
        if (p == null) {//p maybe null ???
            GlobalLog.err("delFollows.poll() -> null $p")
//                continue
            return
        }
        GlobalLog.log("deleteActionNode ---> poll ${p.actionTitle}")
        //添加follows至队列
        DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ParentId.eq(p.id))
                .list()?.forEach {
                    deleteActionNode(it?.id ?: -1L)
                }

        //开始删除
        //Action
        val a = p.actionId
        if (a > -0L)
            DAO.daoSession.actionDao.deleteByKey(a)

        //desc
        if (p.descId != null) {
            DAO.daoSession.actionDescDao.deleteByKey(p.descId)
        }

        //Regs
        DAO.daoSession.regDao.deleteInTx(
                DAO.daoSession.regDao.queryBuilder()
                        .where(RegDao.Properties.NodeId.eq(p.id))
                        .list()
        )
        DAO.daoSession.actionNodeDao.delete(p)
    }
//}

    /**
     * 更新全局命令
     * 删除 scopeType is Global and not from user
     * @param nodes List<ActionNode>
     */
    fun updateGlobalInst(nodes: List<ActionNode>): Boolean {
        //
        return updateActionNodeByType(nodes, ActionNode.NODE_SCOPE_GLOBAL)
    }

    /**
     * 更新InApp命令
     * 删除 scopeType is Global and not from user
     * @param nodes List<ActionNode>
     */
    fun updateInAppInst(nodes: List<ActionNode>): Boolean {
        return updateActionNodeByType(nodes, ActionNode.NODE_SCOPE_IN_APP)
    }

    private fun updateActionNodeByType(nodes: List<ActionNode>, type: Int): Boolean {
        //
        val actionNodeDao = DAO.daoSession.actionNodeDao

        //select * from action_node where from=from_user and scopeType=type and (pud is null or pud = uid)
        val olds = actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.From.notEq(DataFrom.FROM_USER),
                        ActionNodeDao.Properties.ActionScopeType.eq(type),
                        actionNodeDao.queryBuilder().or(ActionNodeDao.Properties.PublishUserId.isNull,
                                ActionNodeDao.Properties.PublishUserId.notEq(UserInfo.getUserId()
                                    ?: -1L)
                        )
                ).list()
        val userList = if (UserInfo.isLogin()) {
            actionNodeDao.queryBuilder().whereOr(ActionNodeDao.Properties.From.notEq(DataFrom.FROM_USER),
                    ActionNodeDao.Properties.PublishUserId.eq(UserInfo.getUserId() ?: -1L))
                    .list().toHashSet()
        } else emptySet<ActionNode>()
        return try {
            DAO.daoSession.runInTx {
                olds.forEach {
                    //删除旧服务器数据
                    //del old global
                    deleteActionNode(it.id)
                }
                nodes.forEach {
                    if (!userList.contains(it)) {
                        Vog.d(this, "updateGlobalInst 添加---> ${it.actionTitle}")
                        insertNewActionNode(it)
                    } else {//存在
                        checkNode(it)
                    }
                }
            }
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    //检查升级 ，follows
    private fun checkNode(it: ActionNode) {
        val oldNode = getActionNodeByTag(it.tagId)
        if (oldNode == null || it.versionCode > oldNode.versionCode) {//更新
            oldNode?.delete()
            Vog.d(this, "updateGlobalInst 更新---> ${it.actionTitle}")
            insertNewActionNode(it)
        } else {//检查follows
            Vog.d(this, "updateGlobalInst 存在---> ${it.actionTitle}")
            if (it.follows?.isNotEmpty() == true)
                for (ci in it.follows) {//递归 深度
                    ci.parentId = it.id
                    checkNode(ci)
                }
        }

    }

    fun getActionNodeByTag(tagId: String): ActionNode? {
        return DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.TagId.eq(tagId))
                .unique()
    }

    /**
     *
     * @param newNode ActionNode
     * @return Long? newId
     */
    fun insertNewActionNode(newNode: ActionNode): Long? {
        newNode.id = null
        var newScopeId: Long = -1
        if (newNode.actionScope != null) {
            val scope = newNode.actionScope
            Vog.d(this, "save sid: $newScopeId")
            newScopeId = getActionScopeId(scope)
        }

        if (newScopeId != -1L)
            newNode.setScopeId(newScopeId)

        val desc = newNode.desc
        if (desc != null) {
            DAO.daoSession.actionDescDao.insert(desc)
            newNode.descId = desc.id
        }

        val action = newNode.action
        if (action != null) {
            DAO.daoSession.actionDao.insert(action)
            Vog.d(this, "save sid: ${action.id}")
            newNode.setActionId(action.id)
        }
        Vog.d(this, "insertNewActionNode ---> 插入 ${newNode.actionTitle}")

        DAO.daoSession.actionNodeDao.insert(newNode)
        Vog.d(this, "save nodeId: ${newNode.id}")

        newNode.regs.forEach {
            it.id = null
            it.nodeId = newNode.id
            DAO.daoSession.regDao.insert(it)
        }

        //childs
        if (newNode.follows?.isNotEmpty() == true)
            for (ci in newNode.follows) {//递归 深度
                Vog.d(this, "insertNewActionNode ---> 检查 child ${ci.actionTitle}")
                ci.parentId = newNode.id
                insertNewActionNode(ci)
            }
        return newNode.id
    }

    /**
     * 更新Marked数据
     * 删除来自服务器,保留用户数据
     * @param types Array<String>
     * @param datas List<MarkedData>
     * @return Boolean
     */
    fun updateMarkedData(types: Array<String>, datas: List<MarkedData>): Boolean {
        val markedDao = DAO.daoSession.markedDataDao
        val l = markedDao.queryBuilder().where(
                MarkedDataDao.Properties.Type.`in`(*types),
                MarkedDataDao.Properties.From.notEq(DataFrom.FROM_USER),
                markedDao.queryBuilder().or(MarkedDataDao.Properties.PublishUserId.isNull,
                        MarkedDataDao.Properties.PublishUserId.notEq(UserInfo.getUserId() ?: -1L)
                )
                ,
                markedDao.queryBuilder().or(MarkedDataDao.Properties.PublishUserId.isNull,
                        MarkedDataDao.Properties.PublishUserId.notEq(UserInfo.getUserId()
                            ?: -1L)
                )
        ).list()
        val userList = markedDao.queryBuilder().where(
                MarkedDataDao.Properties.Type.`in`(*types),
                markedDao.queryBuilder().or(
                        MarkedDataDao.Properties.From.eq(DataFrom.FROM_USER),
                        MarkedDataDao.Properties.PublishUserId.eq(UserInfo.getUserId() ?: -1L)
                )
        ).list().toHashSet()
        return try {
            markedDao.deleteInTx(l)
            datas.forEach {
                it.id = null
                if (!userList.contains(it)) {
                    markedDao.insert(it)
                } else {
                    Vog.d(this, "updateMarkedData ---> 重复:" + it.key)
                }
            }
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    /**
     * 更新广告数据
     * 删除来自服务器,保留用户数据
     *
     * @param types Array<String>
     * @param datas List<AppAdInfo>
     * @return Boolean
     */
    fun updateAppAdInfo(datas: List<AppAdInfo>): Boolean {
        val appAdInfoDao = DAO.daoSession.appAdInfoDao
        return try {
            //删除服务端
            val delList = appAdInfoDao.queryBuilder().where(
                    AppAdInfoDao.Properties.From.notEq(DataFrom.FROM_USER),
                    appAdInfoDao.queryBuilder().or(AppAdInfoDao.Properties.PublishUserId.isNull,
                            AppAdInfoDao.Properties.PublishUserId.notEq(UserInfo.getUserId()
                                ?: -1L))
            ).list()
            appAdInfoDao.deleteInTx(delList)

            val userList = appAdInfoDao.queryBuilder().whereOr(
                    AppAdInfoDao.Properties.From.eq(DataFrom.FROM_USER),
                    AppAdInfoDao.Properties.PublishUserId.eq(UserInfo.getUserId() ?: -1L)
            ).list().toHashSet()

            datas.forEach {
                it.id = null
                if (!userList.contains(it)) {
//                    if (it.belongUser(false)) {
//                        Vog.d(this, "updateMarkedData ---> 标记为用户:" + it.descTitle)
//                        it.from = DataFrom.FROM_USER
//                    }
                    appAdInfoDao.insertInTx(it)
                    Vog.d(this, "updateAppAdInfo ---> ${it.descTitle} ${it.id}")
                } else {
                    Vog.d(this, "updateMarkedData ---> 重复:" + it.descTitle)
                }
            }
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            e.printStackTrace()
            false
        }
    }

    /**
     * 根据pkg ac 查找记录id
     * 若不存在 插入返回
     * @param pkg String
     * @param ac String?
     */
    fun getActionScopeId(scope: ActionScope): Long {
        val sCode = scope.genHashCode()
        val s = DAO.daoSession.actionScopeDao.queryBuilder()
                .where(ActionScopeDao.Properties.HashCode.eq(sCode))
                .unique()
        return if (s == null) {
            DAO.daoSession.actionScopeDao.insert(scope)
            scope.id
        } else s.id
    }

    fun getInsetSettingsByName(name: String): InstSettings? {
        return DAO.daoSession.instSettingsDao.queryBuilder()
                .where(InstSettingsDao.Properties.Name.eq(name)).unique()
    }

    fun getLocalMarkedByType(types: Array<String>): List<MarkedData> {
        val b = DAO.daoSession.markedDataDao.queryBuilder()
                .where(MarkedDataDao.Properties.From.eq(DataFrom.FROM_USER),
                        if (types.size == 1) MarkedDataDao.Properties.Type.eq(types[0])
                        else MarkedDataDao.Properties.Type.`in`(types.toList()))
        return b.list()
    }

    fun getLocalInstByType(type: Int): List<ActionNode> {
        return DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.From.eq(DataFrom.FROM_USER),
                        ActionNodeDao.Properties.ActionScopeType.eq(type),
                        ActionNodeDao.Properties.ParentId.isNull)//ParentId.isNull
                .list().also { l ->
                    //填充
                    l.forEach {
                        it.assembly2(false)
                    }
                }
    }

    fun deleteAllUserInst() {
        DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.From.eq(DataFrom.FROM_USER),
                        ActionNodeDao.Properties.ParentId.isNull)//
                .list().forEach {
                    deleteActionNode(it.id)
                }
    }

    fun deleteAllUserMarkedData() {
        DAO.daoSession.markedDataDao.queryBuilder()
                .where(MarkedDataDao.Properties.From.eq(DataFrom.FROM_USER))
                .list().forEach {
                    DAO.daoSession.markedDataDao.delete(it)
                }
    }

    fun deleteAllUserMarkedAd() {
        DAO.daoSession.appAdInfoDao.queryBuilder()
                .where(AppAdInfoDao.Properties.From.eq(DataFrom.FROM_USER)).list()
                .forEach {
                    DAO.daoSession.appAdInfoDao.delete(it)
                }
    }

    fun getSimActionNode(new: ActionNode): ActionNode? {
        return DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ActionTitle.eq(new.actionTitle ?: ""),
                        ActionNodeDao.Properties.From.eq(DataFrom.FROM_USER),
                        ActionNodeDao.Properties.ActionScopeType.eq(new.actionScopeType)
                ).unique()
    }

    fun getSimMarkedData(new: MarkedData): MarkedData? {
        return DAO.daoSession.markedDataDao.queryBuilder()
                .where(
                        MarkedDataDao.Properties.Key.eq(new.key),
                        MarkedDataDao.Properties.Type.eq(new.type),
                        MarkedDataDao.Properties.From.eq(DataFrom.FROM_USER)
                ).unique()

    }

    fun getSimMarkedAppAd(new: AppAdInfo): AppAdInfo? {
        return DAO.daoSession.appAdInfoDao.queryBuilder()
                .where(
                        AppAdInfoDao.Properties.Pkg.eq(new.pkg),
                        AppAdInfoDao.Properties.Activity.eq(new.activity),
                        AppAdInfoDao.Properties.DescTitle.eq(new.descTitle ?: "")
                ).unique()

    }
}