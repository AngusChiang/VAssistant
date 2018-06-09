package cn.vove7.accessibilityservicedemo.model;

import android.graphics.drawable.Drawable;

public class AppInfo {
   String name;
   String packageName;
   Drawable icon;
   int pid;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getPackageName() {
      return packageName;
   }

   public void setPackageName(String packageName) {
      this.packageName = packageName;
   }

   public Drawable getIcon() {
      return icon;
   }

   public void setIcon(Drawable icon) {
      this.icon = icon;
   }

   public int getPid() {
      return pid;
   }

   public void setPid(int pid) {
      this.pid = pid;
   }
}