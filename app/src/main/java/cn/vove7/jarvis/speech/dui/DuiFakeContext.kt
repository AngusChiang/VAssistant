@file:Suppress("UsePropertyAccessSyntax", "UsePropertyAccessSyntax")

package cn.vove7.jarvis.speech.dui

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.util.Log
import android.view.Display
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * # MyContext
 *
 * Created on 2020/7/22
 * @author Vove
 */
@SuppressLint("NewApi")
class DuiFakeContext(val src: Context) : Context() {

    private val TAG = "STJFakeContext"

    override fun getApplicationContext(): Context {
        return DuiFakeContext(src.getApplicationContext()).also {
            Log.d(TAG, "getApplicationContext: $it")
        }
    }

    override fun setWallpaper(bitmap: Bitmap?) {
        return src.setWallpaper(bitmap).also {
            Log.d(TAG, "setWallpaper: $it")
        }
    }

    override fun setWallpaper(data: InputStream?) {
        return src.setWallpaper(data).also {
            Log.d(TAG, "setWallpaper: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun removeStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        return src.removeStickyBroadcastAsUser(intent, user).also {
            Log.d(TAG, "removeStickyBroadcastAsUser: $it")
        }
    }

    override fun checkCallingOrSelfPermission(permission: String): Int {
        return src.checkCallingOrSelfPermission(permission).also {
            Log.d(TAG, "checkCallingOrSelfPermission: $it")
        }
    }

    override fun getClassLoader(): ClassLoader {
        return src.getClassLoader().also {
            Log.d(TAG, "getClassLoader: $it")
        }
    }

    override fun checkCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int): Int {
        return src.checkCallingOrSelfUriPermission(uri, modeFlags).also {
            Log.d(TAG, "checkCallingOrSelfUriPermission: $it")
        }
    }

    override fun getObbDir(): File {
        return src.getObbDir().also {
            Log.d(TAG, "getObbDir: $it")
        }
    }

    override fun checkUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int): Int {
        return src.checkUriPermission(uri, pid, uid, modeFlags).also {
            Log.d(TAG, "checkUriPermission: $it")
        }
    }

    override fun checkUriPermission(
        uri: Uri?,
        readPermission: String?,
        writePermission: String?,
        pid: Int,
        uid: Int,
        modeFlags: Int
    ): Int {
        return src.checkUriPermission(uri, pid, uid, modeFlags).also {
            Log.d(TAG, "checkUriPermission: $it")
        }
    }

    override fun getExternalFilesDirs(type: String?): Array<File> {
        return src.getExternalFilesDirs(type).also {
            Log.d(TAG, "getExternalFilesDirs: $it")
        }
    }

    override fun getPackageResourcePath(): String {
        return src.getPackageResourcePath().also {
            Log.d(TAG, "getPackageResourcePath: $it")
        }
    }

    @SuppressLint("NewApi")
    override fun deleteSharedPreferences(name: String?): Boolean {
        return src.deleteSharedPreferences(name).also {
            Log.d(TAG, "deleteSharedPreferences: $it")
        }
    }

    override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
        return src.checkPermission(permission, pid, uid).also {
            Log.d(TAG, "checkPermission: $it")
        }
    }

    override fun startIntentSender(
        intent: IntentSender?,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int
    ) {
        return src.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags)
            .also {
                Log.d(TAG, "startIntentSender: $it")
            }
    }

    override fun startIntentSender(
        intent: IntentSender?,
        fillInIntent: Intent?,
        flagsMask: Int,
        flagsValues: Int,
        extraFlags: Int,
        options: Bundle?
    ) {
        return src.startIntentSender(
            intent,
            fillInIntent,
            flagsMask,
            flagsValues,
            extraFlags,
            options
        ).also {
            Log.d(TAG, "startIntentSender: $it")
        }
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return src.getSharedPreferences(name, mode).also {
            Log.d(TAG, "getSharedPreferences: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        return src.sendStickyBroadcastAsUser(intent, user).also {
            Log.d(TAG, "sendStickyBroadcastAsUser: $it")
        }
    }

    @SuppressLint("NewApi")
    override fun getDataDir(): File {
        return src.getDataDir().also {
            Log.d(TAG, "getDataDir: $it")
        }
    }

    override fun getWallpaper(): Drawable {
        return src.getWallpaper().also {
            Log.d(TAG, "getWallpaper: $it")
        }
    }

    @SuppressLint("NewApi")
    override fun isDeviceProtectedStorage(): Boolean {
        return src.isDeviceProtectedStorage().also {
            Log.d(TAG, "isDeviceProtectedStorage: $it")
        }
    }

    override fun getExternalFilesDir(type: String?): File? {
        return src.getExternalFilesDir(type).also {
            Log.d(TAG, "getExternalFilesDir: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        return src.sendBroadcastAsUser(intent, user).also {
            Log.d(TAG, "sendBroadcastAsUser: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendBroadcastAsUser(
        intent: Intent?,
        user: UserHandle?,
        receiverPermission: String?
    ) {
        return src.sendBroadcastAsUser(intent, user, receiverPermission).also {
            Log.d(TAG, "sendBroadcastAsUser: $it")
        }
    }

    override fun getExternalCacheDir(): File? {
        return src.getExternalCacheDir().also {
            Log.d(TAG, "getExternalCacheDir: $it")
        }
    }

    override fun getDatabasePath(name: String?): File {
        return src.getDatabasePath(name).also {
            Log.d(TAG, "getDatabasePath: $it")
        }
    }

    override fun getFileStreamPath(name: String?): File {
        return src.getFileStreamPath(name).also {
            Log.d(TAG, "getFileStreamPath: $it")
        }
    }

    override fun stopService(service: Intent?): Boolean {
        return src.stopService(service).also {
            Log.d(TAG, "stopService: $it")
        }
    }

    @SuppressLint("NewApi")
    override fun checkSelfPermission(permission: String): Int {
        return src.checkSelfPermission(permission).also {
            Log.d(TAG, "checkSelfPermission: $it")
        }
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return src.registerReceiver(receiver, filter).also {
            Log.d(TAG, "registerReceiver: $it")
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        broadcastPermission: String?,
        scheduler: Handler?
    ): Intent? {
        return src.registerReceiver(receiver, filter, broadcastPermission, scheduler).also {
            Log.d(TAG, "registerReceiver: $it")
        }
    }

    override fun getSystemServiceName(serviceClass: Class<*>): String? {
        return src.getSystemServiceName(serviceClass).also {
            Log.d(TAG, "getSystemServiceName: $it")
        }
    }

    override fun getMainLooper(): Looper {
        return src.getMainLooper().also {
            Log.d(TAG, "getMainLooper: $it")
        }
    }

    override fun enforceCallingOrSelfPermission(permission: String, message: String?) {
        return src.enforceCallingOrSelfPermission(permission, message).also {
            Log.d(TAG, "enforceCallingOrSelfPermission: $it")
        }
    }

    override fun getPackageCodePath(): String {
        return src.getPackageCodePath().also {
            Log.d(TAG, "getPackageCodePath: $it")
        }
    }

    override fun checkCallingUriPermission(uri: Uri?, modeFlags: Int): Int {
        return src.checkCallingUriPermission(uri, modeFlags).also {
            Log.d(TAG, "checkCallingUriPermission: $it")
        }
    }

    override fun getWallpaperDesiredMinimumWidth(): Int {
        return src.getWallpaperDesiredMinimumWidth().also {
            Log.d(TAG, "getWallpaperDesiredMinimumWidth: $it")
        }
    }

    override fun createDeviceProtectedStorageContext(): Context {
        return src.createDeviceProtectedStorageContext().also {
            Log.d(TAG, "createDeviceProtectedStorageContext: $it")
        }
    }

    override fun openFileInput(name: String?): FileInputStream {
        return src.openFileInput(name).also {
            Log.d(TAG, "openFileInput: $it")
        }
    }

    override fun getCodeCacheDir(): File {
        return src.getCodeCacheDir().also {
            Log.d(TAG, "getCodeCacheDir: $it")
        }
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        return src.bindService(service, conn, flags).also {
            Log.d(TAG, "bindService: $it")
        }
    }

    override fun deleteDatabase(name: String?): Boolean {
        return src.deleteDatabase(name).also {
            Log.d(TAG, "deleteDatabase: $it")
        }
    }

    override fun getAssets(): AssetManager {
        return src.getAssets().also {
            Log.d(TAG, "getAssets: $it")
        }
    }

    override fun getNoBackupFilesDir(): File {
        return src.getNoBackupFilesDir().also {
            Log.d(TAG, "getNoBackupFilesDir: $it")
        }
    }

    override fun startActivities(intents: Array<out Intent>?) {
        return src.startActivities(intents).also {
            Log.d(TAG, "startActivities: $it")
        }
    }

    override fun startActivities(intents: Array<out Intent>?, options: Bundle?) {
        return src.startActivities(intents, options).also {
            Log.d(TAG, "startActivities: $it")
        }
    }

    override fun getResources(): Resources {
        return src.getResources().also {
            Log.d(TAG, "getResources: $it")
        }
    }

    override fun fileList(): Array<String> {
        return src.fileList().also {
            Log.d(TAG, "fileList: $it")
        }
    }

    override fun setTheme(resid: Int) {
        return src.setTheme(resid).also {
            Log.d(TAG, "setTheme: $it")
        }
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver?) {
        return src.unregisterReceiver(receiver).also {
            Log.d(TAG, "unregisterReceiver: $it")
        }
    }

    override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {
        return src.enforcePermission(permission, pid, uid, message).also {
            Log.d(TAG, "enforcePermission: $it")
        }
    }

    override fun openFileOutput(name: String?, mode: Int): FileOutputStream {
        return src.openFileOutput(name, mode).also {
            Log.d(TAG, "openFileOutput: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyOrderedBroadcast(
        intent: Intent?,
        resultReceiver: BroadcastReceiver?,
        scheduler: Handler?,
        initialCode: Int,
        initialData: String?,
        initialExtras: Bundle?
    ) {
        return src.sendStickyOrderedBroadcast(
            intent,
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras
        ). also {
            Log.d(TAG, "sendStickyOrderedBroadcast: $it")
        }
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        return src.createConfigurationContext(overrideConfiguration).also {
            Log.d(TAG, "createConfigurationContext: $it")
        }
    }

    override fun getFilesDir(): File {
        return src.getFilesDir().also {
            Log.d(TAG, "getFilesDir: $it")
        }
    }

    override fun sendBroadcast(intent: Intent?) {
        return src.sendBroadcast(intent).also {
            Log.d(TAG, "sendBroadcast: $it")
        }
    }

    override fun sendBroadcast(intent: Intent?, receiverPermission: String?) {
        return src.sendBroadcast(intent, receiverPermission).also {
            Log.d(TAG, "sendBroadcast: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendOrderedBroadcastAsUser(
        intent: Intent?,
        user: UserHandle?,
        receiverPermission: String?,
        resultReceiver: BroadcastReceiver?,
        scheduler: Handler?,
        initialCode: Int,
        initialData: String?,
        initialExtras: Bundle?
    ) {
        return src.sendOrderedBroadcastAsUser(
            intent,
            user,
            receiverPermission,
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras
        ).also {
            Log.d(TAG, "sendOrderedBroadcastAsUser: $it")
        }

    }

    override fun grantUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
        return src.grantUriPermission(toPackage, uri, modeFlags).also {
            Log.d(TAG, "grantUriPermission: $it")
        }
    }

    override fun enforceCallingUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        return src.enforceCallingUriPermission(uri, modeFlags, message).also {
            Log.d(TAG, "enforceCallingUriPermission: $it")
        }
    }

    override fun getCacheDir(): File {
        return src.getCacheDir().also {
            Log.d(TAG, "getCacheDir: $it")
        }
    }

    override fun clearWallpaper() {
        return src.clearWallpaper().also {
            Log.d(TAG, "clearWallpaper: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyOrderedBroadcastAsUser(
        intent: Intent?,
        user: UserHandle?,
        resultReceiver: BroadcastReceiver?,
        scheduler: Handler?,
        initialCode: Int,
        initialData: String?,
        initialExtras: Bundle?
    ) {
        return src.sendStickyOrderedBroadcastAsUser(
            intent,
            user,
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras
        ). also {
            Log.d(TAG, "sendStickyOrderedBroadcastAsUser: $it")
        }
    }

    override fun startActivity(intent: Intent?) {
        return src.startActivity(intent).also {
            Log.d(TAG, "startActivity: $it")
        }
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        return src.startActivity(intent, options).also {
            Log.d(TAG, "startActivity: $it")
        }
    }

    override fun getPackageManager(): PackageManager {
        return DuiFakePm(src.getPackageManager()).also {
            Log.d(TAG, "getPackageManager: $it")
        }
    }

    override fun openOrCreateDatabase(
        name: String?,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase {
        return src.openOrCreateDatabase(name, mode, factory).also {
            Log.d(TAG, "openOrCreateDatabase: $it")
        }
    }

    override fun openOrCreateDatabase(
        name: String?,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        return src.openOrCreateDatabase(name, mode, factory, errorHandler).also {
            Log.d(TAG, "openOrCreateDatabase: $it")
        }
    }

    override fun deleteFile(name: String?): Boolean {
        return src.deleteFile(name).also {
            Log.d(TAG, "deleteFile: $it")
        }
    }

    override fun startService(service: Intent?): ComponentName? {
        return src.startService(service).also {
            Log.d(TAG, "startService: $it")
        }
    }

    override fun revokeUriPermission(uri: Uri?, modeFlags: Int) {
        return src.revokeUriPermission(uri, modeFlags).also {
            Log.d(TAG, "revokeUriPermission: $it")
        }
    }

    @SuppressLint("NewApi")
    override fun moveDatabaseFrom(sourceContext: Context?, name: String?): Boolean {
        return src.moveDatabaseFrom(sourceContext, name).also {
            Log.d(TAG, "moveDatabaseFrom: $it")
        }
    }

    override fun startInstrumentation(
        className: ComponentName,
        profileFile: String?,
        arguments: Bundle?
    ): Boolean {
        return src.startInstrumentation(className, profileFile, arguments).also {
            Log.d(TAG, "startInstrumentation: $it")
        }
    }

    override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?) {
        return src.sendOrderedBroadcast(intent, receiverPermission).also {
            Log.d(TAG, "sendOrderedBroadcast: $it")
        }
    }

    override fun sendOrderedBroadcast(
        intent: Intent,
        receiverPermission: String?,
        resultReceiver: BroadcastReceiver?,
        scheduler: Handler?,
        initialCode: Int,
        initialData: String?,
        initialExtras: Bundle?
    ) {
        return src.sendOrderedBroadcast(
            intent,
            receiverPermission,
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras
        ). also {
            Log.d(TAG, "sendOrderedBroadcast: $it")
        }
    }

    override fun unbindService(conn: ServiceConnection) {
        return src.unbindService(conn).also {
            Log.d(TAG, "unbindService: $it")
        }
    }

    override fun getApplicationInfo(): ApplicationInfo {
        return src.getApplicationInfo().also {
            Log.d(TAG, "getApplicationInfo: $it")
        }
    }

    override fun getWallpaperDesiredMinimumHeight(): Int {
        return src.getWallpaperDesiredMinimumHeight().also {
            Log.d(TAG, "getWallpaperDesiredMinimumHeight: $it")
        }
    }

    override fun createDisplayContext(display: Display): Context {
        return src.createDisplayContext(display).also {
            Log.d(TAG, "createDisplayContext: $it")
        }
    }

    override fun getTheme(): Resources.Theme {
        return src.getTheme().also {
            Log.d(TAG, "getTheme: $it")
        }
    }

    override fun getPackageName(): String {
        return "com.aispeech.sample".also {
            Log.d(TAG, "getPackageName: $it")
        }
    }

    override fun getContentResolver(): ContentResolver {
        return src.getContentResolver().also {
            Log.d(TAG, "getContentResolver: $it")
        }
    }

    override fun getObbDirs(): Array<File> {
        return src.getObbDirs().also {
            Log.d(TAG, "getObbDirs: $it")
        }
    }

    override fun enforceCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        return src.enforceCallingOrSelfUriPermission(uri, modeFlags, message).also {
            Log.d(TAG, "enforceCallingOrSelfUriPermission: $it")
        }
    }

    override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean {
        return src.moveSharedPreferencesFrom(sourceContext, name).also {
            Log.d(TAG, "moveSharedPreferencesFrom: $it")
        }
    }

    override fun getExternalMediaDirs(): Array<File> {
        return src.getExternalMediaDirs().also {
            Log.d(TAG, "getExternalMediaDirs: $it")
        }
    }

    override fun checkCallingPermission(permission: String): Int {
        return src.checkCallingPermission(permission).also {
            Log.d(TAG, "checkCallingPermission: $it")
        }
    }

    override fun getExternalCacheDirs(): Array<File> {
        return src.getExternalCacheDirs().also {
            Log.d(TAG, "getExternalCacheDirs: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyBroadcast(intent: Intent?) {
        return src.sendStickyBroadcast(intent).also {
            Log.d(TAG, "sendStickyBroadcast: $it")
        }
    }

    override fun enforceCallingPermission(permission: String, message: String?) {
        return src.enforceCallingPermission(permission, message).also {
            Log.d(TAG, "enforceCallingPermission: $it")
        }
    }

    override fun peekWallpaper(): Drawable {
        return src.peekWallpaper().also {
            Log.d(TAG, "peekWallpaper: $it")
        }
    }

    override fun getSystemService(name: String): Any? {
        return src.getSystemService(name).also {
            Log.d(TAG, "getSystemService: $it")
        }
    }

    override fun getDir(name: String?, mode: Int): File {
        return src.getDir(name, mode).also {
            Log.d(TAG, "getDir: $it")
        }
    }

    override fun databaseList(): Array<String> {
        return src.databaseList().also {
            Log.d(TAG, "databaseList: $it")
        }
    }

    override fun createPackageContext(packageName: String?, flags: Int): Context {
        return src.createPackageContext(packageName, flags).also {
            Log.d(TAG, "createPackageContext: $it")
        }
    }

    override fun enforceUriPermission(
        uri: Uri?,
        pid: Int,
        uid: Int,
        modeFlags: Int,
        message: String?
    ) {
        return src.enforceUriPermission(uri, pid, uid, modeFlags, message).also {
            Log.d(TAG, "enforceUriPermission: $it")
        }
    }

    override fun enforceUriPermission(
        uri: Uri?,
        readPermission: String?,
        writePermission: String?,
        pid: Int,
        uid: Int,
        modeFlags: Int,
        message: String?
    ) {
        return src.enforceUriPermission(uri, pid, uid, modeFlags, message).also {
            Log.d(TAG, "enforceUriPermission: $it")
        }
    }

    @SuppressLint("MissingPermission")
    override fun removeStickyBroadcast(intent: Intent?) {
        return src.removeStickyBroadcast(intent).also {
            Log.d(TAG, "removeStickyBroadcast: $it")
        }
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, flags: Int): Intent? {
        return src.registerReceiver(receiver, filter, flags)
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, broadcastPermission: String?, scheduler: Handler?, flags: Int): Intent? {
        return src.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags)
    }

    override fun revokeUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
        return src.revokeUriPermission(toPackage, uri, modeFlags)
    }

    override fun startForegroundService(service: Intent?): ComponentName? {
        return src.startForegroundService(service)
    }

    override fun createContextForSplit(splitName: String?): Context {
        return src.createContextForSplit(splitName)
    }
}
