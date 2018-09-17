package cn.vove7.common.datamanager

import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.greendao.DaoMaster
import cn.vove7.common.datamanager.greendao.DaoSession

/**
 * DAO 管理
 * Created by Vove on 2018/6/23
 */
object DAO {
    var daoSession: DaoSession = DaoMaster.newDevSession(GlobalApp.APP, "DataBase.db")
//
//    fun init(context: Context) {
//        daoSession = DaoMaster.newDevSession(context, "DataBase.db")
//    }
}