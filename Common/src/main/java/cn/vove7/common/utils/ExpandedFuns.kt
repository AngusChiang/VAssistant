package cn.vove7.common.utils

import android.os.Handler
import android.os.Looper

/**
 * # ExpandedFuns
 *
 * @author Administrator
 * 2018/10/25
 */
/**
 * 代码块运行于UI线程
 * @param action () -> Unit
 */
fun runOnUi(action: () -> Unit) {
    val mainLoop = Looper.getMainLooper()
    if (mainLoop == Looper.myLooper()) {
        action.invoke()
    } else {
        Handler(mainLoop).post(action)
    }
}
