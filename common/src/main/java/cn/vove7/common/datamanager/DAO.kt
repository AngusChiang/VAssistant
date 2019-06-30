package cn.vove7.common.datamanager

import android.annotation.SuppressLint
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.greendao.DaoMaster
import cn.vove7.common.datamanager.greendao.DaoSession

@SuppressLint("StaticFieldLeak")
/**
 * DAO 管理
 * Created by Vove on 2018/6/23
 */
object DAO {
    private const val DB_NAME = "DataBase.db"

    val daoMaster: DaoMaster by lazy {
        val mDaoMaster: DaoMaster
        val helper = DBUpdator(GlobalApp.APP, DB_NAME)
        mDaoMaster = DaoMaster(helper.writableDatabase)
        mDaoMaster
    }
    val daoSession: DaoSession
        get() = daoMaster.newSession()

    fun clear() {
        daoSession.clear()
    }
}