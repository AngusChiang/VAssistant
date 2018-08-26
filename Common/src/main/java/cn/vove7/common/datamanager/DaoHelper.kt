package cn.vove7.common.datamanager

/**
 * # DaoHelper
 *
 * @author 17719247306
 * 2018/8/25
 */
object DaoHelper {
    fun deleteActionNodes(ids: Array<Long>) {
        ids.forEach {
            delectActionNode(it)
        }
    }

    fun delectActionNode(nodeId: Long) {
        //TODO 删除记录 Action Reg ActionScope 判断parentId
        //事务开始


        //事务结束
    }

}