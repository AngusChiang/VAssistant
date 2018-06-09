package cn.vove7.accessibilityservicedemo;

import android.app.Application;
import android.content.Context;
import android.os.Binder;

public class MyApplication extends Application {
   static MyApplication myApplication;

   private MainActivity mainActivity;

   public static MyApplication getInstance() {
      if (myApplication == null) {
         myApplication = new MyApplication();
      }
      return myApplication;
   }

   public MainActivity getMainActivity() {
      return mainActivity;
   }

   public void setMainActivity(MainActivity mainActivity) {
      this.mainActivity = mainActivity;
   }

}
