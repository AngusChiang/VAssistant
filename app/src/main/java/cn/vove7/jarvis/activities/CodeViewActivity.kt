package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.databinding.ActivityCiewCodeBinding
import thereisnospon.codeview.CodeView
import thereisnospon.codeview.CodeViewTheme

/**
 * # CodeViewActivity
 *
 * @author Vove
 * 2019/6/30
 */
class CodeViewActivity : ReturnableActivity<ActivityCiewCodeBinding>() {

    lateinit var code: String

    override val darkTheme: Int
        get() = R.style.DarkTheme

    companion object {
        fun viewCode(context: Context, title: String, code: String, type: String) {
            val intent = Intent(context, CodeViewActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // code过大
            intent.putExtra("title", title)
            intent.putExtra("code", code)
            intent.putExtra("type", type)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.also {
            title = it.getStringExtra("title") +
                    "(" + it.getStringExtra("type") + ")"

            code = it.getStringExtra("code") ?: ""
        } ?: run {
            finish()
            return
        }

        if (isDarkTheme) {
            viewBinding.codeView.setBackgroundColor(0)
            viewBinding.codeView.setTheme(CodeViewTheme.DARK)
        }
        viewBinding.codeView.showCode(code)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("复制")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == "复制") {
            SystemBridge.setClipText(code)
            GlobalApp.toastInfo("已复制")
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}