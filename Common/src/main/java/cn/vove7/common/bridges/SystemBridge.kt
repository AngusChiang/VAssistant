@file:Suppress("unused")

package cn.vove7.common.bridges

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.SearchManager
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.Context.WIFI_SERVICE
import android.content.pm.PackageManager
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
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.view.KeyEvent
import cn.vove7.common.R
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.UtilBridge.bitmap2File
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.helper.AdvanContactHelper
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.ResultBox
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.WrapperNetHelper
import cn.vove7.common.utils.*
import cn.vove7.common.view.ScreenshotActivity
import cn.vove7.common.view.finder.ViewFindBuilder
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.calendar.CalendarAccount
import cn.vove7.vtp.calendar.CalendarHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper
import java.io.File
import java.util.*
import kotlin.concurrent.thread

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

    override fun openAppByPkg(pkg: String, clearTask: Boolean): Boolean {
        return try {
            val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                throw Exception("启动失败 未找到此App: $pkg")
            } else {
                launchIntent.newDoc()
                if (clearTask) {
                    launchIntent.clearTask()
                }
                context.startActivity(launchIntent)
                true
            }
        } catch (e: Exception) {
            GlobalLog.err("打开App{$pkg}失败: " + e.message)
            false
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
        val list = AdvanAppHelper.matchPkgByName(appWord)
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
            GlobalApp.toastInfo("无电话权限")
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
        val mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager;
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
            AppHelper.getAppInfo(context, "", s)
        } else {
            val pkg = getPkgByWord(s)
            AppHelper.getAppInfo(context, s, pkg ?: s)
        }
    }

    @Throws()
    override fun sendKey(keyCode: Int) {
        try {
            RootHelper.execWithSu("input keyevent $keyCode")
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
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
        val mAm = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
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


    private fun sendMediaKey(keyCode: Int) {
        var ke = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        intent.putExtra(Intent.EXTRA_KEY_EVENT, ke)
        GlobalApp.APP.sendBroadcast(intent)

        ke = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        intent.putExtra(Intent.EXTRA_KEY_EVENT, ke)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted()) {
                GlobalApp.toastInfo("请先授予权限")
                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
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

    override var musicMaxVolume: Int = -1
        get() {
            val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }
    override var musicCurrentVolume: Int = -1
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

    override fun vibrate(millis: Long): Boolean {

        val vibrateMan = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateMan.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateMan.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
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
        return try {
            val wifiMan = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiMan.isWifiEnabled = on
            true
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
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }

    /* 开启/关闭热点 */
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
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

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
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
                                0, ERROR_NO_CHANNEL
                                , ERROR_GENERIC
                                , ERROR_INCOMPATIBLE_MODE
                                , ERROR_TETHERING_DISALLOWED)[reason])
                        Vog.d("onFailed ---> ")
                    }
                }, Handler())
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

    override fun setClipText(text: String?) {
        prepareIfNeeded()
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val mClipData = ClipData.newPlainText("", text)
            cm.primaryClip = mClipData
        } catch (e: Throwable) {
            GlobalApp.toastError("复制失败：" + e.message)
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

    //TODO 测试
    override fun location(): Location? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AppBus.post(RequestPermission("位置权限"))
            return null
        }

        val serviceString = Context.LOCATION_SERVICE// 获取的是位置服务
        val locationManager = context.getSystemService(serviceString) as LocationManager
        val providers = locationManager.getProviders(true)
        val locationProvider: String = LocationManager.NETWORK_PROVIDER
        if (!providers.contains(LocationManager.NETWORK_PROVIDER)) {
            GlobalApp.toastInfo("无法获取位置信息")
            GlobalLog.log("未打开位置设置")
            Vog.d("location ---> 没有可用的位置提供器")
            return null
        }
        prepareIfNeeded()
        val result = ResultBox<Location?>()
        var block = Runnable {}
        val handler = Handler()

        val loLis = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                Vog.d("onLocationChanged ---> $location")
                handler.removeCallbacks(block)
                locationManager.removeUpdates(this)
                result.setAndNotify(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
            }
        }
        block = Runnable {
            locationManager.removeUpdates(loLis)
            GlobalLog.log("location ---> 获取位置超时,使用上次位置")
            result.setAndNotify(locationManager.getLastKnownLocation(locationProvider))
        }
        val looper = Looper.myLooper()
        locationManager.requestLocationUpdates(locationProvider, 500L, 0f, loLis, looper)
        handler.postDelayed(block, 5000)//等待5秒
        return result.blockedGet(false)
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
            null
        }
    }

    override fun getNetAddress(): String? {
        val r = ResultBox<String?>()
        WrapperNetHelper.postJson<String>(ApiUrls.GET_IP) {
            success { _, b ->
                if (b.isOk()) {
                    r.setAndNotify(b.data)
                } else r.setAndNotify(null)
            }
            fail { _, e ->
                e.log()
                r.setAndNotify(null)
            }
        }
        return r.blockedGet(false)
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

    fun screenShotWithRelease(): Bitmap? {
        return screenShot().also {
            release()
        }
    }

    override fun screenShot(): Bitmap? {
        Vog.d("screenShot ---> 请求截屏")
        if (screenData == null) {
            val resultBox = ResultBox<Intent?>()
            val capIntent = ScreenshotActivity.getScreenshotIntent(context, resultBox)
            context.startActivity(capIntent)
            screenData = resultBox.blockedGet(false) ?: return null
        }
        Vog.d("screenShot ---> $screenData")
        if (cap == null)
            cap = ScreenCapturer(context, screenData, -1, DeviceInfo.getInfo(context).screenInfo.density
                    , null)

        return try {
            cap?.capture()?.let {
                Vog.d("screenShot ---> $it")
                val bm = processImg(it)
                it.close()
                bm
            }
            //
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
//        val resultBox = ResultBox<Bitmap?>()
//        val capIntent = ScreenshotActivity.getScreenshotIntent(context, resultBox)
//        context.startActivity(capIntent)
//        return resultBox.blockedGet()
    }

    private fun processImg(image: Image): Bitmap? {
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

    fun screen2File(p: String): File? {
        val screenBitmap = screenShotWithRelease()
        return if (screenBitmap != null) {
            bitmap2File(screenBitmap, p)
        } else null
    }

    override fun shareText(content: String?) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
//            intent.putExtra(Intent.EXTRA_SUBJECT, title)
            intent.putExtra(Intent.EXTRA_TEXT, content ?: "")
            intent.type = "text/plain"
            context.startActivity(Intent.createChooser(intent, "分享到"))
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
            context.startActivity(Intent.createChooser(intent, "分享到"))
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
                .putExtra(AlarmClock.EXTRA_SKIP_UI, noUi)
        if (message != null) intent.putExtra(AlarmClock.EXTRA_MESSAGE, message)
        if (days != null) intent.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, ArrayList(days.toList()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
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
        if (AccessibilityApi.isBaseServiceOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            GlobalActionExecutor.lockScreen()
        } else sendKey(26)
    }

    override fun quickSearch(s: String?) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, s)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
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

    override val isCharging: Boolean
        get() = {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, filter)

            val i = intent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN) == BatteryManager.BATTERY_STATUS_CHARGING
            Vog.d("isCharging ---> $i")
            i
        }.invoke()

    @Suppress("DEPRECATION")
    override val simCount: Int
        get() {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            return if (telecomManager != null) {
                if (context.checkPermission(android.Manifest.permission.READ_PHONE_STATE)
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
    @Suppress("unused")
    private fun switchNFC(enable: Boolean) {
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
        return if (RootHelper.isRoot()) {
            killAppByRoot(pkg)
        } else killAppByAS(pkg)
    }

    private fun killAppByRoot(pkg: String): Boolean {
        RootHelper.execWithSu("am force-stop $pkg")
        val name = AdvanAppHelper.getAppInfo(pkg)?.name ?: pkg
        GlobalApp.toastInfo("已关闭：$name")
        return true
    }

    private fun killAppByAS(pkg: String): Boolean {
        openAppDetail(pkg)
        if (!AccessibilityApi.isBaseServiceOn) {
            AppBus.post(RequestPermission("基础无障碍服务"))
            return false
        }

        val s = ViewFindBuilder()
                .equalsText("强行停止", "force stop")
                .waitFor(3000)

        val b = if (s?.tryClick() == true) {
            ViewFindBuilder().equalsText("确定", "OK")
                    .waitFor(600)?.let {
                        Thread.sleep(200)
                        it.tryClick()
                    } ?: ViewFindBuilder().containsText("强行停止", "force stop")
                    .waitFor(600)?.tryClick() ?: false
        } else {
            GlobalApp.toastInfo("应用未运行")
            false
        }
        GlobalActionExecutor.home()
        return b
    }
}