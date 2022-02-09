
#####方法名等混淆指定配置
-obfuscationdictionary ../proguard_keywords.txt
#####类名混淆指定配置
-classobfuscationdictionary ../proguard_keywords.txt
#####包名混淆指定配置
-packageobfuscationdictionary ../proguard_keywords.txt

-optimizationpasses 5

# 混淆时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames


# LoggerKt.logd$default
# 指定不去忽略非公共的库的类
-dontskipnonpubliclibraryclasses

# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
# 不做预校验，preverify是proguard的4个步骤之一
# Android不需要preverify，去掉这一步可加快混淆速度


# 有了verbose这句话，混淆后就会生成映射文件
# 包含有类名->混淆后类名的映射关系
# 然后使用printmapping指定映射文件的名称
-verbose
-printmapping proguardMapping.txt

-dontskipnonpubliclibraryclasses
#用于告诉ProGuard，不要跳过对非公开类的处理。默认情况下是跳过的，因为程序中不会引用它们，有些情况下人们编写的代码与类库中的类在同一个包下，并且对包中内容加以引用，此时需要加入此条声明。

-dontusemixedcaseclassnames
#，这个是给Microsoft Windows用户的，因为ProGuard假定使用的操作系统是能区分两个只是大小写不同的文件名，但是Microsoft Windows不是这样的操作系统，所以必须为ProGuard指定-dontusemixedcaseclassnames选项
# 枚举类不能被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# 保留Parcelable序列化的类不能被混淆
-keep class * implements android.os.Parcelable{
    public static final android.os.Parcelable$Creator *;
}

# 移除android 所有log
-assumenosideeffects class cn.vove7.android.common.LoggerKt{
    public static *** logv$default(...);
    public static *** log$default(...);
    public static *** logd$default(...);
    public static *** loge$default(...);
    public static *** logi$default(...);
    public static *** logw$default(...);
    public static *** logv(...);
    public static *** log(...);
    public static *** logd(...);
    public static *** loge(...);
    public static *** logi(...);
    public static *** logw(...);
}
-assumenosideeffects class cn.vove7.android.common.Logger {
    public *** logv(...);
    public *** log(...);
    public *** logd(...);
    public *** loge(...);
    public *** logi(...);
    public *** logw(...);
}

-assumenosideeffects class cn.vove7.vtp.log.Vog {*;}

-dontnote com.google.android.material.**
-dontwarn androidx.**

-flattenpackagehierarchy
-ignorewarnings



-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers enum * { *;}
-keep class * extends java.lang.Exception

#Gson
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                LineNumberTable,*Annotation*,EnclosingMethodCEx
-keep class com.google.gson.** {*;}

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class cn.vove7.smartkey.** { *; }
-keep class com.baidu.** { *; }
-keep class com.luajava.** { *; }

# Kotlin 运行时检查；字符串混淆可不开启
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** checkNotNull(...);
    public static *** checkParameterIsNotNull(...);
    public static *** checkNotNullParameter(...);
    public static *** checkNotNullExpressionValue(...);
    public static *** checkReturnedValueIsNotNull(...);
    public static *** checkExpressionValueIsNotNull(...);
    public static *** throwUninitializedPropertyAccessException(...);
}

-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keepclassmembers class * extends androidx.databinding.ViewDataBinding {
    public static <methods>;
}

# viewbinding
-keepclassmembers class * extends androidx.viewbinding.ViewBinding {
    public static <methods>;
}
#-keep class cn.vove7.jarvis.databinding.ActivityRealMainBinding{*;}

-keep class cn.vove7.common.bridges.** { *; }
-keepclassmembers public class cn.vove7.common.bridges.**
-keep class cn.vove7.common.net.model.** { *; }
-keep class cn.vove7.common.app.AppConfigKt { *; }
-keep class cn.vove7.common.app.AppConfig { *; }
-keep class cn.vove7.common.view.finder.** { *; }
-keepclassmembers public class cn.vove7.common.view.finder.**

-keep class cn.vove7.common.model.** { *; }
-keep class cn.vove7.common.accessibility.** { *; }
-keep class cn.vove7.common.appbus.AppBus { *; }
-keep class cn.vove7.vtp.text.TextTransHelper { *; }
-keep class cn.vove7.vtp.sharedpreference.SpHelper { *; }
-keep class cn.vove7.common.utils.TextHelper { *; }
-keep class cn.vove7.common.utils.TextDateParser { *; }
-keep class cn.vove7.vtp.builder.BundleBuilder { *; }
-keep class cn.vove7.executorengine.ExecutorImpl { *; }
-keep interface cn.vove7.common.executor.CExecutorI { *; }

# kt 反射属性字段
-keep class cn.vove7.vtp.app.AppInfo { *; }
-keep class cn.vove7.vtp.system.DeviceInfo { *; }
-keep class cn.vove7.vtp.system.ScreenInfo { *; }
-keep class cn.vove7.vtp.net.* {*;}


-keep class cn.vove7.common.datamanager.** { *; }
-keep class cn.vove7.androlua.** { *; }
-keep class cn.vove7.rhino.** { *; }
-keep class cn.vove7.common.accessibility.viewnode.** { *; }
-keep class cn.vove7.jarvis.tools.timedtask.** { *; }

-keep class org.mozilla.** { *; }
-keep class org.json.** { *; }
-keep class org.apache.** { *; }
-keep class com.catchingnow.icebox.** { *; }

-keepclassmembers class cn.vove7.executorengine.exector.ExecutorEngine

-keep class okhttp3.* {*;}

-keep @org.greenrobot.greendao.annotation.Entity class * { *; }

-keep class * extends org.greenrobot.greendao.AbstractDao { *; }

-keep class com.aispeech.** {*;}

# 脚本使用
-keep class kotlin.Pair {*;}