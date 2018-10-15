package cn.vove7.common.utils

import android.os.Looper

/**
 * # LooperHelper
 *
 * @author Administrator
 * 2018/10/12
 */
object LooperHelper {

    fun prepareIfNeeded() {
        if (Looper.myLooper() == null)
            Looper.prepare()
    }
}