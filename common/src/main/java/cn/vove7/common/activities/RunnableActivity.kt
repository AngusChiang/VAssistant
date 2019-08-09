package cn.vove7.common.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.utils.newTask
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.vtp.log.Vog

/**
 * # RunnableActivity
 *
 * @author Vove
 * 2019/7/31
 */
class RunnableActivity : AppCompatActivity() {

    companion object {

        var shellAction: Function1<Activity, Unit>? = null

        fun runInShellActivity(action: Function1<Activity, Unit>) {
            shellAction = action

            GlobalApp.APP.apply {
                startActivity(Intent(this, RunnableActivity::class.java).newTask())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_shell)
        val action = shellAction
        Vog.d("runOnShell ${shellAction != null}")
        runOnNewHandlerThread {
            try {
                action?.invoke(this)
            } catch (e: Exception) {
                e.log()
            } finally {
                shellAction = null
            }
        }
    }

    //屏蔽按键
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return true
    }

    override fun finish() {
        overridePendingTransition(0, 0)
        super.finish()
    }

    override fun finishAndRemoveTask() {
        overridePendingTransition(0, 0)
        super.finishAndRemoveTask()
    }

    override fun onStop() {
        super.onStop()
        shellAction = null
    }

    override fun onDestroy() {
        super.onDestroy()
        shellAction = null
    }
}