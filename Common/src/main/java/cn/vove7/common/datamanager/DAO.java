package cn.vove7.common.datamanager;

import android.content.Context;

import cn.vove7.common.datamanager.greendao.DaoMaster;
import cn.vove7.common.datamanager.greendao.DaoSession;

/**
 * DAO 管理
 * Created by Vove on 2018/6/23
 */
public class DAO {
    public static DaoSession daoSession;

    public static void init(Context context) {
        daoSession = DaoMaster.newDevSession(context, "DataBase.db");
    }
}