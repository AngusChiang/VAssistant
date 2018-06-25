package cn.vove7.datamanager.executor.entity;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by Vove on 2018/6/23
 */
public interface Marked {
   @Id
   Long id = 0L;
   @NotNull
   String key = "";
}
