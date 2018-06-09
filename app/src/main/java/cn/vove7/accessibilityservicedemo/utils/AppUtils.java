package cn.vove7.accessibilityservicedemo.utils;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.ArrayList;
import java.util.List;

import static cn.vove7.accessibilityservicedemo.utils.PermissionUtils.accessibilityServiceEnabled;
import static cn.vove7.accessibilityservicedemo.utils.PermissionUtils.showPackageDetail;

public class AppUtils {

   public static List<AndroidAppProcess> getRunningAppInfos() {
      return AndroidProcesses.getRunningAppProcesses();
   }

   private static final List<String> whiteAppList = new ArrayList<>();

   static {
      whiteAppList.add("com.android.systemui");
      whiteAppList.add("cn.vove7.accessibilityservicedemo");
   }

   public static void cleanProcess(Context context) {
      if (!accessibilityServiceEnabled(context)) {
         Toast.makeText(context, "无障碍未开启", Toast.LENGTH_SHORT).show();
         return;
      }
      List<AndroidAppProcess> appInfos = AppUtils.getRunningAppInfos();
      Log.d("后台数量：", String.valueOf(appInfos.size()));
      for (AndroidAppProcess info : appInfos) {
         if (!isInWhite(info.getPackageName())) {
            Log.d("clean app--->", info.name);
            showPackageDetail(context, info.getPackageName());
         }
      }
   }

   private static boolean isInWhite(String pkg) {
      for (String p : whiteAppList) {
         if (p.equals(pkg)) {
            return true;
         }
      }
      return false;
   }

}
