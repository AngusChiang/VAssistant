package cn.vove7.jarvis.speech.dui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.*
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.Log
import cn.vove7.jarvis.BuildConfig

/**
 * # DuiFakePm
 *
 */
@SuppressLint("NewApi")
class DuiFakePm(val src: PackageManager) : PackageManager() {
    val TAG = "STJFakePackageManager"
    override fun getPackageInfo(versionedPackage: VersionedPackage, flags: Int): PackageInfo {
        return src.getPackageInfo(versionedPackage, flags).also{
            log("getPackageInfo $it")
        }
    }

    override fun isInstantApp(packageName: String): Boolean {
        return src.isInstantApp(packageName)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun log(s: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, s)
        }
    }

    override fun getLaunchIntentForPackage(packageName: String): Intent? {
        return src.getLaunchIntentForPackage(packageName).also {
            log("getLaunchIntentForPackage: $it")
        }
    }

    override fun getResourcesForApplication(app: ApplicationInfo): Resources {
        return src.getResourcesForApplication(app).also {
            log("getResourcesForApplication: $it")
        }
    }

    override fun getResourcesForApplication(packageName: String): Resources {
        return src.getResourcesForApplication(packageName).also {
            log("getResourcesForApplication: $it")
        }
    }

    override fun getReceiverInfo(component: ComponentName, flags: Int): ActivityInfo {
        return src.getReceiverInfo(component, flags).also {
            log("getReceiverInfo: $it")
        }
    }

    override fun queryIntentActivityOptions(
        caller: ComponentName?,
        specifics: Array<out Intent>?,
        intent: Intent,
        flags: Int
    ): MutableList<ResolveInfo> {
        return src.queryIntentActivityOptions(caller, specifics, intent, flags).also {
            log("queryIntentActivityOptions: $it")
        }
    }

    override fun getApplicationIcon(info: ApplicationInfo): Drawable {
        return src.getApplicationIcon(info).also {
            log("getApplicationIcon: $it")
        }
    }

    override fun getApplicationIcon(packageName: String): Drawable {
        return src.getApplicationIcon(packageName).also {
            log("getApplicationIcon: $it")
        }
    }

    override fun extendVerificationTimeout(
        id: Int,
        verificationCodeAtTimeout: Int,
        millisecondsToDelay: Long
    ) {
        return src.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay)
            .also {
                log("extendVerificationTimeout: $it")
            }
    }

    override fun getApplicationEnabledSetting(packageName: String): Int {
        return src.getApplicationEnabledSetting(packageName).also {
            log("getApplicationEnabledSetting: $it")
        }
    }

    override fun queryIntentServices(intent: Intent, flags: Int): MutableList<ResolveInfo> {
        return src.queryIntentServices(intent, flags).also {
            log("queryIntentServices: $it")
        }
    }

    override fun isPermissionRevokedByPolicy(permissionName: String, packageName: String): Boolean {
        return src.isPermissionRevokedByPolicy(permissionName, packageName).also {
            log("isPermissionRevokedByPolicy: $it")
        }
    }

    override fun checkPermission(permissionName: String, packageName: String): Int {
        return src.checkPermission(permissionName, packageName).also {
            log("checkPermission: $it")
        }
    }

    override fun checkSignatures(packageName1: String, packageName2: String): Int {
        return src.checkSignatures(packageName1, packageName2).also {
            log("checkSignatures: $it")
        }
    }

    override fun checkSignatures(uid1: Int, uid2: Int): Int {
        return src.checkSignatures(uid1, uid2).also {
            log("checkSignatures: $it")
        }
    }

    override fun removePackageFromPreferred(packageName: String) {
        return src.removePackageFromPreferred(packageName).also {
            log("removePackageFromPreferred: $it")
        }
    }

    override fun addPermission(info: PermissionInfo): Boolean {
        return src.addPermission(info).also {
            log("addPermission: $it")
        }
    }

    override fun getDrawable(
        packageName: String,
        resid: Int,
        appInfo: ApplicationInfo?
    ): Drawable? {
        return src.getDrawable(packageName, resid, appInfo).also {
            log("getDrawable: $it")
        }
    }

    override fun getPackageInfo(packageName: String, flags: Int): PackageInfo {
        log("getPackageInfo: $packageName")

        return (if (packageName == "com.aispeech.sample") {
            PackageInfo().apply {
                this.signatures = if (BuildConfig.DEBUG) arrayOf(
                    Signature("308201dd30820146020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3137303832363033313931315a170d3437303831393033313931315a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330819f300d06092a864886f70d010101050003818d0030818902818100dd54000ed403db34f7afa283216f62689810ea5ed74a6a5c470325816be2ea573700e928457122d61e92dc0b50802fbcdf6ef4ae95cc447c221dde2089f012cb32e1f26d57c71a63e69ca5afa3531ffae22e3693d6f8a1d5c3d693f6ee7aa53e578cf2c14e0e87317badb1a38cc649401a7fc6d92b944893fcf07d18988a4a270203010001300d06092a864886f70d01010505000381810040c9319969113e3b14a0108258885ac94e2d7aac2ede1000d3207ea9012595c15a74363aec42de16f1cc28a001d650870fc2c7bde132681354f754bb5c109fcc6821421319f7858e4103bad84274b8dfbf5094a08880f1f67e1d09a56704bd1a8d15456b3182316ba1dbe7b4a6f42e10737dee363788640375648abea172176e")
                ) else arrayOf(
                    Signature("3082037130820259a00302010202045298ce7f300d06092a864886f70d01010b05003069310b30090603550406130230313110300e060355040813076a69616e677375310f300d0603550407130673757a686f753111300f060355040a130861697370656563683111300f060355040b130861697370656563683111300f060355040313086169737065656368301e170d3137313132323032303635355a170d3432313131363032303635355a3069310b30090603550406130230313110300e060355040813076a69616e677375310f300d0603550407130673757a686f753111300f060355040a130861697370656563683111300f060355040b130861697370656563683111300f06035504031308616973706565636830820122300d06092a864886f70d01010105000382010f003082010a0282010100b6e0ce8b3c9fea7dd77d344a9982d5d859f3f2e0a9a529ad3bef64775ffb41a182ae3236202b5225636f23388f8683fa8fb5a31d6a8e38b50b80c5b11b26dd7804900958a7de75219e61b844098bdfd5d0043b619c5609146b97287dd939524990fb838c35d3db76838bf85f7544ec01877e2582b644fb466a4a7afc413457577610c672b79e64f205af83780bf537c8fda5cc5bd70bc2206a278d65f1bdb3fb347502c04cde96a5a30ec13de48d418863c3175e6895d9a5690c88d67677d32b92cedffe1485ee53788dd816382f5c903d1eba4193e7439544ad6c564ecdc918ac66b440d65458f4c897e707edff5abecca099f261ad1b990c61eafdf77c2ee30203010001a321301f301d0603551d0e0416041445c4a612367c816e0353b71b1138a710e681798d300d06092a864886f70d01010b05000382010100485bd88fa58195ebec86be89c6e69dabe1271070116fee02c3436a6d398f08ebc859e17b28b63bcb898e687dc1c1f3fa41a74a0565ffff4f882681556104a8bb138c2a03d97a9db94b935b58ebeb081fb504cdfd3a9e156287f4ccc5519b874aa75be4e61c7fda2703a39f9d4f1ce565c6bde70a3e09e91f39872865967c7e3530662bc1e51071bf5551fbec3a8898a36d70a5e5684a5b230969fad31b65ac05c6fd7481fb2a0f87a9ff09415ae37748094ff4b3eff5d5d07b91b135f8d5034c110855ebaca5fbf75db346fbda1f922d2923dad5b8fe93d97155de03b8637e306f82fc334d6b5751638ae434fc6fd8e66630cea846c0faa8924fa3e779f0f8dd")
                )
            }
        } else {
            src.getPackageInfo(packageName, flags)
        }).also {
            if (it.signatures != null && it.signatures.isNotEmpty())
                log("getPackageInfo $packageName {$flags}: ${it.signatures[0].toCharsString()} ")
        }
    }
    //{"activities":[],"applicationInfo":{"className":"s.h.e.l.l.S","compatibleWidthLimitDp":0,"dataDir":"/data/user/0/com.xuexiaoyi.xxy","descriptionRes":0,"enabled":true,"enabledSetting":0,"flags":952647236,"fullBackupContent":0,"installLocation":-1,"largestWidthLimitDp":0,"minSdkVersion":19,"nativeLibraryDir":"/data/app/com.xuexiaoyi.xxy-1/lib","nativeLibraryRootRequiresIsa":false,"networkSecurityConfigRes":0,"overrideDensity":0,"overrideRes":0,"privateFlags":0,"processName":"com.xuexiaoyi.xxy","requiresSmallestWidthDp":0,"seinfo":"default","targetSdkVersion":0,"taskAffinity":"com.xuexiaoyi.xxy","theme":0,"uiOptions":0,"uid":10001,"versionCode":0,"whiteListed":0,"banner":0,"icon":0,"labelRes":0,"logo":0,"packageName":"com.xuexiaoyi.xxy","showUserIcon":-10000},"baseRevisionCode":0,"configPreferences":[],"coreApp":false,"featureGroups":[],"firstInstallTime":1603343651893,"gids":[],"installLocation":1,"lastUpdateTime":1603343651893,"packageName":"com.xuexiaoyi.xxy","requiredForAllUsers":false,"sharedUserLabel":0,"signatures":[{"mHashCode":0,"mHaveHashCode":false,"mSignature":[48,-126,2,-57,48,-126,1,-81,-96,3,2,1,2,2,4,102,106,113,114,48,13,6,9,42,-122,72,-122,-9,13,1,1,11,5,0,48,20,49,18,48,16,6,3,85,4,3,19,9,120,117,101,120,105,97,111,121,105,48,30,23,13,49,57,48,55,50,53,49,51,48,49,48,50,90,23,13,52,52,48,55,49,56,49,51,48,49,48,50,90,48,20,49,18,48,16,6,3,85,4,3,19,9,120,117,101,120,105,97,111,121,105,48,-126,1,34,48,13,6,9,42,-122,72,-122,-9,13,1,1,1,5,0,3,-126,1,15,0,48,-126,1,10,2,-126,1,1,0,-83,-26,44,-29,62,4,96,17,-104,-74,106,-88,11,60,86,-22,-119,13,-81,64,88,45,98,-62,109,-102,-49,18,56,27,59,-112,117,-34,76,116,70,-69,-9,6,-93,15,-122,-116,114,-127,82,-100,60,-100,-28,-74,52,-117,117,-82,-45,-66,-1,-118,89,-19,96,85,5,107,42,31,-23,40,9,64,-25,-44,10,-47,50,7,-83,-10,-4,-109,-58,34,50,-2,-85,-52,-42,-115,-117,98,53,89,-83,91,83,2,74,78,-19,-38,-61,100,0,-46,-37,85,-47,62,-77,24,72,53,-21,5,22,-42,-93,115,-124,-50,68,-34,67,-94,-98,60,-119,-123,109,28,-64,67,79,-79,42,30,42,64,10,52,20,-31,42,77,-123,-90,-40,2,93,11,95,58,83,17,39,-24,102,-74,-36,60,-39,78,-29,-97,-79,23,-109,-73,-73,92,-124,119,-69,98,62,39,-42,53,-84,-24,-69,56,83,-2,-106,81,94,-100,37,83,50,3,-117,-105,91,-80,-70,84,-57,114,55,-1,-59,93,-13,-82,-122,-90,-3,50,-79,76,30,57,104,-63,114,83,-68,-91,65,75,122,54,53,101,-120,105,-71,105,98,7,48,37,-106,63,118,5,-9,-43,99,57,79,-25,82,9,-48,-104,38,121,-59,20,98,-67,2,3,1,0,1,-93,33,48,31,48,29,6,3,85,29,14,4,22,4,20,125,-49,-52,-78,-91,75,60,26,110,40,14,51,3,0,125,-122,52,38,33,73,48,13,6,9,42,-122,72,-122,-9,13,1,1,11,5,0,3,-126,1,1,0,-88,-119,-13,107,-55,24,21,82,-52,-17,100,-72,-73,101,8,7,-64,63,39,-79,28,-118,-25,110,-9,83,-88,51,107,-122,49,101,-125,105,-55,-31,28,24,1,-106,35,-53,-87,-81,104,11,104,-25,-13,-120,97,59,29,-28,124,-40,-93,81,-35,81,28,-58,27,122,-19,-48,50,120,-2,-31,24,-75,-52,-82,-127,-101,96,32,-51,-121,-8,53,42,-97,45,49,40,-84,60,60,47,17,-82,-36,57,14,-56,107,-126,-22,-50,28,114,-81,-53,46,-80,-72,70,61,85,-111,-32,-79,-22,-116,-52,81,113,16,110,-8,6,-84,-113,123,95,-83,60,-89,-44,9,127,84,-25,-16,-26,55,19,-68,72,103,-40,-42,47,68,1,-61,-61,18,-79,-7,-82,88,-97,-83,69,-36,-113,-62,81,-76,-19,96,-27,12,42,107,35,55,-85,-64,-12,33,107,30,-12,78,68,-81,-112,42,51,65,-114,-116,-94,37,126,-92,121,83,59,-74,12,86,-24,34,5,-93,-116,-113,-107,28,7,42,-16,-16,-53,125,114,11,-76,-40,-1,-81,-2,-71,-35,71,-10,81,50,-58,-128,90,42,102,45,-56,84,60,-112,120,15,118,-84,69,97,76,102,-55,7,82,92,-104,-16,-8,-100,-5,-126,-69,42,-87,-58,69]}],"versionCode":12,"versionName":"1.1.2"}
//
//    override fun getPackageInfo(versionedPackage: VersionedPackage, flags: Int): PackageInfo {
//        return src.getPackageInfo(versionedPackage, flags).also {
//            log("getPackageInfo: $it")
//        }
//    }

    override fun getPackagesHoldingPermissions(
        permissions: Array<String>,
        flags: Int
    ): MutableList<PackageInfo> {
        return src.getPackagesHoldingPermissions(permissions, flags).also {
            log("getPackagesHoldingPermissions: $it")
        }
    }

    override fun addPermissionAsync(info: PermissionInfo): Boolean {
        return src.addPermissionAsync(info).also {
            log("addPermissionAsync: $it")
        }
    }

    override fun getSystemAvailableFeatures(): Array<FeatureInfo> {
        return src.getSystemAvailableFeatures().also {
            log("getSystemAvailableFeatures: $it")
        }
    }

    override fun getSystemSharedLibraryNames(): Array<String>? {
        return src.getSystemSharedLibraryNames().also {
            log("getSystemSharedLibraryNames: $it")
        }
    }

    override fun queryIntentContentProviders(intent: Intent, flags: Int): MutableList<ResolveInfo> {
        return src.queryIntentContentProviders(intent, flags).also {
            log("queryIntentContentProviders: $it")
        }
    }

    override fun getApplicationBanner(info: ApplicationInfo): Drawable? {
        return src.getApplicationBanner(info).also {
            log("getApplicationBanner: $it")
        }
    }

    override fun getApplicationBanner(packageName: String): Drawable? {
        return src.getApplicationBanner(packageName).also {
            log("getApplicationBanner: $it")
        }
    }

    override fun getPackageGids(packageName: String): IntArray {
        return src.getPackageGids(packageName).also {
            log("getPackageGids: $it")
        }
    }

    override fun getPackageGids(packageName: String, flags: Int): IntArray {
        return src.getPackageGids(packageName, flags).also {
            log("getPackageGids: $it")
        }
    }

    override fun getResourcesForActivity(activityName: ComponentName): Resources {
        return src.getResourcesForActivity(activityName).also {
            log("getResourcesForActivity: $it")
        }
    }

    override fun getPackagesForUid(uid: Int): Array<String>? {
        return src.getPackagesForUid(uid).also {
            log("getPackagesForUid: $it")
        }
    }

    override fun getPermissionGroupInfo(permissionName: String, flags: Int): PermissionGroupInfo {
        return src.getPermissionGroupInfo(permissionName, flags).also {
            log("getPermissionGroupInfo: $it")
        }
    }

    override fun addPackageToPreferred(packageName: String) {
        return src.addPackageToPreferred(packageName).also {
            log("addPackageToPreferred: $it")
        }
    }

    override fun getComponentEnabledSetting(componentName: ComponentName): Int {
        return src.getComponentEnabledSetting(componentName).also {
            log("getComponentEnabledSetting: $it")
        }
    }

    override fun getLeanbackLaunchIntentForPackage(packageName: String): Intent? {
        return src.getLeanbackLaunchIntentForPackage(packageName).also {
            log("getLeanbackLaunchIntentForPackage: $it")
        }
    }

    override fun getInstalledPackages(flags: Int): MutableList<PackageInfo> {
        return src.getInstalledPackages(flags).also {
            log("getInstalledPackages: $it")
        }
    }

    override fun getAllPermissionGroups(flags: Int): MutableList<PermissionGroupInfo> {
        return src.getAllPermissionGroups(flags).also {
            log("getAllPermissionGroups: $it")
        }
    }

    override fun getNameForUid(uid: Int): String? {
        return src.getNameForUid(uid).also {
            log("getNameForUid: $it")
        }
    }


    override fun getApplicationLogo(info: ApplicationInfo): Drawable? {
        return src.getApplicationLogo(info).also {
            log("getApplicationLogo: $it")
        }
    }

    override fun getApplicationLogo(packageName: String): Drawable? {
        return src.getApplicationLogo(packageName).also {
            log("getApplicationLogo: $it")
        }
    }

    override fun getApplicationLabel(info: ApplicationInfo): CharSequence {
        if (info.packageName == "com.aispeech.sample") {
            return "AISpeech_sdk_samples"
        }
        return src.getApplicationLabel(info).also {
            log("getApplicationLabel: $it")
        }
    }

    override fun getPreferredActivities(
        outFilters: MutableList<IntentFilter>,
        outActivities: MutableList<ComponentName>,
        packageName: String?
    ): Int {
        return src.getPreferredActivities(outFilters, outActivities, packageName).also {
            log("getPreferredActivities: $it")
        }
    }

    override fun setInstallerPackageName(targetPackage: String, installerPackageName: String?) {
        return src.setInstallerPackageName(targetPackage, installerPackageName).also {
            log("setInstallerPackageName: $it")
        }
    }

    override fun getUserBadgedLabel(label: CharSequence, user: UserHandle): CharSequence {
        return src.getUserBadgedLabel(label, user).also {
            log("getUserBadgedLabel: $it")
        }
    }

    override fun getActivityIcon(activityName: ComponentName): Drawable {
        return src.getActivityIcon(activityName).also {
            log("getActivityIcon: $it")
        }
    }

    override fun getActivityIcon(intent: Intent): Drawable {
        return src.getActivityIcon(intent).also {
            log("getActivityIcon: $it")
        }
    }

    override fun canonicalToCurrentPackageNames(packageNames: Array<String>): Array<String> {
        return src.canonicalToCurrentPackageNames(packageNames).also {
            log("canonicalToCurrentPackageNames: $it")
        }
    }

    override fun getProviderInfo(component: ComponentName, flags: Int): ProviderInfo {
        return src.getProviderInfo(component, flags).also {
            log("getProviderInfo: $it")
        }
    }

    override fun clearPackagePreferredActivities(packageName: String) {
        return src.clearPackagePreferredActivities(packageName).also {
            log("clearPackagePreferredActivities: $it")
        }
    }

    override fun getPackageInstaller(): PackageInstaller {
        return src.getPackageInstaller().also {
            log("getPackageInstaller: $it")
        }
    }

    override fun resolveService(intent: Intent, flags: Int): ResolveInfo? {
        return src.resolveService(intent, flags).also {
            log("resolveService: $it")
        }
    }

    override fun verifyPendingInstall(id: Int, verificationCode: Int) {
        return src.verifyPendingInstall(id, verificationCode).also {
            log("verifyPendingInstall: $it")
        }
    }


    override fun getText(
        packageName: String,
        resid: Int,
        appInfo: ApplicationInfo?
    ): CharSequence? {
        return src.getText(packageName, resid, appInfo).also {
            log("getText: $it")
        }
    }

    override fun resolveContentProvider(authority: String, flags: Int): ProviderInfo? {
        return src.resolveContentProvider(authority, flags).also {
            log("resolveContentProvider: $it")
        }
    }

    override fun hasSystemFeature(featureName: String): Boolean {
        return src.hasSystemFeature(featureName).also {
            log("hasSystemFeature: $it")
        }
    }

    override fun hasSystemFeature(featureName: String, version: Int): Boolean {
        return src.hasSystemFeature(featureName, version).also {
            log("hasSystemFeature: $it")
        }
    }

    override fun getInstrumentationInfo(className: ComponentName, flags: Int): InstrumentationInfo {
        return src.getInstrumentationInfo(className, flags).also {
            log("getInstrumentationInfo: $it")
        }
    }

    override fun getInstalledApplications(flags: Int): MutableList<ApplicationInfo> {
        return src.getInstalledApplications(flags).also {
            log("getInstalledApplications: $it")
        }
    }

    override fun getUserBadgedDrawableForDensity(
        drawable: Drawable,
        user: UserHandle,
        badgeLocation: Rect?,
        badgeDensity: Int
    ): Drawable {
        return src.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity)
            .also {
                log("getUserBadgedDrawableForDensity: $it")
            }
    }

    override fun getDefaultActivityIcon(): Drawable {
        return src.getDefaultActivityIcon().also {
            log("getDefaultActivityIcon: $it")
        }
    }

    override fun getPreferredPackages(flags: Int): MutableList<PackageInfo> {
        return src.getPreferredPackages(flags).also {
            log("getPreferredPackages: $it")
        }
    }

    override fun addPreferredActivity(
        filter: IntentFilter,
        match: Int,
        set: Array<ComponentName>?,
        activity: ComponentName
    ) {
        return src.addPreferredActivity(filter, match, set, activity).also {
            log("addPreferredActivity: $it")
        }
    }


    override fun queryIntentActivities(intent: Intent, flags: Int): MutableList<ResolveInfo> {
        return src.queryIntentActivities(intent, flags).also {
            log("queryIntentActivities: $it")
        }
    }

    override fun getActivityBanner(activityName: ComponentName): Drawable? {
        return src.getActivityBanner(activityName).also {
            log("getActivityBanner: $it")
        }
    }

    override fun getActivityBanner(intent: Intent): Drawable? {
        return src.getActivityBanner(intent).also {
            log("getActivityBanner: $it")
        }
    }

    override fun setComponentEnabledSetting(
        componentName: ComponentName,
        newState: Int,
        flags: Int
    ) {
        return src.setComponentEnabledSetting(componentName, newState, flags).also {
            log("setComponentEnabledSetting: $it")
        }
    }

    override fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo {
        if(packageName=="com.aispeech.sample") {
            return ApplicationInfo().also {
                it.packageName = "com.aispeech.sample"
            }
        }
        return src.getApplicationInfo(packageName, flags).also {
            log("getApplicationInfo: $it")
        }
    }

    override fun resolveActivity(intent: Intent, flags: Int): ResolveInfo? {
        return src.resolveActivity(intent, flags).also {
            log("resolveActivity: $it")
        }
    }

    override fun queryBroadcastReceivers(intent: Intent, flags: Int): MutableList<ResolveInfo> {
        return src.queryBroadcastReceivers(intent, flags).also {
            log("queryBroadcastReceivers: $it")
        }
    }

    override fun getXml(
        packageName: String,
        resid: Int,
        appInfo: ApplicationInfo?
    ): XmlResourceParser? {
        return src.getXml(packageName, resid, appInfo).also {
            log("getXml: $it")
        }
    }

    override fun getActivityLogo(activityName: ComponentName): Drawable? {
        return src.getActivityLogo(activityName).also {
            log("getActivityLogo: $it")
        }
    }

    override fun getActivityLogo(intent: Intent): Drawable? {
        return src.getActivityLogo(intent).also {
            log("getActivityLogo: $it")
        }
    }

    override fun queryPermissionsByGroup(
        permissionGroup: String,
        flags: Int
    ): MutableList<PermissionInfo> {
        return src.queryPermissionsByGroup(permissionGroup, flags).also {
            log("queryPermissionsByGroup: $it")
        }
    }

    override fun queryContentProviders(
        processName: String?,
        uid: Int,
        flags: Int
    ): MutableList<ProviderInfo> {
        return src.queryContentProviders(processName, uid, flags).also {
            log("queryContentProviders: $it")
        }
    }

    override fun getPermissionInfo(permissionName: String, flags: Int): PermissionInfo {
        return src.getPermissionInfo(permissionName, flags).also {
            log("getPermissionInfo: $it")
        }
    }

    override fun removePermission(permissionName: String) {
        return src.removePermission(permissionName).also {
            log("removePermission: $it")
        }
    }

    override fun queryInstrumentation(
        targetPackage: String,
        flags: Int
    ): MutableList<InstrumentationInfo> {
        return src.queryInstrumentation(targetPackage, flags).also {
            log("queryInstrumentation: $it")
        }
    }

    override fun currentToCanonicalPackageNames(packageNames: Array<String>): Array<String> {
        return src.currentToCanonicalPackageNames(packageNames).also {
            log("currentToCanonicalPackageNames: $it")
        }
    }

    override fun getPackageUid(packageName: String, flags: Int): Int {
        return src.getPackageUid(packageName, flags).also {
            log("getPackageUid: $it")
        }
    }

    override fun getUserBadgedIcon(drawable: Drawable, user: UserHandle): Drawable {
        return src.getUserBadgedIcon(drawable, user).also {
            log("getUserBadgedIcon: $it")
        }
    }

    override fun getActivityInfo(component: ComponentName, flags: Int): ActivityInfo {
        return src.getActivityInfo(component, flags).also {
            log("getActivityInfo: $it")
        }
    }

    override fun isSafeMode(): Boolean {
        return src.isSafeMode().also {
            log("isSafeMode: $it")
        }
    }

    override fun getInstallerPackageName(packageName: String): String? {
        return src.getInstallerPackageName(packageName).also {
            log("getInstallerPackageName: $it")
        }
    }

    override fun setApplicationEnabledSetting(packageName: String, newState: Int, flags: Int) {
        return src.setApplicationEnabledSetting(packageName, newState, flags).also {
            log("setApplicationEnabledSetting: $it")
        }
    }

    override fun getServiceInfo(component: ComponentName, flags: Int): ServiceInfo {
        return src.getServiceInfo(component, flags).also {
            log("getServiceInfo: $it")
        }
    }

    override fun getPackageArchiveInfo(archiveFilePath: String, flags: Int): PackageInfo? {
        return super.getPackageArchiveInfo(archiveFilePath, flags).also {
            log("getPackageArchiveInfo: $it")
        }
    }

    override fun isInstantApp(): Boolean {
        return src.isInstantApp()
    }

    override fun getInstantAppCookieMaxBytes(): Int {
        return src.getInstantAppCookieMaxBytes()
    }

    override fun getInstantAppCookie(): ByteArray {
        return src.getInstantAppCookie()
    }

    override fun clearInstantAppCookie() {
        return src.clearInstantAppCookie()
    }

    override fun updateInstantAppCookie(cookie: ByteArray?) {
        return src.updateInstantAppCookie(cookie)
    }

    override fun getSharedLibraries(flags: Int): MutableList<SharedLibraryInfo> {
        return src.getSharedLibraries(flags)
    }

    override fun getChangedPackages(sequenceNumber: Int): ChangedPackages? {
        return src.getChangedPackages(sequenceNumber)
    }

    override fun setApplicationCategoryHint(packageName: String, categoryHint: Int) {
        return src.setApplicationCategoryHint(packageName, categoryHint)
    }

    override fun canRequestPackageInstalls(): Boolean {
        return src.canRequestPackageInstalls()
    }
}
