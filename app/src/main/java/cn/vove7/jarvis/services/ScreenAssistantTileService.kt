package cn.vove7.jarvis.services

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi
import cn.vove7.common.utils.newTask
import cn.vove7.jarvis.activities.ScreenPickerActivity
import cn.vove7.jarvis.activities.screenassistant.QrCodeActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenAssistActivity

/**
 * # ScreenAssistantTileService
 * 屏幕助手通知栏快捷图块
 * @author Vove
 * 2019/7/7
 */
@RequiresApi(Build.VERSION_CODES.N)
class ScreenAssistantTileService : TileService() {
    override fun onClick() {
        startActivityAndCollapse(ScreenAssistActivity.createIntent(delayCapture = true))
    }
}

@RequiresApi(Build.VERSION_CODES.N)
class ScreenTextPickTileService : TileService() {
    override fun onClick() {
        startActivityAndCollapse(Intent(this, ScreenPickerActivity::class.java).apply {
            newTask()
            putExtra("delay", true)
        })
    }
}

@RequiresApi(Build.VERSION_CODES.N)
class QrCodeTileService : TileService() {
    override fun onClick() {
        startActivityAndCollapse(Intent(this, QrCodeActivity::class.java).apply {
            newTask()
            putExtra("delay", true)
        })
    }
}