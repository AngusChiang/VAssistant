package cn.vove7.executorengine.bridges

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.Context.WIFI_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.AudioManager.ADJUST_MUTE
import android.media.AudioManager.ADJUST_UNMUTE
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemOperation
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.model.ExResult
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.RegUtils
import cn.vove7.executorengine.R
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.executorengine.helper.AdvanContactHelper
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.hardware.HardwareHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper


object SystemBridge : SystemOperation {
    private val context: Context
        get() {
            return GlobalApp.APP
        }

    override fun openAppDetail(pkg: String): Boolean {
        return try {
            AppHelper.showPackageDetail(context, pkg)
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
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
        DAO.daoSession.markedDataDao.queryBuilder()
                .where(MarkedDataDao.Properties.Type.eq(MarkedData.MARKED_TYPE_APP))
                .list().forEach {
                    if (it.regex.matches(appWord)) {
                        Vog.d(this, "getPkgByWord match from marked ---> ${it.regStr} $appWord")
                        return it.value
                    }
                }

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
    override fun call(s: String): ExResult<String> {
        val ph = AdvanContactHelper.matchPhone(context, s)
            ?: return ExResult("未找到该联系人$s")// "未找到该联系人$s"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ph"))
        return try {
            context.startActivity(callIntent)
            ExResult()
        } catch (e: SecurityException) {

            ExResult("无电话权限")
        } catch (e: Exception) {
            val m = e.message ?: "ERROR: UNKNOWN"
            ExResult(m)
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

    private fun switchFL(on: Boolean): Boolean {
        try {
            HardwareHelper.switchFlashlight(context, on)
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastShort((if (on) "打开" else "关闭") + "手电失败")
            return false
        }
        return true
    }

    override fun getDeviceInfo(): DeviceInfo {
        return SystemHelper.getDeviceInfo(context)
    }

    override fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            GlobalApp.toastShort("无可用浏览器")
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
        sendMediaKey(keyCode)
    }

    override fun mediaPause() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
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

    /**
     * 当前音量静音
     */
    override fun volumeMute() {
        setMute(true)
    }

    fun setMute(state: Boolean) {
        val direction = if (state) ADJUST_MUTE else ADJUST_UNMUTE
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.adjustSuggestedStreamVolume(direction, AudioManager.USE_DEFAULT_STREAM_TYPE, 0)
    }

    override fun volumeUnmute() {
        setMute(false)
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
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (enabled) {
            // 因为wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.isWifiEnabled = false
        }
        if (Build.VERSION.SDK_INT >= 26) {
            Handler(Looper.getMainLooper()).post {
                setWifiApEnabledForAndroidO(enabled)
            }
            return true
        }

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
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setWifiApEnabledForAndroidO(isEnable: Boolean) {
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        //cancelLocalOnlyHotspotRequest 是关闭热点
        //打开热
        if (isEnable)
            manager.startLocalOnlyHotspot(getWifiApLis(), Handler())
        else {
            GlobalApp.toastShort("不支持关闭热点")
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    fun getWifiApLis():WifiManager.LocalOnlyHotspotCallback {
        return object : WifiManager.LocalOnlyHotspotCallback() {
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
        }
    }

    override fun isScreenOn(): Boolean {
        return SystemHelper.isScreenOn(context)
    }

    override fun getClipText(): String? {
        prepareIfNeeded()
        return SystemHelper.getClipBoardContent(context).toString()
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
        val loop = Looper.myLooper()

        val loLis = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                Vog.d(this, "onLocationChanged ---> $location")
                handler.removeCallbacks(block)
                locationManager.removeUpdates(this)
                resu.set(location)
                quitLoop()
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
            resu.set(locationManager.getLastKnownLocation(locationProvider))
            quitLoop()
        }
        locationManager.requestLocationUpdates(locationProvider, 500L, 0f, loLis, loop)
        handler.postDelayed(block, 5000)//等待5秒
        Looper.loop()
        return resu.get()
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

    override fun getIpAddress(): String? {
        return try {
            val wifiService = context.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiService.connectionInfo
            intToIp(wifiInfo.ipAddress)
        } catch (e: Exception) {
            GlobalLog.err(e)
            null
        }
    }

    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." + ((i shr 8) and 0xFF) +
                "." + ((i shr 16) and 0xFF) + "." + (i shr 24 and 0xFF)
    }

}
