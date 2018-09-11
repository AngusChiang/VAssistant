package cn.vove7.jarvis

import android.content.Intent
import android.os.Build
import android.util.Log
import cn.vove7.androlua.LuaApp
import cn.vove7.common.appbus.MessageEvent
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.InitLuaDbData
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.utils.debugserver.RemoteDebugServer
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread

class App : LuaApp() {

    private lateinit var mainService: Intent
    private lateinit var synService: Intent
    lateinit var services: Array<Intent>

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        Vog.init(this, Log.VERBOSE).log2Local(Log.ERROR)
        mainService = Intent(this, MainService::class.java)
        services = arrayOf(mainService)

        if (BuildConfig.DEBUG)
            RemoteDebugServer.start()
        startServices()
        DAO.init(this)
        if (BuildConfig.DEBUG)
            InitLuaDbData.init()
    }

    private fun startServices() {
        thread {
            services.forEach {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            }
            if (SpHelper(this).getBoolean(R.string.key_open_ad_block, true))
                AdKillerService.bindServer()//广告服务
        }
    }

    override fun onTerminate() {
        services.forEach {
            stopService(it)
        }
        super.onTerminate()
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: MessageEvent) {
        when (event.what) {
            MessageEvent.WHAT_MSG_INFO -> {
                Vog.d(this, event.toString())
            }
            MessageEvent.WHAT_MSG_ERR -> Vog.e(this, event.toString())
        }
    }


}
