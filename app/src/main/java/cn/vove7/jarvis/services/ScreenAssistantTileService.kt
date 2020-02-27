package cn.vove7.jarvis.services

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.utils.newTask
import cn.vove7.jarvis.activities.ScreenPickerActivity
import cn.vove7.jarvis.activities.screenassistant.QrCodeActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity

interface TileLongClickable {
    fun onLongClick()
}

/**
 * # ScreenAssistantTileService
 * 屏幕助手通知栏快捷图块
 * @author Vove
 * 2019/7/7
 */
@RequiresApi(Build.VERSION_CODES.N)
class ScreenAssistantTileService : TileService(), TileLongClickable {
    val intent get() = ScreenAssistActivity.createIntent(delayCapture = true)

    override fun onClick() {
        startActivityAndCollapse(intent)
    }

    override fun onLongClick() {
        GlobalApp.APP.startActivity(intent)
    }

}

@RequiresApi(Build.VERSION_CODES.N)
class ScreenTextPickTileService : TileService(), TileLongClickable {
    val intent
        get() = Intent(GlobalApp.APP, ScreenPickerActivity::class.java).apply {
            newTask()
            putExtra("delay", true)
        }

    override fun onClick() {
        startActivityAndCollapse(intent)
    }

    override fun onLongClick() {
        AppBus.postDelay(AppBus.ACTION_BEGIN_SCREEN_PICKER, 800)
    }
}

@RequiresApi(Build.VERSION_CODES.N)
class QrCodeTileService : TileService(), TileLongClickable {
    val intent
        get() = Intent(GlobalApp.APP, QrCodeActivity::class.java).apply {
            newTask()
            putExtra("delay", true)
        }

    override fun onClick() {
        startActivityAndCollapse(intent)
    }

    override fun onLongClick() {
        GlobalApp.APP.startActivity(intent)
    }
}