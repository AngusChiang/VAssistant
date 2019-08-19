package cn.vove7.common.helper

import android.content.Context
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.runOnUi
import es.dmoral.toasty.Toasty

/**
 * # ToastyHelper
 *
 * @author 11324
 * 2019/3/16
 */
object ToastyHelper {
    init {
        Toasty.Config.getInstance()
                .allowQueue(true)
                .apply()
    }

    val app: Context get() = GlobalApp.APP
    fun toast(type: Int, msg: String, duration: Int) {
        runOnUi {
            when (type) {
                TYPE_INFO -> {
                    Toasty.info(app, msg, duration, false).show()
                }
                TYPE_SUCCESS -> {
                    Toasty.success(app, msg, duration, false).show()
                }
                TYPE_WARNING -> {
                    Toasty.warning(app, msg, duration, false).show()
                }
                TYPE_ERROR -> {
                    Toasty.error(app, msg, duration, false).show()
                }
            }
        }
    }

    const val TYPE_INFO = 0
    const val TYPE_SUCCESS = 1
    const val TYPE_WARNING = 2
    const val TYPE_ERROR = 3
}