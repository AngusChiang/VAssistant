package cn.vove7.datamanager.parse.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * #ActionScope
 *
 * @see Action
 * <p>
 * Created by Vove on 2018/6/18
 */
@Entity()
public class ActionScope {
   @Id
   private
   Long id;
   private String appName;
   private String activity;

   @Generated(hash = 1179093725)
   public ActionScope(Long id, String appName, String activity) {
      this.id = id;
      this.appName = appName;
      this.activity = activity;
   }

   public ActionScope(String appName, String activity) {
      this.appName = appName;
      this.activity = activity;
   }

   @Generated(hash = 1143247331)
   public ActionScope() {
   }

   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getAppName() {
      return this.appName;
   }

   public void setAppName(String appName) {
      this.appName = appName;
   }

   public String getActivity() {
      return this.activity;
   }

   public void setActivity(String activity) {
      this.activity = activity;
   }
}
