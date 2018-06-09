package cn.vove7.accessibilityservicedemo.utils;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;

public class Utils {
   public static boolean isScreenOn(Context context) {
      PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

      return powerManager != null && powerManager.isInteractive();
   }

   public static String arr2Str(String[] arr) {
      StringBuilder builder = new StringBuilder("[");
      for (String s : arr) {
         builder.append(s).append(",");
      }
      builder.append("]");
      return builder.toString();
   }

   public static void openApplicationMarket(Context context, String packageName) {
      try {
         String str = "market://details?id=" + packageName;
         Intent localIntent = new Intent(Intent.ACTION_VIEW);
         localIntent.setData(Uri.parse(str));
         context.startActivity(localIntent);
      } catch (Exception e) {
         // 打开应用商店失败 可能是没有手机没有安装应用市场
         e.printStackTrace();
         // 调用系统浏览器进入商城
         String url = COOLAPK_URL + packageName;
         openLinkBySystem(context, url);
      }
   }

   private static final String COOLAPK_URL = "https://www.coolapk.com/apk/";
   private static void openLinkBySystem(Context context, String url) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      context.startActivity(intent);
   }
}
