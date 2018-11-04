package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.jarvis.services.MainService
import cn.vove7.executorengine.parse.ParseEngine
import cn.vove7.vtp.log.Vog
import java.util.*

/**
 * # VoiceAssistReceiver
 *
 * 助手唤醒 与 桌面Shortcut快捷执行
 * @author 17719247306
 * 2018/9/10
 */
class VoiceAssistActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        Vog.d(this, "VoiceAssist ---> $action")
        when (action) {
            Intent.ACTION_ASSIST, "wakeup" -> {
                Vog.d(this, "onCreate ---> ASSIST wakeup")
                val data = intent.getBundleExtra(Intent.EXTRA_ASSIST_CONTEXT)
                for (k in data?.keySet() ?: emptySet()) {
                    Vog.d(this, "Bundle data ---> $k ${data.get(k)}")
                }
                MainService.switchReco()
            }
            else -> {
                try {
                    val id = action!!.toLong()
                    val node = DAO.daoSession.actionNodeDao.load(id)
                    if (node != null) {
                        val que = PriorityQueue<Action>()
                        if (node.belongInApp()) {
                            val scope = node.actionScope
                            if (scope != null)//App内 启动
                                que.add(Action(-999,
                                        String.format(ParseEngine.PRE_OPEN, scope.packageName),
                                        Action.SCRIPT_TYPE_LUA))
                        }
                        que.add(node.action)
                        node.action.param = null
                        AppBus.post(que)
                    } else {
                        GlobalApp.toastShort("指令不存在")
                    }
                } catch (e: Exception) {
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Vog.d(this, "onCreate ---> $referrer")
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, referrer)
                Vog.d(this, "onCreate ---> $bitmap")
            } catch (e: Exception) {
                Vog.d(this, "onCreate ---> null")
            }
        }
        finishAndRemoveTask()
    }
}