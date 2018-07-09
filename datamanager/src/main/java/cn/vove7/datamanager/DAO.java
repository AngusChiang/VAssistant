package cn.vove7.datamanager;

import android.content.Context;

import cn.vove7.datamanager.greendao.DaoMaster;
import cn.vove7.datamanager.greendao.DaoSession;

/**
 * Created by Vove on 2018/6/23
 */
public class DAO {
   public static DaoSession daoSession;

   public static void init(Context context) {
      daoSession = DaoMaster.newDevSession(context, "DataBase.db");
   }
}