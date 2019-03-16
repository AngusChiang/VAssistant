package cn.vove7.common.datamanager

import android.graphics.Color
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

typealias OnUpdate = (Int, String) -> Unit

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
            GlobalLog.err(e)
            false
        }
    }

    /**
     *
     * @param node ActionNode
     * @return Int
     */
    fun insertNewActionNodeInTx(node: ActionNode): Boolean {
        return try {
            DAO.daoSession.runInTx {
                insertNewActionNode(node)
            }
            true
        } catch (e: Exception) {
            GlobalLog.err(e.message)
            false
        }
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
    fun updateGlobalInst(nodes: List<ActionNode>, onUpdate: OnUpdate? = null): Boolean {
        //
        return updateActionNodeByType(nodes, ActionNode.NODE_SCOPE_GLOBAL, onUpdate)
    }

    /**
     * 更新InApp命令
     * 删除 scopeType is Global and not from user
     * @param nodes List<ActionNode>
     */
    fun updateInAppInst(nodes: List<ActionNode>, onUpdate: OnUpdate? = null): Boolean {
        return updateActionNodeByType(nodes, ActionNode.NODE_SCOPE_IN_APP, onUpdate)
    }

    /**
     * 检查是否更新 检查是否删除  tag 更新不变
     * 第一级增量更新  比较数据集  follows 格式化更新
     * old(a1 b1 c1) new(a1 b2 d1)
     * 删除old-new  del: c1
     * 更新old,new并集 .forEach 升级版本高的  up: b2
     * 插入new-old ins: d1
     * @param newNodes List<ActionNode>
     * @param type Int
     * @param onUpdate OnUpdate?
     * @return Boolean
     */
    private fun updateActionNodeByType(newNodes: List<ActionNode>, type: Int, onUpdate: OnUpdate? = null): Boolean {
        val actionNodeDao = DAO.daoSession.actionNodeDao

        //已同步（其他人分享）的云端数据记录
        //select * from action_node where from != from_user and scopeType=type and (pud is null or pud != uid)
        val localSyncedDataSet = actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.From.notEq(DataFrom.FROM_USER),
                        ActionNodeDao.Properties.ActionScopeType.eq(type),
                        actionNodeDao.queryBuilder().or(ActionNodeDao.Properties.PublishUserId.isNull,
                                ActionNodeDao.Properties.PublishUserId.notEq(UserInfo.getUserId()
                                    ?: -1L)
                        )
                ).list().toHashSet()

        //用户分享的数据 可能未审核
        val userSharedDataSet = if (UserInfo.isLogin()) {
            actionNodeDao.queryBuilder().where(ActionNodeDao.Properties.From.eq(DataFrom.FROM_SHARED),
                    ActionNodeDao.Properties.ActionScopeType.eq(type),
                    ActionNodeDao.Properties.PublishUserId.eq(UserInfo.getUserId() ?: -1L))
                    .list().toHashSet()
        } else emptySet<ActionNode>()
        return try {
            //old(a1 b1 c1 d1s) new(a1 b2 d1 [d1s])
            //删除old-new  del: c1 保留d1s
            //更新old,new并集 .forEach 升级版本高的  up: b2
            //插入new-old ins: d1
            DAO.daoSession.runInTx {
                //del old-new  exclude user shared
                localSyncedDataSet.toHashSet().subtract(newNodes).filter { !userSharedDataSet.contains(it) }.forEach {
                    //del old - new
                    //删除旧服务器数据
                    //del old global
//                    if (BuildConfig.DEBUG) {
                    onUpdate?.invoke(Color.BLACK, "删除旧指令：${it.actionTitle}")
//                    }
                    deleteActionNode(it.id)
                }
                newNodes.filter { userSharedDataSet.contains(it) || localSyncedDataSet.contains(it) }.forEach {
                    //up 交集 new and user
                    //交集
//                    if (!userDataSet.contains(it)) {
//                        onUpdate?.invoke(Color.GREEN, "更新指令：${it.actionTitle}")
//                        onUpdate?.invoke(Color.YELLOW, it.desc?.instructions ?: "无描述")
//                        Vog.d("updateGlobalInst 添加---> ${it.actionTitle}")
//                        insertNewActionNode(it)
//                    } else {//存在
                    checkUpgradeNode(it, onUpdate)
//                    }
                }
                newNodes.toHashSet().subtract(localSyncedDataSet).subtract(userSharedDataSet).filter {
                    !userSharedDataSet.contains(it)  //不更新本用户分享的
                }.forEach {
                    //new - old - user insert
                    insertNewActionNode(it, onUpdate)
                }
            }
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            onUpdate?.invoke(Color.BLACK, "错误：${e.message}")
            false
        }
    }

    //检查升级 ，follows
    private fun checkUpgradeNode(node: ActionNode, onUpdate: OnUpdate? = null) {
        val oldNode = getActionNodeByTag(node.tagId)
        if (oldNode == null || node.versionCode > oldNode.versionCode) {//更新
            if (oldNode != null) deleteActionNode(oldNode.id)//完全删除
            Vog.d("updateGlobalInst 更新---> ${node.actionTitle}")
            onUpdate?.invoke(Color.GREEN, "升级指令：${node.actionTitle}")
            onUpdate?.invoke(Color.YELLOW, node.desc?.instructions ?: "无描述")
            insertNewActionNode(node) //插入
        } else {
            //更新follows  删除本地  插入
            //a1,b3  a1,c2
            Vog.d("updateGlobalInst 存在---> ${node.actionTitle}")

            oldNode.follows.forEach {
                //完全删除 old fs
                deleteActionNode(it.id)
            }
            for (ci in node.follows) {//递归 深度
                ci.parentId = oldNode.id  //node 未更新  更新fs
                insertNewActionNode(ci, onUpdate)
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
    fun insertNewActionNode(newNode: ActionNode, onUpdate: OnUpdate? = null): Long? {
        newNode.id = null
        var newScopeId: Long = -1
        onUpdate?.invoke(Color.GREEN, "添加：${newNode.actionTitle}")
        if (newNode.actionScope != null) {
            val scope = newNode.actionScope
            Vog.d("save sid: $newScopeId")
            newScopeId = getActionScopeId(scope)
        }

        if (newScopeId != -1L)
            newNode.setScopeId(newScopeId)

        newNode.desc?.apply {
            DAO.daoSession.actionDescDao.insert(this)
            newNode.descId = this.id
        }

        val action = newNode.action
        if (action != null) {
            DAO.daoSession.actionDao.insert(action)
            Vog.d("save sid: ${action.id}")
            newNode.setActionId(action.id)
        }
        Vog.d("insertNewActionNode ---> 插入 ${newNode.actionTitle}")

        DAO.daoSession.actionNodeDao.insert(newNode)
        Vog.d("save nodeId: ${newNode.id}")

        newNode.regs.forEach {
            it.id = null
            it.nodeId = newNode.id
            DAO.daoSession.regDao.insert(it)
        }

        //childs
        if (newNode.follows?.isNotEmpty() == true)
            for (ci in newNode.follows) {//递归 深度
                Vog.d("insertNewActionNode ---> 检查 child ${ci.actionTitle}")
                ci.parentId = newNode.id
                insertNewActionNode(ci)
            }
        return newNode.id
    }

    /**
     * 更新Marked数据  无版本 每分享一次有新tag
     * 删除来自服务器,保留用户数据
     * 增量更新
     * tag : 1 2 3 5u -> 1 2 4  [5u]
     * 删除old-new  del: 3 exclude user 5
     * 插入new-old ins: 4
     * @param types Array<String>
     * @param datas List<MarkedData>
     * @return Boolean
     */
    fun updateMarkedData(onUpdate: OnUpdate? = null, types: Array<String>, datas: List<MarkedData>): Boolean {
        val markedDao = DAO.daoSession.markedDataDao
        //本地同步(其他人分享)的数据
        val localSyncedDataSet = markedDao.queryBuilder().where(
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
        //用户自己 可能未审核
        val userList = markedDao.queryBuilder().where(
                MarkedDataDao.Properties.Type.`in`(*types),
                markedDao.queryBuilder().or(
                        MarkedDataDao.Properties.From.eq(DataFrom.FROM_USER),
                        MarkedDataDao.Properties.PublishUserId.eq(UserInfo.getUserId() ?: -1L)
                )
        ).list().toHashSet()
        return try {
            //del old-new
            localSyncedDataSet.toHashSet().subtract(datas).filter {
                !userList.contains(it)//排除用户的
            }.forEach {
                onUpdate?.invoke(Color.BLACK, "删除：${it.key}")
                markedDao.deleteInTx(it)
            }
            //up new - old
            datas.subtract(localSyncedDataSet).filter {
                !userList.contains(it) //排除用户的
            }.forEach {
                it.id = null
                onUpdate?.invoke(Color.GREEN, "更新：${it.key}")
                markedDao.insert(it)
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
    fun updateAppAdInfo(datas: List<AppAdInfo>, onUpdate: OnUpdate? = null): Boolean {
        val appAdInfoDao = DAO.daoSession.appAdInfoDao
        return try {
            //删除服务端
            val localSyncedDataSet = appAdInfoDao.queryBuilder().where(
                    AppAdInfoDao.Properties.From.notEq(DataFrom.FROM_USER),
                    appAdInfoDao.queryBuilder().or(AppAdInfoDao.Properties.PublishUserId.isNull,
                            AppAdInfoDao.Properties.PublishUserId.notEq(UserInfo.getUserId()
                                ?: -1L))
            ).list().toHashSet()
            val userList = appAdInfoDao.queryBuilder().whereOr(
                    AppAdInfoDao.Properties.From.eq(DataFrom.FROM_USER),
                    AppAdInfoDao.Properties.PublishUserId.eq(UserInfo.getUserId() ?: -1L)
            ).list().toHashSet()

            //del old-new
            localSyncedDataSet.toHashSet().subtract(datas).filter {
                !userList.contains(it)
            }.forEach {
                onUpdate?.invoke(Color.BLACK, "删除 ${it.descTitle}")
                appAdInfoDao.deleteInTx(it)
            }
            //up new - old
            datas.subtract(localSyncedDataSet).filter {
                !userList.contains(it)
            }.forEach {
                it.id = null
                onUpdate?.invoke(Color.GREEN, "更新标记广告：${it.descTitle}")
                appAdInfoDao.insertInTx(it)
                Vog.d("updateAppAdInfo ---> ${it.descTitle} ${it.id}")
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

    /**
     * ActionNode相似数据
     * @param new ActionNode
     * @return ActionNode?
     */
    fun getSimActionNode(new: ActionNode): ActionNode? {//Sim????  相似
        return DAO.daoSession.actionNodeDao.queryBuilder()
                .where(ActionNodeDao.Properties.ActionTitle.eq(new.actionTitle ?: ""),
                        ActionNodeDao.Properties.From.eq(DataFrom.FROM_USER),
                        ActionNodeDao.Properties.ActionScopeType.eq(new.actionScopeType)
                ).unique()
    }

    /**
     * MarkedData相似数据
     * @param new MarkedData
     * @return MarkedData?
     */
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
