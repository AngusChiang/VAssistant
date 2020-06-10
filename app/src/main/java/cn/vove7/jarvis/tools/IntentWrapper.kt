package cn.vove7.jarvis.tools

import android.content.Intent
import cn.vove7.common.activities.RunnableActivity
import cn.vove7.common.app.AppConfig
import cn.vove7.vtp.log.Vog

/**
 * # IntentWrapper
 *
 * Created on 2020/6/10
 * @author Vove
 */

fun Intent.fixApplicationNewTask(): Intent? {
    //防止在外部无法打开其他应用
    val isVApp = component?.className?.let {
        kotlin.runCatching { Class.forName(it) }.isSuccess
    } ?: false
    Vog.d("isVApp $isVApp")
    return if (!isVApp && AppConfig.openAppCompat) {
        RunnableActivity.runInShellActivity {
            it.startActivity(this)
            it.finish()
        }
        null
    } else {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this
    }

}