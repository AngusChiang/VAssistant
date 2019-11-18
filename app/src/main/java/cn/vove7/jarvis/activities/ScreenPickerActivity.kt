package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import android.widget.CheckedTextView
import android.widget.Toast
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.common.view.finder.ScreenTextFinder
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.DataCollector
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.baiduaip.model.Point
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.view.dialog.TextOperationDialog
import cn.vove7.jarvis.view.dialog.WordSplitDialog
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import java.util.*

/**
 * # ScreenPickerActivity
 *
 * @author Administrator
 * 2018/10/14
 */
class ScreenPickerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppConfig.haveTextPickPermission()) {//免费次数
            GlobalApp.toastWarning("今日次数达到上限")
            finish()
            return
        }
        if (!AccessibilityApi.isBaseServiceOn) {
            AppBus.post(RequestPermission("无障碍服务"))
            finish()
            return
        }
        DataCollector.buriedPoint("sa_3")

        if (unSupportPage.contains(AccessibilityApi.accessibilityService?.currentScope)) {
            MainService.speak("不支持当前页")
            finish()
            return
        }

        val delay = intent.getBooleanExtra("delay", false)
        runOnNewHandlerThread(delay = if (delay) 1000 else 0) {

            val list = ScreenTextFinder().findAll().map {
                val rect = it.bounds
                val points = listOf(
                        Point(rect.left, rect.top),
                        Point(rect.right, rect.top),
                        Point(rect.right, rect.bottom),
                        Point(rect.left, rect.bottom)
                )
                TextOcrItem(
                        it.text ?: it.desc() ?: "", points, 1.0
                )
            }

            if (list.isEmpty()) {
                GlobalApp.toastInfo("未提取到任何内容")
            } else {
                TextOcrActivity.start(this, list as ArrayList<TextOcrItem>, intent.extras)
            }
            finish()
        }
    }

    /**
     * 不支持的页面
     */
    private val unSupportPage = hashSetOf(
            ActionScope("com.tencent.mtt", "com.tencent.mtt.MainActivity")
    )


}