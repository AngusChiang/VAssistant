package cn.vove7.executorengine.bridges

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
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.*
import android.provider.AlarmClock
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.telecom.TelecomManager
import android.view.KeyEvent
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vassistant.plugininterface.bridges.RootHelper
import cn.vassistant.plugininterface.bridges.SystemOperation
import cn.vassistant.plugininterface.bridges.UtilBridge.bitmap2File
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.ResultBox
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.utils.RegUtils
import cn.vove7.common.view.ScreenshotActivity
import cn.vove7.executorengine.R
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.executorengine.helper.AdvanContactHelper
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.calendar.CalendarAccount
import cn.vove7.vtp.calendar.CalendarHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper
import java.io.File
import java.util.*


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
        return AdvanContactHelper.matchPhone(name)
    }

    fun openAppByPkg(pkg: String): Boolean {
        return openAppByPkg(pkg, false)
    }

    override fun openAppByPkg(pkg: String, resetTask: Boolean): Boolean {
        return try {
            val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                GlobalLog.err("启动失败 未找到此App: $pkg")
//                Bus.postInfo(MessageEvent("启动失败(未找到此App[pkg:]):$pkg ", WHAT_ERR))
//                ExResult("未找到此App: $pkg")
                false
            } else {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (resetTask)
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(launchIntent)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Vog.wtf(this, "打开App{$pkg}失败 ${e.message}")
            GlobalLog.err(e.message ?: "未知错误")
//            Bus.postInfo(MessageEvent("启动失败:$pkg  errMsg:${e.message}", WHAT_ERR))
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
    override fun getPkgByWord(appWord: String): String? {
        val list = AdvanAppHelper.matchAppName(appWord)
        return if (list.isNotEmpty()) {
            list[0].data.packageName.also {
                Vog.i(this, "打开应用：$appWord -> $it")
            }
        } else null
    }

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
     * 输入 纯数字 | [标记]联系人
     * @param s String
     * @param simId Int? 卡号 0:卡1  1:卡2
     * @return Boolean
     */
    override fun call(s: String, simId: Int?): Boolean {
        val ph = AdvanContactHelper.matchPhone(s)
            ?: return {
                GlobalLog.err("未找到该联系人 $s")
                false
            }.invoke()
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ph"))
        if (simId != null) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
            val sId = if (simId >= phoneAccountHandleList.size) {
                GlobalLog.err("call ---> 卡号指定错误,使用默认卡拨号")
//                GlobalApp.toastShort("卡号无效")
                0
            } else simId
            try {
                callIntent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                        phoneAccountHandleList.get(sId))
            } catch (e: Exception) {
            }
        }
        return try {
            context.startActivity(callIntent)
            true
        } catch (e: SecurityException) {
            GlobalApp.toastShort("无电话权限")
            false
        } catch (e: Exception) {
            GlobalLog.err("call" + (e.message ?: "ERROR: UNKNOWN"))
            false
        }
    }

    /**
     * 打开手电
     */
    override fun openFlashlight(): Boolean {
        return switchFL(true)
    }

    /**
     * 关闭手电
     */
    override fun closeFlashlight(): Boolean {
        return switchFL(false)
    }

    @Throws(Exception::class)
    private fun switchFlashlight(on: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//<7.0
            val camera: Camera
            camera = Camera.open()
            val parameters = camera.parameters
            parameters.flashMode = if (!on) {
                Camera.Parameters.FLASH_MODE_OFF
            } else {
                Camera.Parameters.FLASH_MODE_TORCH
            }
            camera.parameters = parameters
        } else {
            //获取CameraManager
            val mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager;
            //获取当前手机所有摄像头设备ID
            val ids = mCameraManager.getCameraIdList();
            var r = false
            ids.forEach { id ->
                val c = mCameraManager.getCameraCharacteristics(id);
                //查询该摄像头组件是否包含闪光灯
                val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                val lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    //打开或关闭手电筒
                    mCameraManager.setTorchMode(id, on);
                    r = true
                }
            }
            if (!r) {
                throw Exception("未找到可用闪光灯")
            }
        }
    }

    private fun switchFL(on: Boolean): Boolean {
        try {
            switchFlashlight(on)
        } catch (e: Exception) {
            GlobalLog.err(e, "sfl282")
            GlobalApp.toastShort((if (on) "打开" else "关闭") + "手电失败")
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
            GlobalApp.toastShort("无可用浏览器")
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

    fun removeMusicFocus() {
        val mAm = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            mAm.abandonAudioFocusRequest(AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build())
        } catch (e: NoClassDefFoundError) {//7.1.2- 异常

        }
    }


    fun getMusicFocus() {
        val mAm = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
        val vIsActive = mAm.isMusicActive();
//        val mListener = MyOnAudioFocusChangeListener();
//        val a= AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                .setAudioAttributes()

        if (vIsActive) {
            val result = mAm.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Vog.d(this, "getMusicFocus ---> successfully")
            } else {
                Vog.d(this, "getMusicFocus ---> failed")
            }
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
//        removeMusicFocus()
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
                GlobalApp.toastShort("请先授予权限")
                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                context.startActivity(intent)
            } else {
                mAudioManager.ringerMode = mode
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastShort("设置失败")
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
            Vog.e(this, "isMusicActive：无法获取AudioManager引用")
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
            if (isEnable)
                manager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                        super.onStarted(reservation)
                        Vog.d(this, "onStarted ---> ")
                    }

                    override fun onStopped() {
                        super.onStopped()
                        Vog.d(this, "onStopped ---> ")
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        GlobalLog.err("失败：" + arrayOf(
                                0, ERROR_NO_CHANNEL
                                , ERROR_GENERIC
                                , ERROR_INCOMPATIBLE_MODE
                                , ERROR_TETHERING_DISALLOWED)[reason])
                        Vog.d(this, "onFailed ---> ")
                    }
                }, Handler())
            else {
                GlobalApp.toastShort("不支持关闭热点")
            }
        } catch (e: SecurityException) {
            AppBus.post(RequestPermission("位置权限"))
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastShort("出错")
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
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("", text)
        cm.primaryClip = mClipData
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
            GlobalApp.toastShort(R.string.text_not_fount_email_app)
        } catch (e: Exception) {
            GlobalLog.err(e.message + "code: sy358")
            GlobalApp.toastShort(R.string.text_failed_to_open_email_app)
        }

    }

    //todo
    override fun lockScreen(): Boolean {

        return false
    }

    @SuppressLint("MissingPermission")
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
            GlobalApp.toastShort("无法获取位置信息")
            GlobalLog.log("未打开位置设置")
            Vog.d(this, "location ---> 没有可用的位置提供器")
            return null
        }
        prepareIfNeeded()
        val resu = ResultBox<Location?>()
        var block = Runnable {}
        val handler = Handler()

        val loLis = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                Vog.d(this, "onLocationChanged ---> $location")
                handler.removeCallbacks(block)
                locationManager.removeUpdates(this)
                resu.setAndNotify(location)
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
            resu.setAndQuit(locationManager.getLastKnownLocation(locationProvider))
        }
        val looper = Looper.myLooper()
        locationManager.requestLocationUpdates(locationProvider, 500L, 0f, loLis, looper)
        handler.postDelayed(block, 5000)//等待5秒
        return resu.blockedGet()
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
        NetHelper.postJson<String>(ApiUrls.GET_IP) { _, b ->
            if (b?.isOk() == true)
                r.setAndNotify(b.data)
            else r.setAndNotify(null)
        }
        return r.blockedGet()
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

    override fun screenShot(): Bitmap? {
        val resultBox = ResultBox<Bitmap?>()
        val capIntent = ScreenshotActivity.getScreenshotIntent(context, resultBox)
        context.startActivity(capIntent)
        return resultBox.blockedGet()
    }

    override fun screen2File(): File? {
        val tmpPath = context.cacheDir.absolutePath + "/screen.png"
        return screen2File(tmpPath)
    }

    fun screen2File(p: String): File? {
        val screenBitmap = screenShot()
        return if (screenBitmap != null) {
            bitmap2File(screenBitmap, p)
        } else null
    }

    override fun shareText(content: String?) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
//            intent.putExtra(Intent.EXTRA_SUBJECT, title)
            intent.putExtra(Intent.EXTRA_TEXT, content ?: "")
            intent.setType("text/plain");
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(Intent.createChooser(intent, "分享到"))
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastShort("分享失败")
        }
    }

    override fun shareImage(imgPath: String?) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            if (imgPath ?: "" == "") {
                GlobalApp.toastShort("图片不存在")
                return
                // 纯文本
            } else {
                val f = File(imgPath)
                if (f.exists() && f.isFile) {
                    intent.type = "image/jpg"

                    val imgUri = if (Build.VERSION.SDK_INT >= 24)
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                    else Uri.fromFile(f)

                    intent.putExtra(Intent.EXTRA_STREAM, imgUri)
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(Intent.createChooser(intent, "分享到"))
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastShort("分享失败")
        }
    }

    fun createAlarm(hour: Int, minutes: Int) {
        createAlarm(null, null, hour, minutes, true)
    }

    override fun createAlarm(message: String?, days: Array<Int>?, hour: Int, minutes: Int, noUi: Boolean): Boolean {
        Vog.d(this, "createAlarm ---> $message ${Arrays.toString(days)} $hour : $minutes")
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, noUi)
        if (message != null) intent.putExtra(AlarmClock.EXTRA_MESSAGE, message)
        if (days != null) intent.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, ArrayList(days.toList()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            } else {
                GlobalApp.toastShort("未找到时钟App")
                return false
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            return false
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

    override fun screenOn() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            val wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            //点亮屏幕
            wl.acquire(10000);
            //释放
            wl.release();
        }
    }

    //发送电源按键
    override fun screenOff() {
        sendKey(66)
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
//        val smsManager = android.telephony.SmsManager.getDefault();
//        //拆分短信内容（手机短信长度限制）
//        smsManager.divideMessage(content).forEach {
//            smsManager.sendTextMessage(phone, null, it);
//        }

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"));
        intent.putExtra("sms_body", content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override val batteryLevel: Int
        get() {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            val intent = context.registerReceiver(null, filter);
            if (intent == null) return -1

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) //电量的刻度
            val maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100) //最大
            return level * 100 / maxLevel
        }
    override val isCharging: Boolean
        get() = {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            val intent = context.registerReceiver(null, filter);

            val i = intent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN) == BatteryManager.BATTERY_STATUS_CHARGING
            Vog.d(this, "isCharging ---> $i")
            i
        }.invoke()

    override val simCount: Int
        get() {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            if (telecomManager != null) {
                val phoneAccountHandleList =
                    telecomManager.getCallCapablePhoneAccounts()
                return phoneAccountHandleList?.size ?: 0
            }
            return 0
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

}
