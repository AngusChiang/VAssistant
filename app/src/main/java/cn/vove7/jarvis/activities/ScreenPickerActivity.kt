package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.ActionScope

/**
 * # ScreenPickerActivity
 *
 * @author Administrator
 * 2018/10/14
 */
class ScreenPickerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppBus.postDelay(AppBus.ACTION_BEGIN_SCREEN_PICKER, 800)
        finish()
    }

}