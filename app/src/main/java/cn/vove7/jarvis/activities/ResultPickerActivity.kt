package cn.vove7.jarvis.activities

import android.app.Activity
import android.os.Bundle
import cn.vove7.jarvis.chat.UrlItem
import cn.vove7.jarvis.view.dialog.ResultDisplayDialog

/**
 * # ResultPickerActivity
 *
 * @author 11324
 * 2019/3/11
 */
class ResultPickerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.apply {
            if (!hasExtra("data")) {
                finish()
                return
            } else {
                ResultDisplayDialog(this@ResultPickerActivity, getStringExtra("title"),
                        getBundleExtra("data").getSerializable("items")
                                as ArrayList<UrlItem>).apply {
                    setOnDismissListener {
                        finish()
                    }
                    setOnCancelListener {
                        finish()
                    }
                    show()
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}