@file:Suppress("unused", "DEPRECATION")

package cn.vove7.common.bridges

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.SearchManager
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.Context.WIFI_SERVICE
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.Image
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.*
import android.provider.AlarmClock
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cn.vove7.android.common.logi
import cn.vove7.common.R
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.UtilBridge.bitmap2File
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.helper.AdvanContactHelper
import cn.vove7.common.helper.ConnectiveNsdHelper
import cn.vove7.common.helper.startable
import cn.vove7.common.model.LocationInfo
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.ResultBox
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.model.ResponseMessage
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.common.utils.*
import cn.vove7.common.view.ScreenshotActivity
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.quantumclock.QuantumClock
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.calendar.CalendarAccount
import cn.vove7.vtp.calendar.CalendarHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.GsonHelper
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper
import com.catchingnow.icebox.sdk_client.IceBox
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.lang.Thread.sleep
import java.net.Inet4Address
import java.net.URLEncoder
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@Suppress("unused", "MemberVisibilityCanBePrivate")
object SystemBridge : SystemOperation {
    private val context: Context
        get() = GlobalApp.APP

    override fun openAppDetail(pkg: String): Boolean {
        return try {
//            AppHelper.showPackageDetail(context, pkg)
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", pkg, null)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = uri
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    override fun getLaunchIntent(pkg: String): Intent? {
        return context.packageManager
            .getLaunchIntentForPackage(pkg)
    }

    override fun getPhoneByName(name: String): String? {
        return AdvanContactHelper.matchPhone(name)?.second?.let { it[0] }
    }

    override fun getContactByName(name: String): Pair<String, Array<String>>? {
        return AdvanContactHelper.matchPhone(name)
    }

    fun openAppByPkg(pkg: String): Boolean {
        return openAppByPkg(pkg, false)
    }

    //小黑屋
    fun checkStopApp(pkg: String): Boolean {
        if (hasInstall("web1n.stopapp")) {
            return try {
                val data = Uri.Builder().scheme("web1n.stopapp")
                    .authority("action")
                    .appendPath("run_app").appendPath(pkg)
                    .appendQueryParameter("user", 0.toString()).build()
                context.startActivity(Intent("android.intent.action.VIEW").setData(data))
                true
            } catch (e: Throwable) {
                GlobalLog.err("小黑屋开启应用失败", e)
                false
            }
        }
        return false
    }

    override fun openAppByPkg(pkg: String, clearTask: Boolean): Boolean {
        return try {
            if (!AppInfo(pkg).startable) {
                if (checkStopApp(pkg)) {
                    "小黑屋开启成功".logi()
                    return true
                }
            }
            if (hasInstall(IceBox.PACKAGE_NAME) && IceBox.queryWorkMode(context) != IceBox.WorkMode.MODE_NOT_AVAILABLE &&
                IceBox.getAppEnabledSetting(context, pkg) != 0) {
                if (ContextCompat.checkSelfPermission(context, IceBox.SDK_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                    AppBus.post(RequestPermission("冰箱"))
                    return false
                }
                IceBox.setAppEnabledSettings(context, true, pkg)
                Vog.d("冰箱解冻：$pkg")
            }
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                throw Exception("启动失败 未找到此App: $pkg")
            } else {
                if (clearTask) {
                    launchIntent.clearTask()
                }
                context.startActivity(launchIntent)
                true
            }
        } catch (e: Exception) {
            GlobalLog.err("打开App{$pkg}失败", e)
            false
        }
    }

    override fun freezeAll() {
        if (hasInstall(IceBox.PACKAGE_NAME) && IceBox.queryWorkMode(context) != IceBox.WorkMode.MODE_NOT_AVAILABLE) {
            IceBox.setAppEnabledSettings(context, false)
        }
        if (hasInstall("web1n.stopapp")) {
            val data = Uri.Builder().scheme("web1n.stopapp")
                .authority("action")
                .appendPath("freeze_all")
                .appendQueryParameter("user", 0.toString()).build()
            context.startActivity(Intent("android.intent.action.VIEW").setData(data))
        }
    }

    fun openAppByWord(appWord: String, resetTask: Boolean): String? {
        val pkg = getPkgByWord(appWord)
        return if (pkg != null) {
            val o = openAppByPkg(pkg, resetTask)
            if (o) {
                return pkg
            } else null
        } else null
    }

    /**
     * openAppByWord
     * @return packageName if success
     */
    override fun openAppByWord(appWord: String): String? {
        return openAppByWord(appWord, false)
    }

    /**
     * 标记 -> 应用列表
     * @param appWord String
     * @return String?
     */
    override fun getPkgByName(appWord: String, excludeUnstartable: Boolean): String? {
        val list = AdvanAppHelper.matchPkgByName(appWord, excludeUnstartable)
        return if (list.isNotEmpty()) {
            list[0].data.packageName.also {
                Vog.i("应用：$appWord -> $it")
            }
        } else null
    }

    override fun getPkgByWord(appWord: String): String? =
        getPkgByName(appWord)


    // Open App 启动对应首页Activity
    fun startActivity(pkg: String, fullActivityName: String): Boolean {
        return try {
            val launchIntent = Intent()
            launchIntent.setClassName(pkg, fullActivityName)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } catch (e: Exception) {
            GlobalLog.err(e.message)
            false
        }
    }

    /**
     * 打电话
     * 优先级：标记 -> 通讯录 -> 服务提供
     */
    override fun call(s: String): Boolean {
        return call(s, null)
    }

    /**
     * 输入 纯数字
     * @param s String
     * @param simId Int? 卡号 0:卡1  1:卡2
     * @return Boolean
     */
    override fun call(s: String, simId: Int?): Boolean {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$s"))
        if (simId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                val phoneAccountHandleList = telecomManager.callCapablePhoneAccounts
                val sId = if (simId >= phoneAccountHandleList.size) {
                    GlobalLog.err("call ---> 卡号指定错误,使用默认卡拨号")
                    0
                } else simId
                callIntent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                    phoneAccountHandleList[sId])
            } catch (e: Exception) {
                GlobalLog.err("设置卡id失败" + e.message)
            } catch (e: SecurityException) {

            }
        }
        return try {
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(callIntent)
            true
        } catch (e: SecurityException) {
            AppBus.post(RequestPermission("电话权限"))
            false
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    /**
     * 打开手电
     */
    override fun openFlashlight(): Boolean {
        return switchFLSafely(true)
    }

    /**
     * 关闭手电
     */
    override fun closeFlashlight(): Boolean {
        return switchFLSafely(false)
    }

    @SuppressLint("ObsoleteSdkInt")
    @Throws(Exception::class)
    private fun switchFlashlight(on: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//<7.0
            switchFlashlightLessL(on)
        } else {
            switchFlashlightAboveL(on)
        }
    }

    /**
     * 低于7.0 切换手电
     * @param on Boolean
     */
    @Suppress("DEPRECATION")
    private fun switchFlashlightLessL(on: Boolean) {
        val camera: Camera = Camera.open()
        val parameters = camera.parameters
        parameters.flashMode = if (!on) {
            Camera.Parameters.FLASH_MODE_OFF
        } else {
            Camera.Parameters.FLASH_MODE_TORCH
        }
        camera.parameters = parameters
    }

    /**
     * 高于7.0 切换手电
     * @param on Boolean
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun switchFlashlightAboveL(on: Boolean) {
        //获取CameraManager
        val mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //获取当前手机所有摄像头设备ID
        val ids = mCameraManager.cameraIdList
        var r = false
        ids.forEach { id ->
            val c = mCameraManager.getCameraCharacteristics(id)
            //查询该摄像头组件是否包含闪光灯
            val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            val lensFacing = c.get(CameraCharacteristics.LENS_FACING)
            if (flashAvailable != null && flashAvailable && lensFacing != null
                && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                //打开或关闭手电筒
                mCameraManager.setTorchMode(id, on)
                r = true
            }
        }
        if (!r) {
            throw Exception("未找到可用闪光灯")
        }
    }

    private fun switchFLSafely(on: Boolean): Boolean {
        try {
            switchFlashlight(on)
        } catch (e: Throwable) {
            GlobalLog.err(e)
            GlobalApp.toastInfo((if (on) "打开" else "关闭") + "手电失败")
            return false
        }
        return true
    }

    override fun getDeviceInfo(): DeviceInfo {
        return SystemHelper.getDeviceInfo(context)
    }

    override fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.newTask()
            intent.data = Uri.parse(url)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            GlobalApp.toastInfo("无可用浏览器")
            false
        }
    }

    /**
     * 获取App信息
     * @param s 包名 或 App名/标记名
     */
    override fun getAppInfo(s: String): AppInfo? {
        return if (RegUtils.isPackage(s)) {
            AdvanAppHelper.ALL_APP_LIST[s]
        } else {
            val pkg = getPkgByWord(s)
            AdvanAppHelper.ALL_APP_LIST[pkg]
        }
    }

    override fun hasInstall(pkg: String): Boolean {
        return pkg in AdvanAppHelper.ALL_APP_LIST
    }

    @Throws()
    override fun sendKey(keyCode: Int): Boolean {
        ShellHelper.execAuto("input keyevent $keyCode")
        return true
    }

    override fun mediaPause() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    @Suppress("DEPRECATION")
    fun removeMusicFocus() {
        val mAm = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            mAm.abandonAudioFocus(null)
        } catch (e: NoClassDefFoundError) {//7.1.2- 异常

        }
    }


    @Suppress("DEPRECATION")
    fun requestMusicFocus() {
        Vog.d("暂停音乐")
        val mAm = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = mAm.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Vog.d("requestMusicFocus ---> successfully")
        } else {
            Vog.d("requestMusicFocus ---> failed")
        }
    }

    /**
     * [mediaResume]
     */
    override fun mediaStart() {
        mediaResume()
    }


    /**
     * 兼容
     * @param keyCode Int
     */
    private fun sendMediaKey(keyCode: Int) {
        if (ShellHelper.hasRootOrAdb()) {
            sendKey(keyCode)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sendMediaKeyOnN(keyCode)
        } else {
            sendMediaKeyUnderN(keyCode)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendMediaKeyUnderN(keyCode: Int) {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val kdn = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val kup = KeyEvent(KeyEvent.ACTION_UP, keyCode)

        GlobalApp.APP.packageManager.queryBroadcastReceivers(intent, 0).map {
            ComponentName(it.activityInfo.packageName, it.activityInfo.name)
        }.forEach { cn ->
            Vog.d("sendMediaKey to $cn")
            sendMediaKey2Component(intent, kdn, kup, cn)
        }
    }

    private fun sendMediaKeyOnN(keyCode: Int) {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val kdn = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val kup = KeyEvent(KeyEvent.ACTION_UP, keyCode)


        //当前活跃 media_button_receiver
        val currentActiveApp = Settings.Secure.getString(context.contentResolver, "media_button_receiver")

        kotlin.runCatching {
            val cn = if (currentActiveApp.isNullOrBlank()) {
                null
            } else {
                currentActiveApp.let {
                    it.split(',')[0].let { pc ->
                        val (p, c) = pc.split('/')
                        ComponentName(p, c)
                    }
                }
            }
            sendMediaKey2Component(intent, kdn, kup, cn)
        }.onFailure {
            GlobalApp.toastError("指令发送失败")
            GlobalLog.err("当前媒体解析失败：$currentActiveApp \n${it.message}")
        }
    }

    private fun sendMediaKey2Component(intent: Intent, kdn: KeyEvent, kup: KeyEvent, cn: ComponentName?) {
        Vog.d("sendMediaKey $kdn to $cn")
        intent.putExtra(Intent.EXTRA_KEY_EVENT, kdn)
        intent.component = cn
        GlobalApp.APP.sendBroadcast(intent)
        intent.putExtra(Intent.EXTRA_KEY_EVENT, kup)
        GlobalApp.APP.sendBroadcast(intent)
    }

    override fun mediaResume() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    override fun mediaStop() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_STOP)
    }

    override fun mediaNext() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    override fun mediaPre() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    fun switchMusicStatus() {
        if (isMediaPlaying()) {
            mediaPause()
        } else mediaResume()
    }

    /**
     * 震动
     */
    override fun volumeMute() {
        setRingMode(AudioManager.RINGER_MODE_VIBRATE)
    }

    /**
     * 设置静音
     * @param b Boolean
     */
    fun setMute(b: Boolean) {
        if (b) volumeMute()
        else volumeUnmute()
    }

    fun setRingMode(mode: Int) {
//        val direction = if (state) ADJUST_MUTE else ADJUST_UNMUTE
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        mAudioManager.adjustSuggestedStreamVolume(direction, AudioManager.USE_DEFAULT_STREAM_TYPE, 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted) {
                GlobalApp.toastInfo("请先授予权限")
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            } else {
                mAudioManager.ringerMode = mode
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastInfo("设置失败")
        }
    }

    override fun doNotDisturbMode() {
        setRingMode(AudioManager.RINGER_MODE_SILENT)
    }

    override fun volumeUnmute() {
        setRingMode(AudioManager.RINGER_MODE_NORMAL)
    }

    override fun volumeUp() {
        switchVolume(AudioManager.ADJUST_RAISE)
    }

    override fun volumeDown() {
        switchVolume(AudioManager.ADJUST_LOWER)
    }

    /**
     * @see []
     * @param direction Int [AudioManager.ADJUST_RAISE] [AudioManager.ADJUST_LOWER]
     */
    private fun switchVolume(direction: Int) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)

    }

    override val musicMaxVolume: Int
        get() {
            val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }
    override val musicCurrentVolume: Int
        get() {
            val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

    fun getVolumeByType(type: Int): Int {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return mAudioManager.getStreamVolume(type)
    }

    fun getMaxVolumeByType(type: Int): Int {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return mAudioManager.getStreamMaxVolume(type)
    }

    override fun setMusicVolume(index: Int) {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
    }

    override fun setAlarmVolume(index: Int) {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, index, 0)
    }

    override fun setNotificationVolume(index: Int) {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, index, 0)
    }

    override fun isMediaPlaying(): Boolean {
        val am = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        if (am == null) {
            Vog.e("isMusicActive：无法获取AudioManager引用")
            return false
        }
        return am.isMusicActive
    }

    override fun vibrate(millis: Long, effect: Int): Boolean {

        val vibrateMan = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateMan.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateMan.vibrate(VibrationEffect.createOneShot(millis, effect))
            } else {
                @Suppress("DEPRECATION")
                vibrateMan.vibrate(millis)
            }
        }
        return true
    }

    override fun vibrate(arr: Array<Long>): Boolean {
        val vibrateMan = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateMan.hasVibrator()) {
            val l = LongArray(arr.size)
            var i = 0
            arr.forEach { l[i++] = it }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateMan.vibrate(VibrationEffect.createWaveform(l, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrateMan.vibrate(l, -1)
            }
        }
        return true
    }

    override fun openBluetooth(): Boolean = opBT(true)

    override fun closeBluetooth(): Boolean = opBT(false)

    private fun opBT(enable: Boolean): Boolean {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return if (enable) mBluetoothAdapter.enable() //开启
        else mBluetoothAdapter.disable() //关闭
    }

    override fun openWifi(): Boolean {
        return switchWifi(true)
    }

    private fun switchWifi(on: Boolean): Boolean {
        if (ShellHelper.hasRootOrAdb()) {
            ShellHelper.execAuto("svc wifi " + (if (on) "enable" else "disable"))
            return true
        }

        return try {
            val wifiMan = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            return wifiMan.setWifiEnabled(on)
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    override fun closeWifi(): Boolean {
        return switchWifi(false)
    }

    //fixme
    override fun closeWifiAp(): Boolean {
        return setWifiApEnabled(false)
    }

    override fun openWifiAp(): Boolean {
        return setWifiApEnabled(true)
    }

    val isWifiEnable: Boolean
        get() {
            val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }

    /* 开启/关闭热点 */
    @Suppress("DEPRECATION")
    private fun setWifiApEnabled(enabled: Boolean): Boolean {
        // 因为wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
        closeWifi()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Handler(Looper.getMainLooper()).post {
                setWifiApEnabledForAndroidO(enabled)
            }
            return true
        }
        val wifiManager = context.applicationContext
            .getSystemService(WIFI_SERVICE) as WifiManager

//        val ap: WifiConfiguration? = null
        return try {
            // 热点的配置类
            val apConfig = WifiConfiguration()
            // 配置热点的名称(可以在名字后面加点随机数什么的)
//            apConfig.SSID = ssid
//            apConfig.preSharedKey = password
//            apConfig.allowedKeyManagement.set(4)//设置加密类型，这里4是wpa加密

            val method = wifiManager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, java.lang.Boolean.TYPE)
            // 返回热点打开状态
            method.invoke(wifiManager, apConfig, enabled) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 8.0 开启热点方法
     * 注意：这个方法开启的热点名称和密码是手机系统里面默认的那个
     * fixme 一段时间后自动关闭 and 名称密码?
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setWifiApEnabledForAndroidO(isEnable: Boolean) {
        val manager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        //cancelLocalOnlyHotspotRequest 是关闭热点
        //打开热
        try {
            if (isEnable) {
                manager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                        super.onStarted(reservation)
                        Vog.d("onStarted ---> ")
                    }

                    override fun onStopped() {
                        super.onStopped()
                        Vog.d("onStopped ---> ")
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        GlobalLog.err("失败：" + arrayOf(
                            0, ERROR_NO_CHANNEL, ERROR_GENERIC, ERROR_INCOMPATIBLE_MODE, ERROR_TETHERING_DISALLOWED)[reason])
                        Vog.d("onFailed ---> ")
                    }
                }, Handler(Looper.getMainLooper()))
            } else {
                GlobalApp.toastInfo("不支持关闭热点")
            }
        } catch (e: SecurityException) {
            AppBus.post(RequestPermission("位置权限"))
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastInfo("出错")
        }
    }

    override fun isScreenOn(): Boolean {
        return SystemHelper.isScreenOn(context)
    }

    override fun getClipText(): String? {
        prepareIfNeeded()
        return try {
            SystemHelper.getClipBoardContent(context).toString()
        } catch (e: Exception) {
            null
        }
    }

    override fun setClipText(text: String?): Boolean {
        prepareIfNeeded()
        return try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val mClipData = ClipData.newPlainText("", text)
            cm.setPrimaryClip(mClipData)
            true
        } catch (e: Throwable) {
            GlobalApp.toastError("复制失败：" + e.message)
            false
        }
    }

    fun sendEmail(to: String) {
        sendEmail(to, null, null)
    }

    fun sendEmail(to: String, subject: String?) {
        sendEmail(to, subject, null)
    }

    override fun sendEmail(to: String, subject: String?, content: String?) {
        val uri = Uri.parse("mailto:$to")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject ?: "")
        intent.putExtra(Intent.EXTRA_TEXT, content ?: "") // 正文
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (aw: ActivityNotFoundException) {
            GlobalApp.toastInfo(R.string.text_not_fount_email_app)
        } catch (e: Exception) {
            GlobalLog.err(e.message)
            GlobalApp.toastInfo(R.string.text_failed_to_open_email_app)
        }
    }

    /**
     * 暂时使用root方式发送按键
     * @note 管理员权限方式为锁定屏幕, 打开需解锁
     */
    override fun lockScreen(): Boolean {
        screenOff()
        return false
    }

    override fun locationInfo(): LocationInfo? {
        return try {
            val res = HttpBridge.get("http://whois.pconline.com.cn/ipJson.jsp?json=true")
            GsonHelper.fromJson<LocationInfo>(res)
        } catch (e: Throwable) {
            e.log()
            null
        }

    }

    @SuppressLint("MissingPermission")
    override fun location(): Location? {
        if (!PermissionUtils.isAllGranted(context, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))) {

            AppBus.post(RequestPermission("位置权限"))
            return null
        }
        val serviceString = Context.LOCATION_SERVICE// 获取的是位置服务

        val lm = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) GlobalApp.ForeService else GlobalApp.APP)?.let {
            it.getSystemService(serviceString) as LocationManager
        } ?: throw Exception("前台服务未启动，无法获取位置信息")

        val ht = HandlerThread("location")
        ht.start()
        val handler = Handler(ht.looper)
        val result = ResultBox<Location?>()

        val block = arrayOf(Runnable { })

        val loLis = object : LocationListener {
            @SuppressLint("MissingPermission")
            override fun onLocationChanged(location: Location) {
                Vog.d("onLocationChanged ---> $location")
                handler.removeCallbacks(block[0])
                lm.removeUpdates(this)
                result.setAndNotify(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        locationInternal(lm, LocationManager.GPS_PROVIDER, ht.looper, block, handler, loLis, result)

        return try {
            result.blockedGet(false)
        } catch (e: InterruptedException) {
            lm.removeUpdates(loLis)
            throw e
        } finally {
            ht.quitSafely()
        }

    }

    @SuppressLint("MissingPermission")
    private fun locationInternal(
        lm: LocationManager,
        lp: String,
        looper: Looper,
        block: Array<Runnable>,
        handler: Handler,
        loLis: LocationListener,
        result: ResultBox<Location?>
    ) {
        Vog.d("定位设备：$lp")
        if (!lm.isProviderEnabled(lp)) {
            GlobalApp.toastInfo("位置服务未开启")
            GlobalLog.log("位置服务未开启")
            if (lp == LocationManager.NETWORK_PROVIDER) {
                runInCatch {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                locationInternal(lm, LocationManager.NETWORK_PROVIDER, looper, block, handler, loLis, result)
            }
            return
        }

        lm.requestLocationUpdates(lp, 500L, 0f, loLis, looper)

        block[0] = Runnable {
            if (lp == LocationManager.NETWORK_PROVIDER) {
                Vog.d("location ---> 获取位置超时,使用上次位置")
                result.setAndNotify(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                )
                lm.removeUpdates(loLis)
            } else {
                Vog.d("location ---> gps 获取位置超时,使用网络定位")
                lm.removeUpdates(loLis)
                locationInternal(lm, LocationManager.NETWORK_PROVIDER, looper, block, handler, loLis, result)
            }
        }
        handler.postDelayed(block[0], 10000)//等待10秒
    }

    private fun quitLoop() {
        val looper = Looper.myLooper()
        looper?.quit()
    }

    private fun prepareIfNeeded() {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
    }

    override fun getLocalIpAddress(): String? {
        return try {
            val wifiService = context.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiService.connectionInfo
            intToIp(wifiInfo.ipAddress)
        } catch (e: Exception) {
            GlobalLog.err(e)
            Inet4Address.getLoopbackAddress().hostName
        }
    }

    override fun getNetAddress(): String? {
        val ts = (QuantumClock.currentTimeMillis / 1000)
        val reqJson = "{}"
        val sign = SecureHelper.signData(reqJson, ts)
        val headers = mapOf(
            "versionCode" to "${AppConfig.versionCode}",
            "timestamp" to ts.toString(),
            "token" to (UserInfo.getUserToken() ?: ""),
            "sign" to sign
        )
        val data = HttpBridge.postJson(ApiUrls.SERVER_IP + ApiUrls.GET_IP, reqJson, headers)
        val b = GsonHelper.fromJson<ResponseMessage<String>>(data)
        return b?.data
    }

    /**
     * 转IP
     * @param i Int
     * @return String
     */
    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." + ((i shr 8) and 0xFF) +
            "." + ((i shr 16) and 0xFF) + "." + (i shr 24 and 0xFF)
    }

    var screenData: Intent? = null

    @SuppressLint("StaticFieldLeak")
    var cap: ScreenCapturer? = null

    fun screenShotWithRelease(savePath: String) = screenShot(savePath).also {
        release()
    }

    override fun screenShot(savePath: String): File? {
        val (f, box) = screenShotAsync(savePath)
        box?.blockedGet()
        return f
    }

    /**
     * 异步截图
     * @param savePath String
     * @return Pair<File?, ResultBox<*>?>
     */
    override fun screenShotAsync(savePath: String): Pair<File?, ResultBox<*>?> {
        Vog.d("screenShot ---> 请求截屏 $savePath")

        if (ShellHelper.hasRootOrAdb()) {
            val box = ShellHelper.execAutoAsync("screencap -p $savePath", true)
            return File(savePath) to box
        }

        val data = screenData
        val intent = if (data == null) {
            //出现授权窗口，延迟截图
            val beginCap = SystemClock.uptimeMillis()

            val resultBox = ResultBox<Pair<Intent?, Boolean>>()
            val capIntent = ScreenshotActivity.getScreenshotIntent(context, resultBox)
            context.startActivity(capIntent)

            val sd = resultBox.blockedGet(false) ?: return null to null
            if (sd.second || SystemClock.uptimeMillis() - beginCap > 600) {
                sleep(700)
            }
            sd.first ?: return null to null
        } else {
            data
        }
        Vog.d("screenShot ---> $screenData")
        if (cap == null) {
            cap = ScreenCapturer(context, intent)
        }

        return try {
            cap?.capture()?.let {
                Vog.d("screenShot ---> $it")
                val bm = processImg(it)
                it.close()
                bitmap2File(bm, savePath)
            } to null
        } catch (e: Exception) {
            e.log()
            null to null
        }
    }

    private fun processImg(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride,
            height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        return bitmap
    }


    override fun screen2File(): File? {
        val tmpPath = context.cacheDir.absolutePath + "/screen.png"
        return screen2File(tmpPath)
    }

    fun screen2File(p: String) = screenShotWithRelease(p)

    override fun shareText(content: String?) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
//            intent.putExtra(Intent.EXTRA_SUBJECT, title)
            intent.putExtra(Intent.EXTRA_TEXT, content ?: "")
            intent.type = "text/plain"
            context.startActivity(Intent.createChooser(intent, "分享到").newTask())
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastInfo("分享失败")
        }
    }

    override fun shareImage(imgPath: String?) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            if (imgPath?.isEmpty() != false) {
                GlobalApp.toastInfo("图片不存在")
                return
            } else {
                val f = File(imgPath)
                if (f.exists() && f.isFile) {
                    intent.type = "image/jpg"

                    val imgUri = if (Build.VERSION.SDK_INT >= 24) {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                    } else Uri.fromFile(f)

                    intent.putExtra(Intent.EXTRA_STREAM, imgUri)
                }
            }
            context.startActivity(Intent.createChooser(intent, "分享到").newTask())
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastInfo("分享失败")
        }
    }

    fun createAlarm(hour: Int, minutes: Int) {
        createAlarm(null, null, hour, minutes, true)
    }

    override fun createAlarm(message: String?, days: Array<Int>?, hour: Int, minutes: Int, noUi: Boolean): Boolean {
        Vog.d("createAlarm ---> $message ${Arrays.toString(days)} $hour : $minutes")
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            .putExtra(AlarmClock.EXTRA_VIBRATE, true)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, noUi)
        if (message != null) intent.putExtra(AlarmClock.EXTRA_MESSAGE, message)
        if (days != null) intent.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, ArrayList(days.toList()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            try {
                context.startActivity(intent)
                true
            } catch (e: ActivityNotFoundException) {
                GlobalApp.toastInfo("未找到时钟App")
                false
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    override fun createCalendarEvent(title: String, content: String?, beginTime: Long,
                                     endTime: Long?, earlyAlarmMinute: Long?) {
        try {
            val account = CalendarAccount("V Assistant", "V Assistant", autoCreateContext = context)
            val cal = CalendarHelper(context, account)
            val end = endTime ?: (beginTime + 1000 * 60 * 10)//十分钟
            cal.addCalendarEvent(title, content ?: "", beginTime, end, earlyAlarmMinute)
        } catch (e: SecurityException) {
            GlobalLog.err(e)
            AppBus.post(RequestPermission("读写日历权限"))
        }
    }

    override fun createCalendarEvent(title: String, content: String?, beginTime: Long,
                                     endTime: Long?, isAlarm: Boolean) {
        try {
            val account = CalendarAccount("V Assistant", "V Assistant", autoCreateContext = context)
            val cal = CalendarHelper(context, account)
            val end = endTime ?: (beginTime + 1000 * 60 * 10)//十分钟
            cal.addCalendarEvent(title, content ?: "", beginTime, end, isAlarm)
        } catch (e: SecurityException) {
            GlobalLog.err(e)
            AppBus.post(RequestPermission("读写日历权限"))
        }

    }

    @Suppress("DEPRECATION")
    override fun screenOn() {
        if (ShellHelper.hasRootOrAdb()) {
            ShellHelper.execAuto("input keyevent ${KeyEvent.KEYCODE_WAKEUP}")
            return
        }
        thread {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isInteractive) {
                // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
                val wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, SystemBridge::class.java.simpleName)
                //点亮屏幕
                try {
                    wl.acquire(10 * 60 * 1000L /*10 minutes*/)
                } finally {
                    //释放
                    wl.release()
                }
            }
        }
    }

    //发送电源按键
    override fun screenOff() {
        if (ShellHelper.hasRootOrAdb()) {
            ShellHelper.execAuto("input keyevent ${KeyEvent.KEYCODE_SLEEP}")
            return
        }
        if (AccessibilityApi.isBaseServiceOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            GlobalActionExecutor.lockScreen()
        } else sendKey(26)
    }

    override fun quickSearch(s: String?) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, s)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            GlobalApp.toastError("无可用应用")
        }
    }

    override fun disableSoftKeyboard(): Boolean {
        return AccessibilityApi.accessibilityService?.disableSoftKeyboard() ?: false
    }

    override fun enableSoftKeyboard(): Boolean {
        return AccessibilityApi.accessibilityService?.enableSoftKeyboard() ?: false
    }

    override fun sendSMS(phone: String, content: String) {
//        //获取短信管理器
//        val smsManager = android.telephony.SmsManager.getDefault()
//        //拆分短信内容（手机短信长度限制）
//        smsManager.divideMessage(content).forEach {
//            smsManager.sendTextMessage(phone, null, it)
//        }

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
        intent.putExtra("sms_body", content)
        context.startActivity(intent.newTask())
    }

    override val batteryLevel: Int
        get() {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, filter)
                ?: return -1

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) //电量的刻度
            val maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100) //最大
            return level * 100 / maxLevel
        }

    override val isCharging: Boolean = run {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)

        val i = intent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) == BatteryManager.BATTERY_STATUS_CHARGING
        Vog.d("isCharging ---> $i")
        i
    }

    @Suppress("DEPRECATION")
    override val simCount: Int
        get() {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            return if (telecomManager != null) {
                if (context.checkPermission(Manifest.permission.READ_PHONE_STATE)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    telecomManager.callCapablePhoneAccounts.size
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SubscriptionManager.from(context).activeSubscriptionInfoCount
                } else {
                    0
                }

            } else {
                GlobalLog.err("获取TELECOM_SERVICE 失败}")
                0
            }
        }

    override val contacts: Array<Pair<String, String?>>
        get() {
            val list = mutableListOf<Pair<String, String?>>()
            AdvanContactHelper.getChoiceData().forEach {
                list.add(Pair(it.title, it.subtitle))
            }
            return list.toTypedArray()
        }

    override fun saveMarkedContact(name: String, regex: String, phone: String): Boolean {
        return saveMarkedData(MarkedData.MARKED_TYPE_CONTACT, name, regex, phone)
    }

    override fun saveMarkedApp(name: String, regex: String, pkg: String): Boolean {
        return saveMarkedData(MarkedData.MARKED_TYPE_APP, name, regex, pkg)
    }

    private fun saveMarkedData(type: String, key: String, regex: String, value: String): Boolean {
        return try {
            DAO.daoSession.markedDataDao.insertInTx(MarkedData(key, type, regex, value))
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    /**
     * 在截屏后释放
     */
    fun release() {
        cap?.release()
        cap = null
        screenData = null
    }

    override fun enableNfc() {
        switchNFC(true)
    }

    override fun disableNfc() {
        switchNFC(false)
    }

    /**
     * 你不能以编程的方式启用NFC。用户只能通过设置或用键件按钮手动的启用。
     * @param enable Boolean
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    private fun switchNFC(enable: Boolean) {
        if (ShellHelper.hasRootOrAdb()) {
            ShellHelper.execAuto("svc nfc " + (if (enable) "enable" else "disable"))
            return
        }
        val nfcManager = context.getSystemService(Context.NFC_SERVICE) as NfcManager?

        if (nfcManager == null || nfcManager.defaultAdapter == null) {
            GlobalLog.err("此设备不支持nfc")
        }
        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS).newTask())
    }

    /**
     * 利用无障碍服务
     * @param pkg String
     */
    override fun killApp(pkg: String): Boolean {
        @Suppress("UNREACHABLE_CODE")
        if (pkg == context.packageName) {
            exitProcess(0)
            return true
        }
        return if (ShellHelper.hasRootOrAdb()) {
            killAppByShell(pkg)
        } else killAppByAS(pkg)
    }

    private fun killAppByShell(pkg: String): Boolean {
        ShellHelper.execAuto("am force-stop $pkg")
        val name = AdvanAppHelper.getAppInfo(pkg)?.name ?: pkg
        GlobalApp.toastInfo("已关闭：$name")
        return true
    }

    private fun killAppByAS(pkg: String): Boolean {
        openAppDetail(pkg)
        if (!AccessibilityApi.isBaseServiceOn) {
            AppBus.post(RequestPermission("基础无障碍服务"))
            return true
        }

        val s = ViewFindBuilder()
            .equalsText("强行停止", "force stop")
            .waitFor(3000)

        val b = if (s?.tryClick() == true) {
            ViewFindBuilder().equalsText("确定", "OK")
                .waitFor(600)?.let {
                    sleep(200)
                    it.tryClick()
                } ?: ViewFindBuilder().containsText("强行停止", "force stop")
                .waitFor(600)?.tryClick() ?: false
        } else {
            GlobalApp.toastInfo("应用未运行")
            true
        }
        GlobalActionExecutor.home()
        return b
    }

    override fun getGlobalSettings(key: String): Any? {
        val cr = context.contentResolver
        return try {
            Settings.Global.getInt(cr, key)
        } catch (e: Settings.SettingNotFoundException) {
            try {
                Settings.Global.getFloat(cr, key)
            } catch (e: Settings.SettingNotFoundException) {
                Settings.Global.getString(cr, key)
            }
        }
    }

    override fun getSecureSettings(key: String): Any? {
        val cr = context.contentResolver
        return try {
            Settings.Secure.getInt(cr, key)
        } catch (e: Settings.SettingNotFoundException) {
            try {
                Settings.Secure.getFloat(cr, key)
            } catch (e: Settings.SettingNotFoundException) {
                Settings.Secure.getString(cr, key)
            }
        }
    }

    override fun putSecureSettings(key: String, value: Any) {
        val cr = context.contentResolver
        when (value) {
            is String -> {
                Settings.Secure.putString(cr, key, value)
            }
            is Int -> {
                Settings.Secure.putInt(cr, key, value)
            }
            is Float -> {
                Settings.Secure.putFloat(cr, key, value)
            }
        }
    }

    override fun putGlobalSettings(key: String, value: Any) {
        val cr = context.contentResolver
        when (value) {
            is String -> {
                Settings.Global.putString(cr, key, value)
            }
            is Int -> {
                Settings.Global.putInt(cr, key, value)
            }
            is Float -> {
                Settings.Global.putFloat(cr, key, value)
            }
        }
    }

    override fun getSystemSettings(key: String): Any? {
        val cr = context.contentResolver
        return try {
            Settings.System.getInt(cr, key)
        } catch (e: Settings.SettingNotFoundException) {
            try {
                Settings.System.getFloat(cr, key)
            } catch (e: Settings.SettingNotFoundException) {
                Settings.System.getString(cr, key)
            }
        }
    }

    override fun putSystemSettings(key: String, value: Any) {
        val cr = context.contentResolver
        when (value) {
            is String -> {
                Settings.System.putString(cr, key, value)
            }
            is Int -> {
                Settings.System.putInt(cr, key, value)
            }
            is Float -> {
                Settings.System.putFloat(cr, key, value)
            }
        }
    }

    override fun hasPermission(p: String): Boolean {
        return PermissionUtils.isAllGranted(GlobalApp.APP, arrayOf(p))
    }

    override var screenBrightness: Int
        get() {
            val contentResolver = GlobalApp.APP.contentResolver
            val defVal = 125
            return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal)
        }
        set(value) {
            val v = if (value < 0) 0 else if (value > 255) 255 else value
            val contentResolver = GlobalApp.APP.contentResolver
            Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, v)
        }

    override var screenBrightnessMode: Int
        get() {
            val contentResolver = GlobalApp.APP.contentResolver
            return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE, 0)
        }
        set(value) {
            val v = if (value == 0 || value == 1) value else 0
            val contentResolver = GlobalApp.APP.contentResolver
            Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE, v)
        }

    val displayMetrics
        get() = DisplayMetrics().also {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getRealMetrics(it)
        }

    val screenHW: Pair<Int, Int>
        get() {
            val m = displayMetrics
            return m.heightPixels to m.widthPixels
        }

    override val screenHeight: Int get() = screenHW.first

    override val screenWidth: Int get() = screenHW.second

    /**
     * 返回设备名
     * @param ip String
     * @return String?
     */
    private fun scanIp(ip: String, client: OkHttpClient): String? {
        Vog.d("scanIp $ip")
        val call = client.newCall(Request.Builder().url("http://$ip:8001").get().build())
        return try {
            call.execute().body?.string()
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * 扫描局域网中其他 VAssist 服务
     * 使用NSD
     * @return List<Pair<String, String>> ip to name
     */
    fun scanVAssistHostsInLAN(): List<Pair<String, String>> =
        ConnectiveNsdHelper.getDevices().map {
            it.host to it.name
        }


    /**
     * 发送脚本到 `BusServer`
     * @param ip2name List<Pair<String, String>>
     * @param script String
     * @param type String
     */
    @JvmOverloads
    fun sendScript2RemoteDevice(ip2name: List<Pair<String, String>>, script: String, type: String, toast: Boolean = true) {
        val ts = QuantumClock.currentTimeMillis

        //todo url 长度限制
        val ps = mapOf(
            "action" to "script",
            "script" to script,
            "type" to type,
            "from" to deviceName
        ).toQueryString()

        @Suppress("SpellCheckingInspection")
        val headers = mapOf(
            "ts" to ts.toString(),
            "sign" to SecureHelper.signData(ps, ts, "cssccssc")
        )
        val result = _send2Bus(ip2name, ps, headers)
        if (toast) {
            GlobalApp.toastInfo(result.joinToString("\n"), 1)
        }
    }

    private fun Map<String, String>.toQueryString(): String {
        return URLEncoder.encode(entries.joinToString("&") { it.key + "=" + it.value }, "utf-8")
    }

    @Suppress("FunctionName")
    private fun _send2Bus(
        ip2name: List<Pair<String, String>>,
        ps: String,
        hs: Map<String, String>
    ): List<String> = ip2name.map { (ip, name) ->
        val res = HttpBridge.get("http://$ip:8001/api?$ps", hs)
        if (res != null) {
            GlobalLog.log("远程指令[成功]：$ip $name")
            "$name 发送成功"
        } else {
            GlobalLog.err("远程指令[失败]：$ip $name")
            "$name 发送失败"
        }
    }

    /**
     * 发送指令 `BusServer`
     * @param ip2name List<Pair<String, String>>
     * @param cmd String
     * @param toast Boolean
     */
    @JvmOverloads
    fun sendCommand2RemoteDevice(ip2name: List<Pair<String, String>>, cmd: String, toast: Boolean = true) {
        val ts = QuantumClock.currentTimeMillis
        val ps = mapOf(
            "action" to "command",
            "command" to cmd,
            "from" to deviceName
        ).toQueryString()

        @Suppress("SpellCheckingInspection")
        val headers = mapOf(
            "ts" to ts.toString(),
            "sign" to SecureHelper.signData(ps, ts, "cssccssc")
        )
        val result = _send2Bus(ip2name, ps, headers)
        if (toast) {
            GlobalApp.toastInfo(result.joinToString("\n"), 1)
        }
    }

    override fun sendCommand2OtherDevices(cmd: String?) {
        val ip2name = scanVAssistHostsInLAN()
        when (ip2name.size) {
            0 -> GlobalApp.toastWarning("未发现设备")
            1 -> sendCommand2RemoteDevice(ip2name, cmd ?: "")
            else -> {
                DialogBridge.multiSelect("选择设备", ip2name.map { it.second + "\n" + it.first }.toTypedArray()) {
                    if (it != null) {
                        sendCommand2RemoteDevice(it.map { i -> ip2name[i] }, cmd ?: "")
                    }
                }
            }
        }
    }

    override val deviceName: String get() = Build.MODEL

    val isDarkMode
        get() = (GlobalApp.APP.resources!!.configuration.uiMode
            and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    override fun isAdbEnabled(): Boolean {
        val p2 = Runtime.getRuntime().exec("getprop init.svc.adbd")
        return (String(p2.inputStream.readBytes()).trimEnd() == "running").also {
            p2.destroy()
        }
    }

    override fun adbPort(): Int {
        val p = Runtime.getRuntime().exec("getprop service.adb.tcp.port")
        val t = String(p.inputStream.readBytes())
        return (t.trimEnd().toIntOrNull() ?: -1)
    }

    override fun isWirelessAdbEnabled(): Boolean = try {
        adbPort() in (1024..65535) && isAdbEnabled()
    } catch (e: Throwable) {
        GlobalLog.err(e)
        false
    }

    override fun currentApp(): AppInfo? {
        if (AccessibilityApi.isBaseServiceOn) {
            return AccessibilityApi.accessibilityService?.currentAppInfo
        }
        if (ShellHelper.hasRootOrAdb()) {
            currentScope()?.also {
                return AppInfo(it.packageName)
            }
        }
        return null
    }

    override fun currentScope(): ActionScope? {
        if (AccessibilityApi.isBaseServiceOn) {
            AccessibilityApi.currentScope?.also {
                return it
            }
        }

        if (ShellHelper.hasRootOrAdb()) {
            val ret = ShellHelper.execWithAdb("dumpsys window | grep mCurrentFocus=Window").blockedGet()
                ?: return null
            "dumpsys window: $ret".logi()
            val reg = "([\\S]+)/([\\S]+)".toRegex()
            val matchRet = reg.find(ret) ?: return null
            return ActionScope(matchRet.groupValues[1], matchRet.groupValues[2])
        }
        return null
    }
}