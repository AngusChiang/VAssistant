package cn.vove7.common.datamanager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import cn.vove7.common.datamanager.greendao.*
import cn.vove7.vtp.log.Vog

/**
 * # DBUpdator
 *
 * @author Administrator
 * 2018/10/23
 */
class DBUpdator(context: Context, name: String) : DaoMaster.OpenHelper(context, name) {

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        super.onUpgrade(db, oldVersion, newVersion)
        Vog.d("onUpgrade ---> old $oldVersion - new $newVersion")
        if (oldVersion < newVersion) {
            MigrationHelper.migrate(
                    db, RegDao::class.java, ActionDao::class.java,
                    ActionDescDao::class.java, ActionNodeDao::class.java,
                    ActionScopeDao::class.java, AppAdInfoDao::class.java,
                    InstSettingsDao::class.java, MarkedDataDao::class.java
            )
        }

    }
}